fun addTaskNpmToMaven(project: Project) {

    val buildPackageJsonForMaven = tasks.register<DefaultTask>("buildPackageJsonForMaven") {

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
            //commonMainImplementation
            //org.jetbrains.kotlin.gradle.plugin.KotlinMultiplatformPluginWrapper
            val plugin = project.plugins.find { it.javaClass.name.contains("KotlinMultiplatform") }
            val kotlinVersionPlugin = plugin?.javaClass?.getMethod("getKotlinPluginVersion")?.invoke(plugin)
            // val kotlinVersion =  project.configurations.get("jsMainImplementation").dependencies.find { it.name.contains("kotlin") }?.version
            if (kotlinVersionPlugin == null) throw Exception("need   \"apply plugin: 'org.jetbrains.kotlin.multiplatform'\"")
            (packageJsonData.get("dependencies") as MutableMap<String, Any>).put("kotlin", kotlinVersionPlugin)

            project.plugins
            val packageJson = project.file("${project.buildDir}/tmp/package.json")
            println(packageJson.getParentFile())
            if (!packageJson.getParentFile().exists())
                packageJson.getParentFile().mkdirs()
            packageJson.createNewFile()
            println(packageJson)
            packageJson.writeText(groovy.json.JsonBuilder(packageJsonData).toPrettyString())
            println(packageJson.exists())
            println(packageJson.absoluteFile)
            println(packageJson.getParentFile().listFiles().get(0).absoluteFile)
        }
    }
    this.tasks.register("unpackJsNpm", Copy::class) {

        println("register unpackJsNpm in ${this.project.name}")
        val jarJsPath = "$buildDir/libs/${project.name}-js-${project.version}.jar"
        this.from(zipTree(jarJsPath))
        this.into("$buildDir/jsNpmToMaven")
        this.dependsOn("jsJar")
    }
    val movePackageJson = this.tasks.register("movePackageJson", Copy::class) {

        this.from("$buildDir/tmp/package.json")
        this.into("$buildDir/jsNpmToMaven")
        this.dependsOn(buildPackageJsonForMaven)
    }

    val packJsNpmToMaven by this.project.tasks.registering(Zip::class) {
        println("register packJsNpmToMaven")
        from("$buildDir/jsNpmToMaven")
        this.archiveFileName.set("${project.name}-js-${project.version}.jar")
        this.destinationDirectory.set(file("$buildDir/npm"))
        dependsOn("unpackJsNpm")
        dependsOn(movePackageJson)
        println(this.project.tasks.names)

    }

    apply(plugin = ("maven-publish"))
    (this.project.extensions["publishing"] as org.gradle.api.publish.internal.DefaultPublishingExtension
            ).publications {


        //gradle publishMavenNpmPublicationToMavenLocal
        create<MavenPublication>("mavenNpm") {
            artifactId = project.name + "-npm"
            artifact(file("$buildDir/npm/${project.name}-js-${project.version}.jar"))
            //  from(components["npm"])
            afterEvaluate {
                artifactId = project.name + "-npm"
            }

        }
    }
    tasks.getByPath("publishMavenNpmPublicationToMavenLocal").dependsOn(packJsNpmToMaven)

}
addTaskNpmToMaven(this)