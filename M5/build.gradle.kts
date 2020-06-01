plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("net.akehurst.kotlin.kt2ts") apply(true)
}
kotlin {
    jvm("jvm") {

        mavenPublication {
            artifactId = project.name + "-jvm"
        }
    }

    js("js") {
        mavenPublication {
            artifactId = project.name + "-js"
        }
    }
    metadata {
        mavenPublication {
            artifactId = project.name + "-common"
        }
        //this.metadataJar.appendix = "common"
    }
    // For ARM, should be changed to iosArm32 or iosArm64
    // For Linux, should be changed to e.g. linuxX64
    // For MacOS, should be changed to e.g. macosX64
    // For Windows, should be changed to e.g. mingwX64
    sourceSets {
        commonMain {
            dependencies {
                this.implementation(kotlin("stdlib-common"))
                this.implementation(project(":M3"))
            }
        }
        commonTest {
            dependencies {
                this.implementation(kotlin("test-common"))
                this.implementation(kotlin("test-annotations-common"))
            }
        }
        val jvmCompilation = jvm().compilations["main"]
        jvmCompilation.kotlinOptions { jvmTarget = "1.8" }
        jvmCompilation.defaultSourceSet {
            dependencies {
                implementation("io.github.microutils:kotlin-logging-common:1.7.9")
                implementation(kotlin("stdlib-jdk8"))
                implementation("org.jeasy:easy-rules-api-jvm:3.2.4-SNAPSHOT")
            }
        }

        jvm().compilations["test"].defaultSourceSet {
            dependencies {
                this.implementation(kotlin("test"))
                this.implementation(kotlin("test-junit"))
            }
        }
        js().compilations["main"].kotlinOptions { moduleKind = "umd" }
        js().compilations["main"].defaultSourceSet {
            dependencies {
                implementation(kotlin("stdlib-js"))
                implementation("io.github.microutils:kotlin-logging-js:1.7.9")
                implementation("org.jeasy:easy-rules-api-npm:3.2.4-SNAPSHOT")
            }
        }
        js().compilations["test"].defaultSourceSet {
            dependencies {
                this.implementation(kotlin("test-js"))
            }
        }
    }
}
kt2ts {
    declarationsFile.set(file("${project.buildDir}/jsNpmToMaven/index.d.ts"))
    classPatterns.set(listOf(
            "sample5.*"
    ))
}