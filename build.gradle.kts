@file:Suppress("PropertyName", "LocalVariableName")


plugins {
    id("org.jetbrains.intellij") version "1.14.1"
    java
    id("org.jetbrains.grammarkit") version "2022.3.1"
    kotlin("jvm") version "1.8.22" // sync with version below
}

group = "com.github.oowekyala"
version = "1.11"

val IntellijVersion = "231.9011.34" // note: "since" version should be updated manually in plugin.xml
val KotlinVersion = "1.8.22"
val JvmTarget = "17"
val PackageRoot = "/com/github/oowekyala/ijcc"
val PathToPsiRoot = "$PackageRoot/lang/psi"


repositories {
    mavenCentral()
 //   jcenter()

    maven("https://www.jetbrains.com/intellij-repository/releases")
//    maven("https://cache-redirector.jetbrains.com/repo.maven.apache.org/maven2")
    maven("https://cache-redirector.jetbrains.com/intellij-dependencies")

/*    maven {
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
*/
}



ext {
    // creates secret properties
    set("intellijPublishToken", "")
    if (projectDir.resolve("secrets.properties").exists())
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
    implementation("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.3.5")
    implementation(kotlin("reflect")) // this could be avoided

    // this is for tests
    testImplementation("com.github.oowekyala.treeutils:tree-matchers:2.0.2")
    testImplementation("io.kotlintest:kotlintest-runner-junit5:3.1.11")

    testImplementation(kotlin("test"))
    testImplementation("junit:junit:4.12") // todo upgrade

    constraints {
        testImplementation("org.jetbrains.kotlin:kotlin-stdlib:$KotlinVersion")
        testImplementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:$KotlinVersion")
        testImplementation("org.jetbrains.kotlin:kotlin-reflect:$KotlinVersion")
    }
}

tasks {

    val GenerationTaskGroup = "Code generation"

     generateParser {
        group = GenerationTaskGroup
        description = "Generate the parser and PSI hierarchy"
        sourceFile.set(file("src/main/grammars/JavaCC.bnf"))
        targetRoot.set("$buildDir/gen")
        pathToParser.set("$PackageRoot/lang/parser/JavaccParser.java")
        pathToPsiRoot.set(PathToPsiRoot)
        purgeOldFiles.set(true)


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


     generateLexer  {
        group = GenerationTaskGroup
        description = "Generate the JFlex lexer used by the parser"

        sourceFile.set(file("src/main/grammars/JavaCC.flex"))
        targetDir.set("$buildDir/gen/com/github/oowekyala/ijcc/lang/lexer")
        targetClass.set("JavaccLexer")
        purgeOldFiles.set(true)
    }


    compileJava {
        dependsOn(generateParser, generateLexer)
        sourceCompatibility = JvmTarget
        targetCompatibility = JvmTarget
    }

    compileTestJava {
        sourceCompatibility = JvmTarget
        targetCompatibility = JvmTarget
    }

    compileKotlin {
        dependsOn(generateParser, generateLexer)
        kotlinOptions {
            freeCompilerArgs = listOf(
                "-Xjvm-default=all"
            )
            jvmTarget = JvmTarget
        }

    }

    compileTestKotlin {
        kotlinOptions.jvmTarget = JvmTarget
    }

    // See https://github.com/JetBrains/gradle-intellij-plugin/
    intellij {
        version.set(IntellijVersion)
        updateSinceUntilBuild.set(true)
        //ideaDependencyCachePath.set("deps")
        plugins.set(listOf("com.intellij.java"))
        // setPlugins("java")
    }

  /*  runIde {
        // this launches in the sandbox subdir
        jvmArgs = listOf("-Xmx2G")
        setConfigDirectory(rootProject.projectDir.resolve("sandbox").resolve("config"))
    }
*/
    buildPlugin {
        archiveVersion.set(project.version.toString())
        archiveBaseName.set("intellij-javacc")
    }

    publishPlugin {
        token.set(project.property("intellijPublishToken") as String)
    }

    patchPluginXml {
        // todo gradle changelog plugin

        val changelog = layout.files("changelog.html").singleFile.readText()

        changeNotes.set(changelog)

 //       version.set(project.version)
    }
}
