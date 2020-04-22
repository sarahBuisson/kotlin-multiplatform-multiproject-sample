plugins {
    id("org.jetbrains.kotlin.multiplatform")
}
kotlin {
    jvm("jvm") {

        mavenPublication {
            artifactId =project.name +"-jvm"
        }
    }



    js() {
        nodejs()
        mavenPublication {
            artifactId = project.name + "-js"
        }
        compilations.getAt("main").kotlinOptions {
            println(this)
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
        jvm().compilations["main"].kotlinOptions { jvmTarget = "1.8"}
        jvm().compilations["main"].defaultSourceSet {

            dependencies {
                this.implementation(kotlin("stdlib-jdk8"))
            }
        }
        jvm().compilations["test"].defaultSourceSet {
            dependencies {
                this.implementation(kotlin("test"))
                this.implementation(kotlin("test-junit"))
            }
        }
        js().compilations["main"].kotlinOptions { moduleKind = "umd"}
        js().compilations["main"].defaultSourceSet {
            dependencies {
                this.implementation(kotlin("stdlib-js"))
            }
        }
        js().compilations["test"].defaultSourceSet {
            dependencies {
                this.implementation(kotlin("test-js"))
            }
        }
    }
}