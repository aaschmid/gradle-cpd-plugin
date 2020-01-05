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

apply(from = "legacy-build.gradle")
