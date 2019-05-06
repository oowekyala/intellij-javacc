@file:Suppress("PropertyName", "LocalVariableName")

import com.github.oowekyala.includeIjCoreDeps
import com.github.oowekyala.includeJars
import com.github.oowekyala.intellijCoreDep
import com.github.oowekyala.intellijDep
import org.jetbrains.grammarkit.tasks.GenerateLexer
import org.jetbrains.grammarkit.tasks.GenerateParser

plugins {
    kotlin("jvm")
    id("java")
    id("org.jetbrains.grammarkit") version "2018.2.2"
    id("com.github.johnrengelman.shadow") version "5.0.0"
}

val PackageRoot = "/com/github/oowekyala/ijcc"
val PathToPsiRoot = "$PackageRoot/lang/psi"
val lightPsiJarPath = "${project.buildDir}/libs/idea-skinny.jar"

val grammarKit by configurations.creating


dependencies {
    compile("org.apache.commons:commons-lang3:3.9") // only used to unescape java I think
    compile("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.1")

    // FIXME this is for GeneratedParserUtilBase
    runtimeOnly("com.github.JetBrains:Grammar-Kit:2017.1.6")

    compile(intellijCoreDep()) { includeJars("intellij-core") }
    compile(intellijDep()) {
        includeIjCoreDeps(rootProject)
        includeJars("platform-api") // FIXME this is just for GeneratedParserUtilBase
        // FIXME PsiClassOwnerNotResolved!!
        // includeJars("java-impl", "java-api", "openapi")
    }

    grammarKit(intellijDep()) {
        // Dependencies for the grammar-kit plugin
        // https://github.com/JetBrains/Grammar-Kit/blob/master/resources/META-INF/MANIFEST.MF
        includeJars(
            "annotations", "asm-all", "automaton", "extensions",
            "guava", "idea", "jdom", "picocontainer", "platform-api",
            "platform-impl", "trove4j", "util", rootProject = rootProject
        )
    }

    // this is for jjtx
    compile("com.google.guava:guava:27.0.1-jre")
    compile("org.apache.velocity:velocity:1.6.2")

    implementation("com.google.code.gson:gson:2.8.5")
    implementation("com.github.oowekyala.treeutils:tree-printers:2.0.2")
    implementation("org.yaml:snakeyaml:1.24")
    implementation("com.google.googlejavaformat:google-java-format:1.7")
    // implementation("com.tylerthrailkill.helpers:pretty-print:2.0.1")
    implementation("com.xenomachina:kotlin-argparser:2.0.7")


    testImplementation(grammarKit)
    testImplementation(intellijDep()) {
        includeJars("openapi", "idea_rt")
    }

    testCompile("junit:junit:4.12")
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

    shadowJar {
        baseName = "jjtricks-min"
        appendix = ""

        mergeServiceFiles()

        minimize {
            exclude(dependency("org.apache.velocity:velocity:.*"))
            exclude(dependency("org.jetbrains.kotlin:.*:.*"))
            exclude(dependency("com.google.googlejavaformat:.*:.*"))
            exclude(dependency("com.google.guava:.*:.*"))
            exclude(dependency("com.google.guava:.*:.*"))
            exclude(dependency("com.google.errorprone:javac-shaded:.*"))
        }

        manifest {
            attributes(
                "Main-Class" to "com.github.oowekyala.jjtx.Jjtricks"
            )
        }
    }
}
