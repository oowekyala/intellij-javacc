@file:Suppress("PropertyName", "LocalVariableName")

import com.google.common.io.Files
import org.jetbrains.grammarkit.tasks.GenerateLexer
import org.jetbrains.grammarkit.tasks.GenerateParser
import java.io.PrintStream
import java.net.URI

plugins {
    kotlin("jvm") version "1.3.10"
    id("java")
    id("org.jetbrains.intellij") version "0.4.8"
    id("org.jetbrains.grammarkit") version "2018.2.2"
    id("com.github.johnrengelman.shadow") version "5.0.0"
}

val KotlinVersion by extra { "1.3.10" } // sync with above
val PackageRoot = "/com/github/oowekyala/ijcc"
val PathToPsiRoot = "$PackageRoot/lang/psi"
val lightPsiJarPath = "${project.buildDir}/libs/light-psi-all.jar"


group = "com.github.oowekyala"
version = "1.4"


ext { 
    // creates secret properties
    set("intellijPublishUsername", "")
    set("intellijPublishPassword", "")
    apply(from = "secrets.properties")
}

allprojects {


    repositories {
        mavenCentral()
        jcenter()
        maven {
            url = URI("https://dl.bintray.com/kotlin/kotlinx")
        }
    }

}


dependencies {
    compile("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$KotlinVersion")
    compile("org.apache.commons:commons-lang3:3.9") // only used to unescape java I think
    implementation("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.1")
    implementation(kotlin("reflect")) // this could be avoided

    // this is for JJTX
    compile("com.google.guava:guava:23.5-jre")
    implementation("com.google.code.gson:gson:2.8.5")
    implementation("com.github.oowekyala.treeutils:tree-printers:2.0.2")
    compile("org.apache.velocity:velocity:1.6.2")
    implementation("org.yaml:snakeyaml:1.24")
    implementation("com.google.googlejavaformat:google-java-format:1.7")
    implementation("com.tylerthrailkill.helpers:pretty-print:2.0.1")
    implementation("com.xenomachina:kotlin-argparser:2.0.7")

    runtimeOnly(files(lightPsiJarPath))

    // this is for tests
    testCompile("com.github.oowekyala.treeutils:tree-matchers:2.0.2")
    testCompile("org.jetbrains.kotlin:kotlin-reflect:$KotlinVersion")
    testImplementation("io.kotlintest:kotlintest-runner-junit5:3.1.11")

    constraints {
        testImplementation("org.jetbrains.kotlin:kotlin-stdlib:$KotlinVersion")
        testImplementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:$KotlinVersion")
        testImplementation("org.jetbrains.kotlin:kotlin-reflect:$KotlinVersion")
    }

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


intellij {
    version = "2018.2.4"
    updateSinceUntilBuild = false
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

    compileJava {
        dependsOn(generateParser, generateLexer)
        sourceCompatibility = "1.8"
        targetCompatibility = "1.8"
    }

    compileKotlin {
        dependsOn(generateLexer, generateParser)

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

    runIde {
        jvmArgs = listOf("-Xmx2G")
        setConfigDirectory(projectDir.resolve("sandbox").resolve("config"))
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

    // This is for JJTricks

    val classLogFile = File("${project.buildDir}/tmp/minimiseIdea/all-classes.log")


    // idea is not in the runtime class path, only compile
    val fakeClassPath =
        sourceSets["main"].compileClasspath
            .plus(sourceSets["main"].output.classesDirs)
            .plus(files(sourceSets["main"].output.resourcesDir))


    val dumpClassLog by creating(JavaExec::class.java) {
        dependsOn(jar)

        main = "com.github.oowekyala.jjtx.JjtxLightPsi\$GenerateClassLog"
        jvmArgs = listOf("-verbose:class")

        classpath = fakeClassPath

        workingDir = File("${project.projectDir}/sandbox")

        Files.createParentDirs(classLogFile)

        standardOutput = PrintStream(classLogFile.outputStream().buffered())

        doLast {
            standardOutput.flush()
        }
    }

    val minimiseIdea by creating(JavaExec::class.java) {
        dependsOn(dumpClassLog)

        main = "com.github.oowekyala.jjtx.JjtxLightPsi"
        classpath = fakeClassPath

        args = listOf("${project.buildDir}/libs", classLogFile.toString())
    }

    shadowJar {
        dependsOn(minimiseIdea)

        baseName = "jjtricks-shadow"

        mergeServiceFiles {}
        manifest {
            attributes(
                "MainClass" to "com.github.oowekyala.jjtx.Jjtricks"
            )
        }
    }

}
