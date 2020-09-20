import kotlin.reflect.full.functions

class NpmToMavenPlugin : Plugin<Project> {

    @javax.inject.Inject
    constructor() {
    }

    override fun apply(project: Project) {
        val unpackJsNpm by project.tasks.registering(Copy::class) {
            println("register unpackJsNpm in ${project.name}")
            val jarJsPath = "${project.buildDir}/libs/${project.name}-js-${project.version}.jar"
            this.from(project.zipTree(jarJsPath))
            this.into("${project.buildDir}/jsNpmToMaven")
            this.dependsOn("jsJar")
        }

        val buildPackageJsonForMaven = project.tasks.register<DefaultTask>("buildPackageJsonForMaven") {

            doLast {
                println("run buildPackageJsonForMaven")
                val jsNpmToMavenDir = project.file("${project.buildDir}/jsNpmToMaven")

                //package json generated by gradle kotlin plugin
                val gradlepackageJsonPath = "${project.rootProject.buildDir}/js/packages/${project.rootProject.name}-${project.name}/package.json"
                val mavenDependencies = mutableMapOf<String, Any?>()
                val dependencies = mutableMapOf<String, Any?>()
                val allJsDependencies = project.configurations.get("jsMainImplementation").allDependencies + project.configurations.get("commonMainImplementation").allDependencies
                val allDependencies = project.configurations.get("commonMainImplementation").allDependencies + project.configurations.get("commonMainImplementation").allDependencies

                if (project.file(gradlepackageJsonPath).exists()) {
                    val gradlePackageJson: Map<String, Object> = groovy.json.JsonSlurper().parseText(project.file(gradlepackageJsonPath).readText()) as Map<String, Object>
                    val gradleGeneratedDependencies: Map<String, String> = gradlePackageJson.get("dependencies") as Map<String, String>
                    gradleGeneratedDependencies
                            .filter { it.key != "kotlin-source-map-loader" }//TODO : delete when this dependencie is available
                            .filter { it.key != "kotlin-test-nodejs-runner" }//TODO : delete when this dependencie is available
                            .filter { it.key != "kotlin-test" }//TODO : delete when this dependencie is available
                            .forEach { entry ->
                                println("allJsDependencies")
                                println(allJsDependencies)
                                println("mvnMainDependency")
                                println(allDependencies)
                                val mvnJsDependency = allJsDependencies.find { projectDep ->
                                    val shortdepName = projectDep.name.replace("-npm", "").replace("-js", "")
                                    entry.key.endsWith(shortdepName)
                                }
                                val mvnMainDependency = allDependencies.find { projectDep ->
                                    val shortdepName = projectDep.name.replace("-npm", "").replace("-js", "")
                                    entry.key.endsWith(shortdepName)
                                }
                                if (mvnJsDependency != null) {
                                    if (mvnJsDependency is org.gradle.api.internal.artifacts.dependencies.DefaultProjectDependency) {

                                        mavenDependencies.put(entry.key, "${mvnJsDependency.group}:${mvnJsDependency.name}-npm:${mvnJsDependency.version}")
                                        if (willBeTypescript()) {
                                            mavenDependencies.put("${mvnJsDependency.group}-${mvnJsDependency.name.replace("-npm","")}", "${mvnJsDependency.group}:${mvnJsDependency.name}-npm:${mvnJsDependency.version}")
                                        }
                                    } else {
                                        mavenDependencies.put(entry.key, "${mvnJsDependency.group}:${mvnJsDependency.name}:${mvnJsDependency.version}")
                                        if (willBeTypescript()) {
                                            mavenDependencies.put("${mvnJsDependency.group}-${mvnJsDependency.name.replace("-npm","")}", "${mvnJsDependency.group}:${mvnJsDependency.name}:${mvnJsDependency.version}")
                                        }
                                    }
                                } else {
                                    dependencies.put(entry.key, entry.value)

                                }
                            }
                }
                val mainJs: String = (jsNpmToMavenDir?.listFiles()?.first { it.extension == "js" && !it.nameWithoutExtension.endsWith("meta") }?.name)
                        ?: "index.js"
                val packageJsonData = mutableMapOf(
                        "name" to project.name + "-js",
                        "version" to project.version,
                        "private" to true,
                        "dependencies" to dependencies,
                        "scripts" to mutableMapOf<String, Any?>(
                                "postinstall" to "npx install-jar-dependency package.json"
                        ),
                        "jarDependencies" to mutableMapOf<String, Any?>(),
                        "mavenDependencies" to mavenDependencies,
                        "devDependencies" to mutableMapOf<String, Any?>("install-jar-dependency" to "0.0.13"),
                        "installJarConfig" to mutableMapOf<String, Any?>("additionalMavenRepositories" to listOf<String>(
                                "https://packagecloud.io/sarahBuisson/snapshot/maven2/",
                                "https://packagecloud.io/sarahBuisson/sarahbuisson/maven2/")
                        ),
                        "workspaces" to mutableListOf<Any?>(),
                        "bundledDependencies" to mutableListOf<Any?>(),
                        "main" to mainJs,
                        "author" to mutableMapOf<String, Any?>(
                                "name" to "Sarah Buisson",
                                "email" to "sarah.buisson@gmail.com",
                                "website" to "https://github.com/sarahBuisson/"
                        ),
                        "license" to "ISC"
                )

                //download all the maven js dependencies and put them into an embedded directory.
                //TO DO : we probably don"t need to do so now we have mavenDependancy plugin

                val kotlinPlugin = project.plugins.find { it.javaClass.name.contains("KotlinMultiplatform") }
                val kotlinVersionPlugin = kotlinPlugin?.javaClass?.getMethod("getKotlinPluginVersion")?.invoke(kotlinPlugin)
                if (kotlinVersionPlugin == null) {
                    throw Exception("need   \"apply plugin: 'org.jetbrains.kotlin.multiplatform'\"")
                }
                (packageJsonData.get("dependencies") as MutableMap<String, Any>).put("kotlin", kotlinVersionPlugin)
                if (willBeTypescript()) {
                    packageJsonData.set("types", "index.d.ts")
                    (packageJsonData.get("mavenDependencies") as MutableMap<String, Any>).put("unknown-kotlin", "org.jetbrains.kotlin:kotlin-stdlib-js:"+kotlinVersionPlugin)
                }
                project.plugins
                val packageJson = project.file("${project.buildDir}/jsNpmToMaven/package.json")
                if (!packageJson.getParentFile().exists())
                    packageJson.getParentFile().mkdirs()
                packageJson.createNewFile()
                packageJson.writeText(groovy.json.JsonBuilder(packageJsonData).toPrettyString())
                if (project.tasks.findByName("generateTypescriptDefinitionFile") != null) {

                    project.apply(plugin = "net.akehurst.kotlin.kt2ts")
                    val extensionKt2ts = project.extensions["kt2ts"]
                    val pr: RegularFileProperty = extensionKt2ts::class.members
                            .find { it.name=="declarationsFile" }!!
                            .call(extensionKt2ts) as RegularFileProperty
                    val typescriptDef:File = project.file("${project.buildDir}/jsNpmToMaven/index.d.ts")
                    if (!typescriptDef.exists()) {
                        typescriptDef.createNewFile()
                        typescriptDef.writeText("//File to fill by plugin kt2s")
                    }
                    pr.set(typescriptDef)
                }

            }
            println(project.tasks)
            if (project.tasks.findByName("generateTypescriptDefinitionFile") != null) {
                (project.tasks.findByName("buildPackageJsonForMaven")!! as Task).dependsOn(project.tasks.findByName("generateTypescriptDefinitionFile"))
            }
            this.dependsOn(unpackJsNpm)

        }


        if (project.tasks.findByName("generateTypescriptDefinitionFile") != null) {
            project.apply(plugin = "net.akehurst.kotlin.kt2ts")
            val extensionKt2ts = project.extensions["kt2ts"]
            val pr: RegularFileProperty = extensionKt2ts::class.members
                    .find { it.name=="declarationsFile" }!!
                    .call(extensionKt2ts) as RegularFileProperty
            val typescriptDef:File = project.file("${project.buildDir}/jsNpmToMaven/index.d.ts")
            pr.set(typescriptDef)


        }

        val packJsNpmToMaven by project.tasks.registering(Zip::class) {
            println("register packJsNpmToMaven")
            this.from("${project.buildDir}/jsNpmToMaven")
            this.archiveFileName.set("${project.name}-npm-${project.version}.jar")
            this.destinationDirectory.set(project.file("${project.buildDir}/libs"))
            this.dependsOn(unpackJsNpm)
            this.dependsOn(buildPackageJsonForMaven)

        }
        val packJsNpmToTgz by project.tasks.registering(Zip::class) {
            println("register packJsNpmToMaven")
            this.from("${project.buildDir}/jsNpmToMaven")
            this.archiveFileName.set("${project.name}-npm-${project.version}.tgz")
            this.destinationDirectory.set(project.file("${project.buildDir}/libs"))
            this.dependsOn(unpackJsNpm)
            this.dependsOn(buildPackageJsonForMaven)
            println(project.tasks.names)

        }


        project.apply(plugin = ("maven-publish"))

        val publishingExtension = project.extensions["publishing"] as org.gradle.api.publish.internal.DefaultPublishingExtension;
        publishingExtension.publications {
            register("npmGithub", MavenPublication::class) {
                artifact(project.file("${project.buildDir}/libs/${project.name}-npm-${project.version}.jar"))
                groupId = project.group.toString()
                artifactId = project.name + "-npm"
                version = project.version.toString()

            }
            register("mavenJsGithub", MavenPublication::class) {
                artifact(project.file("${project.buildDir}/libs/${project.name}-js-${project.version}.jar"))
                groupId = project.group.toString()
                artifactId = project.name + "-js"
                version = project.version.toString()
            }

        }

        project.apply(plugin = ("org.jetbrains.kotlin.multiplatform"))
        project.tasks.get("build").dependsOn(packJsNpmToMaven)
        project.tasks.get("build").dependsOn(packJsNpmToTgz)
    }

    private fun willBeTypescript():Boolean {

      //  val property: Any? = (project as org.gradle.api.internal.project.DefaultProject).findProperty("kt2ts")
       // println(property)
        return true// project.hasProperty("kt2ts");
         }
}

apply<NpmToMavenPlugin>()
