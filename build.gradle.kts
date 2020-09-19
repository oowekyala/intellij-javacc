@file:Suppress("PropertyName", "LocalVariableName")

import org.jetbrains.grammarkit.tasks.GenerateLexer
import org.jetbrains.grammarkit.tasks.GenerateParser
import java.net.URI

plugins {
    id("org.jetbrains.intellij") version "0.4.22"
    java
    id("org.jetbrains.grammarkit") version "2020.1"
    kotlin("jvm") version "1.4.10" // sync with version below
}

group = "com.github.oowekyala"
version = "1.6"

val IntellijVersion = "2020.1" // note: "since" version should be updated manually in plugin.xml
val KotlinVersion = "1.4.10"
val PackageRoot = "/com/github/oowekyala/ijcc"
val PathToPsiRoot = "$PackageRoot/lang/psi"


repositories {
    mavenCentral()
    jcenter()
    maven {
        url = URI("https://jetbrains.bintray.com/intellij-plugin-service")
    }
    maven {
        url = URI("https://dl.bintray.com/kotlin/kotlinx")
    }
    maven {
        url = URI("https://jitpack.io")
    }
    maven {
        url = URI("https://oss.sonatype.org/content/repositories/snapshots/")
    }
    maven("https://jetbrains.bintray.com/intellij-third-party-dependencies")

}



ext {
    // creates secret properties
    set("intellijPublishToken", "")
    apply(from = "secrets.properties")
}

sourceSets {
    main {
        java {
            srcDirs("$buildDir/gen")
            srcDirs("src/main/kotlin")
            srcDirs("src/main/java")
        }
    }
}


dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.1")
    implementation("org.jetbrains.kotlin:kotlin-reflect:$KotlinVersion") // this could be avoided

    compile("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$KotlinVersion")

    // this is for tests
    testCompile("com.github.oowekyala.treeutils:tree-matchers:2.0.2")
    testImplementation("io.kotlintest:kotlintest-runner-junit5:3.1.11")

    testImplementation(kotlin("test"))
    testImplementation("junit:junit:4.12")

    constraints {
        testImplementation("org.jetbrains.kotlin:kotlin-stdlib:$KotlinVersion")
        testImplementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:$KotlinVersion")
        testImplementation("org.jetbrains.kotlin:kotlin-reflect:$KotlinVersion")
    }
}

tasks {

    val GenerationTaskGroup = "Code generation"

    val generateParser by creating(GenerateParser::class) {
        group = GenerationTaskGroup
        description = "Generate the parser and PSI hierarchy"


        source = "src/main/grammars/JavaCC.bnf"
        targetRoot = "$buildDir/gen"
        pathToParser = "$PackageRoot/lang/parser/JavaccParser.java"
        pathToPsiRoot = PathToPsiRoot
        purgeOldFiles = true

        doLast {
            // Eliminate the duplicate PSI classes found in the generated and main source tree

            val deletedNames = listOf("JccRegularExpressionOwnerImpl.java")

            fun getPsiFiles(root: String): FileCollection {

                val psiInterfaces = "$root$PathToPsiRoot"
                val psiImpl = "$psiInterfaces/impl"

                return layout.files(file(psiInterfaces).listFiles()) + layout.files(file(psiImpl).listFiles())
            }


            val userPsiFiles = getPsiFiles("src/main/kotlin").map { it.nameWithoutExtension }

            val genPsiFileDups = getPsiFiles("$buildDir/gen").filter { genFile ->
                // in this source tree they're .java
                genFile.isFile && (userPsiFiles.any { it == genFile.nameWithoutExtension } || genFile.name in deletedNames)
            }
            logger.info("Detected ${genPsiFileDups.count()} generated PSI files overridden by sources in the main source tree:")
            genPsiFileDups.sorted().forEach { logger.info(it.name) }

            delete(genPsiFileDups)
            logger.info("Deleted.")
        }
    }


    val generateLexer by creating(GenerateLexer::class) {
        group = GenerationTaskGroup
        description = "Generate the JFlex lexer used by the parser"

        source = "src/main/grammars/JavaCC.flex"
        targetDir = "$buildDir/gen/com/github/oowekyala/ijcc/lang/lexer"
        targetClass = "JavaccLexer"
        purgeOldFiles = true
    }


    compileJava {
        dependsOn(generateParser, generateLexer)
        sourceCompatibility = "1.8"
        targetCompatibility = "1.8"
    }

    compileKotlin {
        dependsOn(generateParser, generateLexer)
        kotlinOptions {
            freeCompilerArgs = listOf(
                "-Xjvm-default=enable",
                "-Xuse-experimental=kotlin.Experimental"
            )
            jvmTarget = "1.8"
        }

    }

    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }

    // See https://github.com/JetBrains/gradle-intellij-plugin/
    intellij {
        version = IntellijVersion
        updateSinceUntilBuild = false
        ideaDependencyCachePath = "${rootProject.path}/dependencies/repo/ijcc.build"
        setPlugins("java")
    }

    runIde {
        // this launches in the sandbox subdir
        jvmArgs = listOf("-Xmx2G")
        setConfigDirectory(rootProject.projectDir.resolve("sandbox").resolve("config"))
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
            "$buildDir/resources/main$PackageRoot/icons"
        )
    }

    buildPlugin {
        dependsOn(compressIcons)

        archiveVersion.set(project.version.toString())
        archiveBaseName.set("intellij-javacc")

    }

    publishPlugin {
        token(project.property("intellijPublishToken"))
    }

    patchPluginXml {

        val changelog = layout.files("changelog.html").singleFile.readText()

        changeNotes(changelog)

        version(project.version)
    }
}
