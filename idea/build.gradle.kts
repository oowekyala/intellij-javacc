@file:Suppress("PropertyName", "LocalVariableName")


plugins {
    kotlin("jvm")
    id("java")
    id("org.jetbrains.intellij") version "0.4.8"
}

val PackageRoot = "/com/github/oowekyala/ijcc"

dependencies {
    implementation(kotlin("reflect")) // this could be avoided

    compile(project(":core"))
}


ext {
    // creates secret properties
    set("intellijPublishUsername", "")
    set("intellijPublishPassword", "")
    apply(from = "../secrets.properties")
}




tasks {

    // compresses the icons and replaces them in the copied resource directory
    // the icons in the source dir are "optimised for maintainability", which means
    // much bigger than needed
    // you need svgo on your path (npm install -g svgo)
    val compressIcons by creating(Exec::class.java) {
        dependsOn("processResources")

        val iconsDir = "${buildDir.absolutePath}/resources/main$PackageRoot/icons"

        commandLine(
            "svgo",
            "-f",
            iconsDir
        )
    }

    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }


    intellij {
        version = "2018.2.4"
        updateSinceUntilBuild = false
    }

    runIde {
        jvmArgs = listOf("-Xmx2G")
        setConfigDirectory(rootProject.projectDir.resolve("sandbox").resolve("config"))
    }

    buildPlugin {
        dependsOn(compressIcons)
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
