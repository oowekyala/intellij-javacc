@file:Suppress("PropertyName", "LocalVariableName")

import org.jetbrains.grammarkit.tasks.GenerateLexer
import org.jetbrains.grammarkit.tasks.GenerateParser
import java.net.URI

plugins {
    kotlin("jvm") version "1.3.10"
    id("java")
    id("org.jetbrains.intellij") version "0.3.12"
    id("org.jetbrains.grammarkit") version "2018.2.2"
}

val KotlinVersion by extra { "1.3.10" } // sync with above
val PackageRoot = "/com/github/oowekyala/ijcc"
val PathToPsiRoot = "$PackageRoot/lang/psi"


group = "com.github.oowekyala"
version = "1.1"


ext {
    // creates secret properties
    set("intellijPublishUsername", "")
    set("intellijPublishPassword", "")
    apply(from = "secrets.properties")
}



repositories {
    mavenCentral()
    jcenter()
    maven {
        url = URI("http://dl.bintray.com/kotlin/kotlinx")
    }
}


dependencies {
    compile("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$KotlinVersion")
    compile("org.apache.commons:commons-lang3:3.1") // only used to unescape java I think
    compile("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.1")
    // https://mvnrepository.com/artifact/net.java.dev.javacc/javacc
    testCompile("com.github.oowekyala.treeutils:tree-matchers:2.0.1")
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
        targetRoot = "${buildDir}/gen"
        pathToParser = "/com/github/oowekyala/ijcc/lang/parser/JavaccParser.java"
        pathToPsiRoot = PathToPsiRoot
        purgeOldFiles = true
    }


    val generateLexer by creating(GenerateLexer::class) {
        group = GenerationTaskGroup
        description = "Generate the JFlex lexer used by the parser"

        source = "src/main/grammars/JavaCC.flex"
        targetDir = "${buildDir}/gen/com/github/oowekyala/ijcc/lang/lexer"
        targetClass = "JavaccLexer"
        purgeOldFiles = true
    }


    val overrideDefaultPsi: Task by creating {
        dependsOn(generateParser)

        group = GenerationTaskGroup
        description = "Eliminate the duplicate PSI classes found in the generated and main source tree"

        doLast {

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

    compileJava {
        dependsOn(overrideDefaultPsi, generateLexer)
        sourceCompatibility = "1.8"
        targetCompatibility = "1.8"
    }

    compileKotlin {
        dependsOn(generateLexer, overrideDefaultPsi)

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

    publishPlugin {
        username(project.property("intellijPublishUsername"))
        password(project.property("intellijPublishPassword"))
    }

    patchPluginXml {
        //language=HTML
        changeNotes(
            """
                Main topic: improved support for the lexer generator.
                <p>What's new:
                <ul>
                    <li>Completion of string literals with known string tokens!</li>
                    <li>Tokens declared in BNF expansions are now in the structure view (even the implicit ones), and documented</li>
                    <li>100% coverage of JavaCC semantic errors</li>
                    <li>Left-recursive production detection, and regex loop detection</li>
                    <li>Usage of string tokens that would be matched as another token now trigger a warning.
                    "Token can never be matched" inspection has been toned down because it triggered false positives.
                    </li>
                    <li><b>Java injection support has been downgraded to experimental status.</b> The
                    current implementation is very resource-intensive, and it has been observed to cause
                    significant delays in e.g. inspection highlight refresh. That undermines the responsiveness
                    and usefulness of many other features so it's now disabled by default. You can still use
                    it of course by setting the injection level in the plugin settings (Settings | Languages & Frameworks | JavaCC).
                    </li>
                </ul>

                <p>What's fixed:
                <ul>
                    <li>Match JavaCC's errors more closely, in particular with string token definitions, which it treats very specially.</li>
                    <li>Countless false positives of "token name has not been defined"</li>
                </ul>
            """.trimIndent()
        )

        version(project.version)
    }

}
