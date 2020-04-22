@file:Suppress("PropertyName", "LocalVariableName")

import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper
import java.net.URI
import com.github.oowekyala.localDepsRepo
import com.github.oowekyala.includeJars
import com.github.oowekyala.intellijCoreDep
import com.github.oowekyala.includeIjCoreDeps
import com.github.oowekyala.intellijDep

/*

Setup:

 ./gradlew :prepare-deps:installLocalDeps



 */

plugins {
    kotlin("jvm") version "1.3.11" // sync with extra property below
    java
}

group = "com.github.oowekyala"
version = "1.5"

extra["customDepsOrg"] = "ijcc.build"

extra["versions.kotlin"] = "1.3.11" // sync with above plugin version
extra["versions.intellijSdk"] = "2019.3.1"

extra["versions.jar.asm-all"] = "7.0.1"
extra["versions.jar.guava"] = "27.1-jre"
extra["versions.jar.picocontainer"] = "1.2"
extra["versions.jar.automaton"] = "1.12-1"
extra["versions.jar.streamex"] = "0.6.8"

extra["verifyDependencyOutput"] = false
extra["intellijReleaseType"] =
    if (extra["versions.intellijSdk"]?.toString()?.endsWith("SNAPSHOT") == true)
        "snapshots"
    else
        "releases"

extra["IntellijCoreDependencies"] =
    listOf(
        "asm-all", // above 2019.1 the jar is versioned 7.0.1
        "guava",
        "jdom",
        "jna",
        "log4j",
        "picocontainer",
//        "snappy-in-java",
        "streamex",
        "trove4j"
    )


val KotlinVersion = extra["versions.kotlin"]
val PackageRoot = "/com/github/oowekyala/ijcc"
val PathToPsiRoot = "$PackageRoot/lang/psi"
val lightPsiJarPath = "${project.buildDir}/libs/idea-skinny.jar"


repositories {
    jcenter()
    //    mavenCentral()
    localDepsRepo(project)
    maven {
        url = URI("https://dl.bintray.com/kotlin/kotlinx")
    }
    maven("https://jetbrains.bintray.com/intellij-third-party-dependencies")
}


subprojects {

    val sub = this@subprojects

    sub.apply<KotlinPluginWrapper>()


    sub.repositories {
        jcenter()
        //    mavenCentral()
        localDepsRepo(project)
        maven {
            url = URI("https://dl.bintray.com/kotlin/kotlinx")
        }
        maven {
            url = URI("https://jitpack.io")
        }
        maven("https://jetbrains.bintray.com/intellij-third-party-dependencies")
    }

    sub.dependencies {

        compile("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$KotlinVersion")

        // this is for tests
        testCompile("com.github.oowekyala.treeutils:tree-matchers:2.0.2")
        testCompile("org.jetbrains.kotlin:kotlin-reflect:$KotlinVersion")
        testImplementation("io.kotlintest:kotlintest-runner-junit5:3.1.11")

        testImplementation(intellijCoreDep()) {
            includeJars("intellij-core")
        }

        testImplementation(kotlin("test"))
        testImplementation("junit:junit:4.12")
        testImplementation(intellijDep()) {
            includeIjCoreDeps(project)
            includeJars(
                "openapi", "bootstrap", "idea_rt",
                "annotations", "asm-all", "automaton", "extensions",
                "guava", "idea", "jdom", "picocontainer", "platform-api",
                "platform-impl", "trove4j", "util", rootProject = rootProject
            )
        }

        constraints {
            testImplementation("org.jetbrains.kotlin:kotlin-stdlib:$KotlinVersion")
            testImplementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:$KotlinVersion")
            testImplementation("org.jetbrains.kotlin:kotlin-reflect:$KotlinVersion")
        }
    }


    sub.tasks {


        compileJava {
            sourceCompatibility = "1.8"
            targetCompatibility = "1.8"
        }

        compileKotlin {
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
    }

}

