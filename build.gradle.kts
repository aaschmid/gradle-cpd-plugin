import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter

plugins {
    groovy
    jacoco
    id("com.github.kt3k.coveralls") version "2.11.0"

    `java-gradle-plugin`

    `maven-publish`
    signing
    id("com.gradle.plugin-publish") version "0.13.0"
    id("com.jfrog.bintray") version "1.8.5"
}

description = "Gradle plugin to find duplicate code using PMDs copy/paste detection (= CPD)"
group = "de.aaschmid"
version = "3.3-SNAPSHOT"

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
    compileOnly("net.sourceforge.pmd:pmd-dist:6.10.0")

    testImplementation("net.sourceforge.pmd:pmd-dist:6.10.0")
    testImplementation("com.google.guava:guava:28.1-jre")
    testImplementation("org.junit.jupiter:junit-jupiter:5.5.2")
    testImplementation("org.assertj:assertj-core:3.13.2")
    testImplementation("org.mockito:mockito-core:3.1.0")
    testImplementation("org.mockito:mockito-junit-jupiter:3.1.0")

    "integTestImplementation"("org.assertj:assertj-core:3.13.2")
    "integTestImplementation"("org.junit.vintage:junit-vintage-engine:5.5.2")
    "integTestImplementation"("org.spockframework:spock-core:1.3-groovy-2.5") {
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
    toolVersion = "0.8.6"
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
    }
    check {
        dependsOn(integTest)
    }

    val jacocoMerge = register("jacocoMerge", JacocoMerge::class) {
        executionData(withType(Test::class).toSet())
        dependsOn(test, integTest)
    }

    jacocoTestReport {
        executionData(jacocoMerge.get().destinationFile)
        reports {
            xml.isEnabled = true
            html.isEnabled = false
        }
        dependsOn(jacocoMerge)
    }
}

gradlePlugin {
    pluginSourceSet(sourceSets.main.get())
    testSourceSets(sourceSets.test.get(), sourceSets.named("integTest").get())

    plugins {
        register("cpd") {
            id = "de.aaschmid.cpd"
            implementationClass = "de.aaschmid.gradle.plugins.cpd.CpdPlugin"
        }
        // Note: comment out for "publishPlugins" because old plugin ids are no longer supported
        //       but working while downloading plugin from MavenCentral
        register("legacyCpd") {
            id = "cpd"
            implementationClass = "de.aaschmid.gradle.plugins.cpd.CpdPlugin"
        }
    }
}

// -- sign and publish artifacts -------------------------------------------------------------------------------------

// Steps:
//   0. Set correct artifact version above, commit and create a tag prefixed with "v"
//   1. Prepare ~/.gradle/gradle.properties in order to contain signing keys and required passwords for publishing
//   2. "build"
//   3. "bintrayUpload"
//   4. Comment out "legacyCpd" in "gradlePlugin" closure and "publishPlugin"
//   5. Finish milestone, release on Github
//   6. Check bintray if sync and release was done on oss.sonatype.org

val isReleaseVersion by extra(!project.version.toString().endsWith("-SNAPSHOT"))

// usernames and passwords from `gradle.properties` otherwise empty
val sonatypeUsername by extra(findProperty("sonatypeUsername")?.toString() ?: "")
val sonatypePassword by extra(findProperty("sonatypePassword")?.toString() ?: "")
val bintrayUsername by extra(findProperty("bintrayUsername")?.toString() ?: "")
val bintrayApiKey by extra(findProperty("bintrayApiKey")?.toString() ?: "")
val sonatypeTokenUser by extra(findProperty("sonatypeTokenUser")?.toString() ?: "")
val sonatypeTokenPassword by extra(findProperty("sonatypeTokenPassword")?.toString() ?: "")

apply(from = "legacy-build.gradle")
