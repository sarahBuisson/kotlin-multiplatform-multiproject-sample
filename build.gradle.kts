import groovy.json.JsonBuilder
import org.gradle.api.publish.maven.MavenPublication


val rootGroup = "com.example.mkotlinVersiony.library"
val rootVersion = "0.0.5-SNAPSHOT"

plugins {
    kotlin("multiplatform") version "1.3.72"
    id("maven-publish")
    jacoco

}
allprojects {
    this.group = rootGroup
    this.version = rootVersion
    repositories {
        maven(url ="https://packagecloud.io/sarahBuisson/sarahbuisson/maven2")
        google()
        jcenter()
        mavenLocal()
    }

}

subprojects {
    apply(from = "${rootDir}/gradle/scripts/kotlinMultiNpm.gradle.kts")
    apply(from = "${rootDir}/gradle/scripts/jacoco.gradle.kts")
    apply(from = "${rootDir}/gradle/scripts/github.gradle.kts")
}
