@file:Suppress("PropertyName")

import org.gradle.api.publish.ivy.internal.artifact.FileBasedIvyArtifact
import org.gradle.api.publish.ivy.internal.publication.DefaultIvyConfiguration
import org.gradle.api.publish.ivy.internal.publication.DefaultIvyPublicationIdentity
import org.gradle.api.publish.ivy.internal.publisher.IvyDescriptorFileGenerator

val cacheRedirectorEnabled = findProperty("cacheRedirectorEnabled")?.toString()?.toBoolean() == true
val verifyDependencyOutput: Boolean by rootProject.extra
val intellijReleaseType: String by rootProject.extra
val intellijVersion = rootProject.extra["versions.intellijSdk"] as String
val asmVersion = rootProject.findProperty("versions.jar.asm-all") as String?


val intellijVersionDelimiterIndex = intellijVersion.indexOfAny(charArrayOf('.', '-'))
if (intellijVersionDelimiterIndex == -1) {
    error("Invalid IDEA version $intellijVersion")
}

val platformBaseVersion = intellijVersion.substring(0, intellijVersionDelimiterIndex)

logger.info("verifyDependencyOutput: $verifyDependencyOutput")
logger.info("intellijVersion: $intellijVersion")

repositories {

    if (cacheRedirectorEnabled) {
        maven("https://cache-redirector.jetbrains.com/www.jetbrains.com/intellij-repository/$intellijReleaseType")
        maven("https://cache-redirector.jetbrains.com/plugins.jetbrains.com/maven")
        maven("https://cache-redirector.jetbrains.com/jetbrains.bintray.com/intellij-third-party-dependencies/")
    }

    maven("https://www.jetbrains.com/intellij-repository/$intellijReleaseType")
    maven("https://plugins.jetbrains.com/maven")
    maven("https://jetbrains.bintray.com/intellij-third-party-dependencies/")
}

val intellij by configurations.creating
val sources by configurations.creating
val jpsStandalone by configurations.creating
val intellijCore by configurations.creating

/**
 * Special repository for annotations.jar required for idea runtime only.
 *
 * See IntellijDependenciesKt.intellijRuntimeAnnotations for more details.
 */
val intellijRuntimeAnnotations = "intellij-runtime-annotations"

val customDepsRepoDir = rootProject.rootDir.resolve("dependencies/repo")
val customDepsOrg: String by rootProject.extra
val customDepsRevision = intellijVersion
val repoDir = File(customDepsRepoDir, customDepsOrg)


dependencies {

    if (asmVersion != null) {
        sources("org.jetbrains.intellij.deps:asm-all:$asmVersion:sources@jar")
    }

    intellij("com.jetbrains.intellij.idea:ideaIC:$intellijVersion")
    sources("com.jetbrains.intellij.idea:ideaIC:$intellijVersion:sources@jar")
    jpsStandalone("com.jetbrains.intellij.idea:jps-standalone:$intellijVersion")
    intellijCore("com.jetbrains.intellij.idea:intellij-core:$intellijVersion")

}


val makeIntellijCore = buildIvyRepositoryTask(intellijCore, customDepsOrg, customDepsRepoDir)

val makeIntellijAnnotations by tasks.creating(Copy::class.java) {
    dependsOn(makeIntellijCore)

    from(repoDir.resolve("intellij-core/$intellijVersion/artifacts/annotations.jar"))

    val targetDir = File(repoDir, "$intellijRuntimeAnnotations/$intellijVersion")
    into(targetDir)

    val ivyFile = File(targetDir, "$intellijRuntimeAnnotations.ivy.xml")
    outputs.files(ivyFile)

    doLast {
        writeIvyXml(
            customDepsOrg,
            intellijRuntimeAnnotations,
            intellijVersion,
            intellijRuntimeAnnotations,
            targetDir,
            targetDir,
            targetDir,
            allowAnnotations = true
        )
    }
}

val mergeSources by tasks.creating(Jar::class.java) {
    dependsOn(sources)
    from(provider { sources.map(::zipTree) })
    destinationDir = File(repoDir, sources.name)
    baseName = "intellij"
    classifier = "sources"
    version = intellijVersion
}

val sourcesFile = mergeSources.outputs.files.singleFile

val makeIde = run {
    val task = buildIvyRepositoryTask(intellij, customDepsOrg, customDepsRepoDir, null, sourcesFile)

    task.configure {
        dependsOn(mergeSources)
    }

    task
}

val installLocalDeps: Task by tasks.creating {
    dependsOn(
        makeIntellijCore,
        makeIde,
        buildIvyRepositoryTask(jpsStandalone, customDepsOrg, customDepsRepoDir, null, sourcesFile),
        makeIntellijAnnotations
    )
}

