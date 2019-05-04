@file:Suppress("PropertyName", "LocalVariableName")

import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper
import java.net.URI

plugins {
    kotlin("jvm") version "1.3.10"
    java
}

val KotlinVersion by extra { "1.3.10" } // sync with above
val PackageRoot = "/com/github/oowekyala/ijcc"
val PathToPsiRoot = "$PackageRoot/lang/psi"
val lightPsiJarPath = "${project.buildDir}/libs/idea-skinny.jar"


group = "com.github.oowekyala"
version = "1.4"


ext { 
    // creates secret properties
    set("intellijPublishUsername", "")
    set("intellijPublishPassword", "")
    apply(from = "secrets.properties")
}

subprojects {


    apply<KotlinPluginWrapper>()

    repositories {
        mavenCentral()
        jcenter()
        maven {
            url = URI("https://dl.bintray.com/kotlin/kotlinx")
        }
        maven("https://jetbrains.bintray.com/intellij-third-party-dependencies")
    }


    dependencies {

//        val compile by configurations.creating {}
//        val testCompile by configurations.creating {}
//        val testImplementation by configurations.creating {}

        compile("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$KotlinVersion")
        compile("org.apache.commons:commons-lang3:3.9") // only used to unescape java I think

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


    tasks {
//
//
//        compileJava {
//            sourceCompatibility = "1.8"
//            targetCompatibility = "1.8"
//        }

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

