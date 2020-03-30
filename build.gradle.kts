import groovy.json.JsonBuilder
import org.gradle.api.publish.maven.MavenPublication


repositories {
    mavenCentral()
}
val kotlinVersion = "1.3.61"
val rootGroup = "com.example.my.library"
val rootVersion = "0.0.4-SNAPSHOT"


plugins {
    val kotlinVersion = "1.3.61"
    kotlin("multiplatform").version(kotlinVersion)
    id("maven-publish")
    jacoco

}
allprojects {
    this.group = rootGroup
    this.version = rootVersion

}

subprojects {
    this.extra.set("kotlinVersion", kotlinVersion)
    //apply(from = "${rootDir}/gradle/scripts/kotlinMultiNpm.gradle.kts")
    apply(from = "${rootDir}/gradle/scripts/jacoco.gradle.kts")
    apply(from = "${rootDir}/gradle/scripts/github.gradle.kts")

}