// Task to delete legacy repo locations
tasks.create("cleanLegacy", Delete::class.java) {
    delete("$projectDir/intellij-sdk")
}

tasks.create("cleanDeps", Delete::class.java) {
    delete(customDepsRepoDir)
}

fun buildIvyRepositoryTask(
    configuration: Configuration,
    organization: String,
    repoDirectory: File,
    pathRemap: ((String) -> String)? = null,
    sources: File? = null
) = tasks.register("buildIvyRepositoryFor${configuration.name.capitalize()}") {

    fun ResolvedArtifact.moduleDirectory(): File =
        File(repoDirectory, "$organization/${moduleVersion.id.name}/${moduleVersion.id.version}")

    dependsOn(configuration)
    inputs.files(configuration)

    if (verifyDependencyOutput) {
        outputs.dir(provider {
            configuration.resolvedConfiguration.resolvedArtifacts.single().moduleDirectory()
        })
    } else {
        outputs.upToDateWhen {
            configuration.resolvedConfiguration.resolvedArtifacts.single()
                .moduleDirectory()
                .exists()
        }
    }

    doFirst {
        configuration.resolvedConfiguration.resolvedArtifacts.single().run {
            val moduleDirectory = moduleDirectory()
            val artifactsDirectory = File(moduleDirectory(), "artifacts")

            logger.info("Unpacking ${file.name} into ${artifactsDirectory.absolutePath}")
            copy {
                val fileTree = when (extension) {
                    "tar.gz" -> tarTree(file)
                    "zip"    -> zipTree(file)
                    else     -> error("Unsupported artifact extension: $extension")
                }

                from(fileTree.matching {
                    exclude("**/plugins/Kotlin/**")
                })

                into(artifactsDirectory)

                if (pathRemap != null) {
                    eachFile {
                        path = pathRemap(path)
                    }
                }

                includeEmptyDirs = false
            }

            writeIvyXml(
                organization,
                moduleVersion.id.name,
                moduleVersion.id.version,
                moduleVersion.id.name,
                File(artifactsDirectory, "lib"),
                File(artifactsDirectory, "lib"),
                File(moduleDirectory, "ivy"),
                *listOfNotNull(sources).toTypedArray()
            )

            val pluginsDirectory = File(artifactsDirectory, "plugins")
            if (pluginsDirectory.exists()) {
                file(File(artifactsDirectory, "plugins"))
                    .listFiles { file: File -> file.isDirectory }
                    .forEach {
                        writeIvyXml(
                            organization,
                            it.name,
                            moduleVersion.id.version,
                            it.name,
                            File(it, "lib"),
                            File(it, "lib"),
                            File(moduleDirectory, "ivy"),
                            *listOfNotNull(sources).toTypedArray()
                        )
                    }
            }
        }
    }
}

fun writeIvyXml(
    organization: String,
    moduleName: String,
    version: String,
    fileName: String,
    baseDir: File,
    artifactDir: File,
    targetDir: File,
    vararg sourcesJar: File,
    allowAnnotations: Boolean = false
) {
    fun shouldIncludeIntellijJar(jar: File) =
        jar.isFile
            && jar.extension == "jar"
            && !jar.name.startsWith("kotlin-")
            && (allowAnnotations || jar.name != "annotations.jar") // see comments for [intellijAnnotations] above

    with(IvyDescriptorFileGenerator(DefaultIvyPublicationIdentity(organization, moduleName, version))) {
        addConfiguration(DefaultIvyConfiguration("default"))
        addConfiguration(DefaultIvyConfiguration("sources"))
        artifactDir.listFiles()?.forEach { jarFile ->
            if (shouldIncludeIntellijJar(jarFile)) {
                val relativeName = jarFile.toRelativeString(baseDir).removeSuffix(".jar")
                addArtifact(
                    FileBasedIvyArtifact(
                        jarFile,
                        DefaultIvyPublicationIdentity(organization, relativeName, version)
                    ).also {
                        it.conf = "default"
                    }
                )
            }
        }

        sourcesJar.forEach {
            val sourcesArtifactName = it.name.substringBeforeLast("-").substringBeforeLast("-")
            addArtifact(
                FileBasedIvyArtifact(
                    it,
                    DefaultIvyPublicationIdentity(organization, sourcesArtifactName, version)
                ).also { artifact ->
                    artifact.conf = "sources"
                    artifact.classifier = "sources"
                }
            )
        }

        writeTo(File(targetDir, "$fileName.ivy.xml"))
    }
}

fun skipToplevelDirectory(path: String) = path.substringAfter('/')

fun skipContentsDirectory(path: String) = path.substringAfter("Contents/")


