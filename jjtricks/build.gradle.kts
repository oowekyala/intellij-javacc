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



    // this is for tests
    testCompile(rootProject.sourceSets["test"].output)
    testCompile("com.github.oowekyala.treeutils:tree-matchers:2.0.2")
    testCompile("org.jetbrains.kotlin:kotlin-reflect:$KotlinVersion")
    testImplementation("io.kotlintest:kotlintest-runner-junit5:3.1.11")


}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}
tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
