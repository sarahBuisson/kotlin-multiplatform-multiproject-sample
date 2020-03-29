public open class NpmToMavenPlugin : Plugin<Project> {

    @javax.inject.Inject
    constructor() {
    }

    override fun apply(project: Project) {

        val buildPackageJsonForMaven = project.tasks.register<DefaultTask>("buildPackageJsonForMaven") {

            doLast {
                println("run buildPackageJsonForMaven")
                val packageJsonData = mutableMapOf(
                        "name" to project.name + "-js",
                        "version" to project.version,
                        "private" to true,
                        "dependencies" to mutableMapOf<String, Any?>(),
                        "scripts" to mutableMapOf<String, Any?>(
                                "postinstall" to "install-jar-dependency package.json"
                        ),
                        "jarDependencies" to mutableMapOf<String, Any?>(),
                        "mavenDependencies" to mutableMapOf<String, Any?>(),
                        "workspaces" to mutableListOf<Any?>(),
                        "bundledDependencies" to mutableListOf<Any?>(),
                        "main" to project.name,
                        "author" to mutableMapOf<String, Any?>(
                                "name" to "Sarah Buisson",
                                "email" to "sarah.buisson@gmail.com",
                                "website" to "https://github.com/sarahBuisson/"
                        ),
                        "homepage" to "https://github.com/sarahBuisson/easy-rules",
                        "license" to "ISC"
                )

                //download all the maven js dependencies and put them into an embedded directory.
                //TO DO : we probably don"t need to do so now we have mavenDependancy plugin
                project.configurations.get("jsMainImplementation").allDependencies.forEach {
                    if (it != null && (it.name.contains("js") || it.name.contains("npm"))) {
                        (packageJsonData.get("mavenDependencies") as MutableMap<String, Any?>).put(
                                it.name,
                                "${it.group}:${it.name}:${it.version}"
                        )
                    }
                }
                val projectClass: Class<org.gradle.api.internal.artifacts.dependencies.DefaultProjectDependency> = org.gradle.api.internal.artifacts.dependencies.DefaultProjectDependency::class.java
                project.configurations.get("commonMainImplementation").allDependencies.filterIsInstance(projectClass).forEach {

                    (packageJsonData.get("mavenDependencies") as MutableMap<String, Any?>).put(
                            it.name,
                            "${it.group}:${it.name}:${it.version}"
                    )

                }
                val kotlinPlugin = project.plugins.find { it.javaClass.name.contains("KotlinMultiplatform") }
                val kotlinVersionPlugin = kotlinPlugin?.javaClass?.getMethod("getKotlinPluginVersion")?.invoke(kotlinPlugin)
                if (kotlinVersionPlugin == null) {
                    throw Exception("need   \"apply plugin: 'org.jetbrains.kotlin.multiplatform'\"")
                }
                (packageJsonData.get("dependencies") as MutableMap<String, Any>).put("kotlin", kotlinVersionPlugin)

                project.plugins
                val packageJson = project.file("${project.buildDir}/tmp/package.json")
                if (!packageJson.getParentFile().exists())
                    packageJson.getParentFile().mkdirs()
                packageJson.createNewFile()
                packageJson.writeText(groovy.json.JsonBuilder(packageJsonData).toPrettyString())
            }
        }



        project.tasks.register("unpackJsNpm", Copy::class) {
            println("register unpackJsNpm in ${project.name}")
            val jarJsPath = "${project.buildDir}/libs/${project.name}-js-${project.version}.jar"
            this.from(project.zipTree(jarJsPath))
            this.into("${project.buildDir}/jsNpmToMaven")
            this.dependsOn("jsJar")
        }

        val movePackageJson = project.tasks.register("movePackageJson", Copy::class) {

            this.from("${project.buildDir}/tmp/package.json")
            this.into("${project.buildDir}/jsNpmToMaven")
            this.dependsOn(buildPackageJsonForMaven)
        }

        val packJsNpmToMaven by project.tasks.registering(Zip::class) {
            println("register packJsNpmToMaven")
            this.from("${project.buildDir}/jsNpmToMaven")
            this.archiveFileName.set("${project.name}-npm-${project.version}.jar")
            this.destinationDirectory.set(project.file("${project.buildDir}/npm"))
            this.dependsOn("unpackJsNpm")
            this.dependsOn(movePackageJson)
            println(project.tasks.names)

        }

        project.apply(plugin = ("maven-publish"))

        val publishingExtension = project.extensions["publishing"] as org.gradle.api.publish.internal.DefaultPublishingExtension;
         publishingExtension.publications {
           register("mavenGithub", MavenPublication::class) {
                artifact(project.file("${project.buildDir}/npm/${project.name}-npm-${project.version}.jar"))
                groupId = project.group.toString()
                artifactId = project.name
                version = project.version.toString()
            }

        }

    }
}

project.apply<NpmToMavenPlugin>()