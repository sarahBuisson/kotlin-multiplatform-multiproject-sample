public open class GithubPublishPlugin : Plugin<Project> {

    @javax.inject.Inject
    constructor() {
    }

    override fun apply(project: Project) {
        project.apply(plugin = ("maven-publish"))
        val publishingExtension = project.extensions["publishing"] as org.gradle.api.publish.internal.DefaultPublishingExtension;

        if (System.getenv("GITHUB_REPO_URL") != null)
            publishingExtension.repositories {
                maven {

                    println("repo github")
                    this.setUrl("https://maven.pkg.github.com/" + System.getenv("GITHUB_REPO_URL"))
                    this.credentials {
                        username = System.getenv("GITHUB_ACTOR")
                        password = System.getenv("GITHUB_TOKEN")
                    }
                }
            }
    }
}

project.apply<GithubPublishPlugin>()