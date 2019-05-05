package com.github.oowekyala

import org.gradle.api.Project
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.artifacts.repositories.IvyArtifactRepository
import org.gradle.kotlin.dsl.extra
import java.io.File


fun Project.localDepsRepo() = File("${rootProject.rootDir.absoluteFile}/dependencies/repo")

fun RepositoryHandler.localDepsRepo(project: Project): IvyArtifactRepository = ivy {
    val baseDir = project.localDepsRepo()
    setUrl(baseDir)

    ivyPattern("${baseDir.canonicalPath}/[organisation]/[module]/[revision]/[module].ivy.xml")
    ivyPattern("${baseDir.canonicalPath}/[organisation]/[module]/[revision]/ivy/[module].ivy.xml")
    ivyPattern("${baseDir.canonicalPath}/[organisation]/ideaIC/[revision]/ivy/[module].ivy.xml") // bundled plugins

    artifactPattern("${baseDir.canonicalPath}/[organisation]/[module]/[revision]/artifacts/lib/[artifact](-[classifier]).[ext]")
    artifactPattern("${baseDir.canonicalPath}/[organisation]/[module]/[revision]/artifacts/[artifact](-[classifier]).[ext]")
    artifactPattern("${baseDir.canonicalPath}/[organisation]/ideaIC/[revision]/artifacts/plugins/[module]/lib/[artifact](-[classifier]).[ext]") // bundled plugins
    artifactPattern("${baseDir.canonicalPath}/[organisation]/sources/[artifact]-[revision](-[classifier]).[ext]")
    artifactPattern("${baseDir.canonicalPath}/[organisation]/[module]/[revision]/[artifact](-[classifier]).[ext]")

    metadataSources {
        ivyDescriptor()
    }
}

fun Project.intellijDep() = "ijcc.build:ideaIC:${rootProject.extra["versions.intellijSdk"]}"

fun Project.intellijCoreDep() = "ijcc.build:intellij-core:${rootProject.extra["versions.intellijSdk"]}"

fun ModuleDependency.includeIjCoreDeps(project: Project) =
    includeJars(
        *(project.rootProject.extra["IntellijCoreDependencies"] as List<String>).toTypedArray(),
        rootProject = project.rootProject
    )


fun ModuleDependency.includeJars(vararg names: String, rootProject: Project? = null) {
    names.forEach {
        var baseName = it.removeSuffix(".jar")
        if (rootProject != null && rootProject.extra.has("ignore.jar.$baseName")) {
            return@forEach
        }
        if (rootProject != null && rootProject.extra.has("versions.jar.$baseName")) {
            baseName += "-${rootProject.extra["versions.jar.$baseName"]}"
        }
        artifact {
            name = baseName
            type = "jar"
            extension = "jar"
        }
    }
}
