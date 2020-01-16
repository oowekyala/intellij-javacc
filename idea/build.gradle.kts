@file:Suppress("PropertyName", "LocalVariableName")

import com.github.oowekyala.*


plugins {
    kotlin("jvm")
    id("java")
    id("org.jetbrains.intellij") version "0.4.8"
}

val PackageRoot = "/com/github/oowekyala/ijcc"

version = "1.5"

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.1")
    implementation(kotlin("reflect")) // this could be avoided

    compile(project(":core")) {
        exclude(module = "Grammar-Kit")
    }
    testCompile(project(":core").dependencyProject.sourceSets["test"].output)
}


ext {
    // creates secret properties
    set("intellijPublishUsername", "")
    set("intellijPublishPassword", "")
    apply(from = "../secrets.properties")
}




tasks {


    compileKotlin {
        kotlinOptions {
            freeCompilerArgs = listOf(
                "-Xjvm-default=enable",
                "-Xuse-experimental=kotlin.Experimental"
            )
            jvmTarget = "1.8"
        }
    }

    // compresses the icons and replaces them in the copied resource directory
    // the icons in the source dir are "optimised for maintainability", which means
    // much bigger than needed
    // you need svgo on your path (npm install -g svgo)
    val compressIcons by creating(Exec::class.java) {
        dependsOn("processResources")

        commandLine(
            "svgo",
            "-f",
            ijccResource("/icons")
        )
    }

    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }


    intellij {
        version = "2019.3.1"
        updateSinceUntilBuild = false
        ideaDependencyCachePath = "${rootProject.path}/dependencies/repo/ijcc.build"
        setPlugins("java")
    }

    runIde {
        jvmArgs = listOf("-Xmx2G")
        setConfigDirectory(rootProject.projectDir.resolve("sandbox").resolve("config"))
    }

    buildPlugin {
        dependsOn(compressIcons)

        archiveVersion.set(project.version.toString())
        archiveBaseName.set("intellij-javacc")

    }

    publishPlugin {
        username(project.property("intellijPublishUsername"))
        password(project.property("intellijPublishPassword"))
    }

    patchPluginXml {

        val changelog = layout.files("changelog.html").singleFile.readText()

        changeNotes(changelog)

        version(project.version)
    }

}
