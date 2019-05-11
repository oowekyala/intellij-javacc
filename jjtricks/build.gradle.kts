import com.github.oowekyala.*


plugins {
    kotlin("jvm")
    id("java")
    // Applying the grammarkit plugin allows resolving the grammarkit dependency
    // bequested by the :core project... Idk there may be some repo stuff going
    // on
    id("org.jetbrains.grammarkit") version "2018.2.2"
    id("com.github.johnrengelman.shadow") version "5.0.0"
}

group = "com.github.oowekyala"
version = "1.0"

val runtime by configurations
val compileOnly by configurations


dependencies {
    api(project(":core"))

    val ijdeps by configurations.creating

    ijdeps(intellijCoreDep()) { includeJars("intellij-core") }
    ijdeps(intellijDep()) {
        includeIjCoreDeps(rootProject)
        includeJars("platform-api")
    }

    // trick Idea into putting those on the classpath when
    // running app inside IDE
    compileOnly(ijdeps)
    runtimeOnly(ijdeps)

    api("com.google.guava:guava:27.0.1-jre")
    api("org.apache.velocity:velocity:1.6.2")

    implementation("com.google.code.gson:gson:2.8.5")
    implementation("com.github.oowekyala.treeutils:tree-printers:2.1.0")
    implementation("org.yaml:snakeyaml:1.24")
    implementation("com.google.googlejavaformat:google-java-format:1.7")
    // for debugging only, this pulls in a huge IBM dependency
    // implementation("com.tylerthrailkill.helpers:pretty-print:2.0.2")
    implementation("com.xenomachina:kotlin-argparser:2.0.7")

    testImplementation(project(":core").dependencyProject.sourceSets["test"].output)
    testImplementation("commons-io:commons-io:2.6")
}

sourceSets {
    main {
        java {
            srcDirs("src/main/kotlin")
            srcDirs("src/main/java")
        }
    }
}

tasks {

    compileJava {}

    compileKotlin {
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
            "jjtx.testEnv.jjtricks.testResDir" to "$projectDir/src/test/resources",
            "idea.home.path" to ideaBin()
        )
    }

    shadowJar {
        archiveBaseName.set("jjtricks")
        archiveAppendix.set("")
        archiveClassifier.set("standalone")

        configurations.add(compileOnly)

        mergeServiceFiles()

        minimize {
            exclude(dependency("org.apache.velocity:velocity:.*"))
            exclude(dependency("org.jetbrains.kotlin:.*:.*"))
            exclude(dependency("com.google.googlejavaformat:.*:.*"))
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
