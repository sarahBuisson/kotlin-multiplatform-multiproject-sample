project.apply(plugin = "jacoco")

project.apply { this.plugin("jacoco") }

tasks.register("jacocoTestReport", JacocoReport::class) {
    executionData.setFrom(fileTree(project.rootDir.absolutePath).include("**/build/jacoco/*.exec"))
    classDirectories.setFrom(project.fileTree("${buildDir}/classes/kotlin"))

    sourceDirectories.setFrom(files(project.fileTree("${projectDir}/src/commonMain/kotlin"),
            project.fileTree("${projectDir}/src/main/kotlin"),
            project.fileTree("${projectDir}/src/main/java")))

    reports {
        this.csv.isEnabled = true
        csv.destination = file("$buildDir/reports/jacocoCsv/report.csv")
        xml.isEnabled = true
        html.isEnabled = true
        xml.destination = file("$buildDir/reports/jacocoXml/report.xml")
        html.destination = file("$buildDir/reports/jacoco/report.html")
    }
}