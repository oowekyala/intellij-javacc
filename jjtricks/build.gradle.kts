import com.github.oowekyala.*


plugins {
    kotlin("jvm")
    id("java")
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
        includeJars("platform-api", "platform-impl", rootProject = rootProject)
    }

    // trick Idea into putting those on the classpath when
    // running app inside IDE
    compileOnly(ijdeps)
    runtimeOnly(ijdeps)

    api("com.google.guava:guava:27.0.1-jre")
    api("org.apache.velocity:velocity:1.6.2")

    implementation("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.1")
    implementation("com.google.code.gson:gson:2.8.5")
    implementation("com.github.everit-org.json-schema:org.everit.json.schema:1.11.1")
    implementation("com.github.oowekyala.treeutils:tree-printers:2.1.0")
    implementation("org.yaml:snakeyaml:1.24")
    implementation("com.google.googlejavaformat:google-java-format:1.7")
    // for debugging only, this pulls in a huge IBM dependency
    // implementation("com.tylerthrailkill.helpers:pretty-print:2.0.2")
    implementation("com.xenomachina:kotlin-argparser:2.0.7")
    implementation("net.java.dev.javacc:javacc:7.0.4")

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

    val schemaDocs by creating(Exec::class.java) {
        
        group = "Documentation"

        val docOut1 = "$buildDir/rawSchemaDoc"
        val docOut2 = "$buildDir/finalSchemaDoc"

        val inSchema = "src/main/resources/com/github/oowekyala/jjtx/schema/jjtopts.schema.json"


        // npm install -g bootprint
        // npm install -g bootprint-json-schema

        commandLine = listOf(
            "bootprint",
            "json-schema",
            inSchema,
            docOut1
        )

        doLast {
            // Filter the output to improve it
            // The original project is unbuildable because of shitty npm deps
            filterSchemaDoc(file(docOut1), file(docOut2))
            println("Schema doc accessible at $docOut2/index.html")
        }
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
