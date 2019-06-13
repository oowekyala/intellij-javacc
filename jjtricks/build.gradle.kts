import com.github.oowekyala.*
import groovy.xml.dom.DOMCategory.attributes
import org.gradle.internal.impldep.org.junit.experimental.categories.Categories.CategoryFilter.exclude


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
        // FIXME jjtricks should not depend on platform-impl,
        // currently done just to get GeneratedParserUtilBase,
        // but bloats the jar
        includeJars("platform-api", "platform-impl", rootProject = rootProject)
    }

    // trick Idea into putting those on the classpath when
    // running app inside IDE
    compileOnly(ijdeps)
    runtimeOnly(ijdeps)

    api("com.google.guava:guava:27.0.1-jre")
    api("org.apache.velocity:velocity:1.7")

    implementation("velocity-tools:velocity-tools-generic:1.4")
    implementation("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.1")
    implementation("com.google.code.gson:gson:2.8.5")
    implementation("com.github.everit-org.json-schema:org.everit.json.schema:1.11.1")
    implementation("com.github.oowekyala.treeutils:tree-printers:2.1.0")
    implementation("org.yaml:snakeyaml:1.24")
    implementation("com.google.googlejavaformat:google-java-format:1.7")
    implementation("net.java.dev.javacc:javacc:5.0")
    implementation("fr.inria.gforge.spoon:spoon-core:7.5.0-beta-21")
    // for debugging only, this pulls in a huge IBM dependency to support emojis...
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

    val schemaPrefix = "src/main/resources/com/github/oowekyala/jjtx/schema/jjtopts.schema"
    val yamlSchema = "$schemaPrefix.yaml"
    val jsonSchema = "$schemaPrefix.json"


    val schemaToJson by creating {

        doLast {
            yamlToJson(file(yamlSchema), file(jsonSchema))
        }
    }

    val schemaDocs by creating(Exec::class.java) {
        
        group = "Documentation"

        val docOut1 = "$buildDir/rawSchemaDoc"
        val docOut2 = "$buildDir/finalSchemaDoc"



        // npm install -g bootprint
        // npm install -g bootprint-json-schema

        commandLine = listOf(
            "bootprint",
            "json-schema",
            jsonSchema,
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
            exclude(dependency("commons-logging:commons-logging:.*"))
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
