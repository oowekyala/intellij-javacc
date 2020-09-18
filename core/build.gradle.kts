@file:Suppress("PropertyName", "LocalVariableName")

import com.github.oowekyala.*
import org.jetbrains.grammarkit.tasks.GenerateLexer
import org.jetbrains.grammarkit.tasks.GenerateParser

plugins {
    kotlin("jvm")
    id("java")
    id("org.jetbrains.grammarkit") version "2020.1"
    id("com.github.johnrengelman.shadow") version "5.0.0"
}


val grammarKit: Configuration by configurations.creating


dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.1")

    compileOnly(intellijCoreDep()) { includeJars("intellij-core") }
    compileOnly(intellijDep()) {
        includeIjCoreDeps(rootProject)
        includeJars(
            "platform-api",
            "platform-impl",
            "platform-util-ui",
            "platform-util-ex",
            "idea",
            rootProject = rootProject
        )
    }

    /*
    lib/annotations-19.0.0.jar
    lib/asm-all-7.0.1.jar
    lib/automaton-1.12-1.jar
    lib/extensions.jar
    lib/guava-28.2-jre.jar
    lib/idea.jar
    lib/platform-api.jar
    lib/platform-concurrency.jar
    lib/platform-impl.jar
    lib/util.jar
    lib/trove4j.jar
    lib/jdom.jar
     */

    grammarKit(intellijDep()) {
        // Dependencies for the grammar-kit plugin
        // https://github.com/JetBrains/Grammar-Kit/blob/master/resources/META-INF/MANIFEST.MF
        includeJars(
            "annotations",
            "asm-all",
            "automaton",
            "extensions",
            "guava",
            "idea",
            "jdom",
            "platform-api",
            "platform-impl",
            "platform-concurrency",
            "trove4j",
            "util",
            "testFramework",
            rootProject = rootProject
        )
    }

    testImplementation(grammarKit)

    sourceSets["test"].compileClasspath += configurations.compileOnly
    sourceSets["test"].runtimeClasspath += configurations.compileOnly
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



tasks {

    val GenerationTaskGroup = "Code generation"

    val generateParser by creating(GenerateParser::class) {
        group = GenerationTaskGroup
        description = "Generate the parser and PSI hierarchy"

        val PathToPsiRoot = "$IjccPackage/lang/psi"

        source = "src/main/grammars/JavaCC.bnf"
        targetRoot = "$buildDir/gen"
        pathToParser = "/com/github/oowekyala/ijcc/lang/parser/JavaccParser.java"
        pathToPsiRoot = PathToPsiRoot
        purgeOldFiles = true

        classpath(grammarKit)

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


    test {
        systemProperties(
            "idea.home.path" to ideaBin(),
            "idea.home" to ideaBin()
        )
    }
}
