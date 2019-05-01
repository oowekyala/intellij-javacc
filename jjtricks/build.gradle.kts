import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    kotlin("jvm")
    id("org.jetbrains.intellij")
}

group = "com.github.oowekyala"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    compile(rootProject)


    // this is only for JJTX
    compile("com.google.guava:guava:23.5-jre")
    compile("com.google.code.gson:gson:2.8.5")
    compile("com.github.oowekyala.treeutils:tree-printers:2.0.2")
    compile("org.apache.velocity:velocity:1.6.2")
    compile("org.yaml:snakeyaml:1.24")
    compile("com.google.googlejavaformat:google-java-format:1.7")

}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}
tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
