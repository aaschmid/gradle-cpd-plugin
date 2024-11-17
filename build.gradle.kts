import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter

plugins {
    groovy
    jacoco
    id("com.github.kt3k.coveralls") version "2.11.0"

    `java-gradle-plugin`

    id("com.gradle.plugin-publish") version "0.13.0"
}

description = "Gradle plugin to find duplicate code using PMDs copy/paste detection (= CPD)"
group = "de.aaschmid"
version = "3.5"

val isBuildOnJenkins by extra(System.getenv("BUILD_TAG")?.startsWith("jenkins-") ?: false)

repositories {
    mavenCentral()
}

sourceSets {
    register("integTest") {
        compileClasspath += main.get().output + test.get().output
        runtimeClasspath += main.get().output + test.get().output
    }
}

dependencies {
    compileOnly("net.sourceforge.pmd:pmd-dist:7.7.0")

    testImplementation("net.sourceforge.pmd:pmd-dist:7.7.0")
    testImplementation("com.google.guava:guava:33.1.0-jre")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testImplementation("org.assertj:assertj-core:3.25.3")
    testImplementation("org.mockito:mockito-core:5.11.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.11.0")

    "integTestImplementation"("org.assertj:assertj-core:3.25.3")
    "integTestImplementation"("org.junit.vintage:junit-vintage-engine:5.10.2")
    "integTestImplementation"("org.spockframework:spock-core:2.3-groovy-3.0") {
        exclude(module = "groovy-all")
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8

    withJavadocJar()
    withSourcesJar()
}

jacoco {
    toolVersion = "0.8.12"
}

tasks {
    named<Javadoc>("javadoc") {
        if (JavaVersion.current().isJava9Compatible) {
            (options as StandardJavadocDocletOptions).addBooleanOption("html5", true)
        }
    }

    jar {
        manifest {
            val now = LocalDate.now()
            val today = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

            attributes(
                    "Built-By" to "Gradle ${gradle.gradleVersion}",
                    "Built-Date" to today, // using now would destroy incremental build feature
                    "Specification-Title" to "gradle-cpd-plugin",
                    "Specification-Version" to project.version,
                    "Specification-Vendor" to "Andreas Schmid, service@aaschmid.de",
                    "Implementation-Title" to "gradle-cpd-plugin",
                    "Implementation-Version" to project.version,
                    "Implementation-Vendor" to "Andreas Schmid, service@aaschmid.de"
            )
        }
    }

    test {
        ignoreFailures = isBuildOnJenkins

        useJUnitPlatform()
        testLogging {
            exceptionFormat = TestExceptionFormat.FULL
        }
    }

    val integTest = register("integTest", Test::class) {
        inputs.files(jar)
        shouldRunAfter(test)

        ignoreFailures = isBuildOnJenkins

        testClassesDirs = sourceSets.named("integTest").get().output.classesDirs
        classpath = sourceSets.named("integTest").get().runtimeClasspath

        useJUnitPlatform()
    }

    check {
        dependsOn(integTest)
    }

    jacocoTestReport {
        executionData(withType(Test::class).toSet())
        reports {
            xml.required.set(true)
            html.required.set(false)
        }
        dependsOn(withType(Test::class))
    }
}

gradlePlugin {
    pluginSourceSet(sourceSets.main.get())
    testSourceSets(sourceSets.test.get(), sourceSets.named("integTest").get())

    plugins {
        create("cpd") {
            id = "de.aaschmid.cpd"
            implementationClass = "de.aaschmid.gradle.plugins.cpd.CpdPlugin"
        }
    }
}

// -- sign and publish artifacts -------------------------------------------------------------------------------------

// Steps:
//   0. Set correct artifact version above, commit and create a tag prefixed with "v"
//   1. Prepare ~/.gradle/gradle.properties in order to contain signing keys and required passwords for publishing
//   2. "./gradlew build publishPlugin"
//   3. Finish milestone and release on Github

val isReleaseVersion by extra(!project.version.toString().endsWith("-SNAPSHOT"))

// See documentation on https://plugins.gradle.org/docs/publish-plugin
pluginBundle {
    website = "https://github.com/aaschmid/gradle-cpd-plugin"
    vcsUrl = "https://github.com/aaschmid/gradle-cpd-plugin"

    description = "A Gradle plugin to find duplicate code using PMDs copy/paste detection (= CPD)."
    tags = listOf("duplicates", "cpd", "copy-paste-detection")

    (plugins) {
        "cpd" {
            displayName = "Gradle CPD plugin"
        }
    }

    mavenCoordinates {
        groupId = project.group as String
        artifactId = "gradle-cpd-plugin"
    }
}
