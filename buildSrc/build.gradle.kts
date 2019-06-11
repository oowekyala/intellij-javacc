@file:Suppress("PropertyName", "LocalVariableName")

plugins {
    kotlin("jvm") version "1.3.10"
    java
    `kotlin-dsl`
}

val KotlinVersion = "1.3.10"

repositories {
    mavenCentral()
}



dependencies {
    compile("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$KotlinVersion")
    compile("org.jsoup:jsoup:1.12.1")
    implementation("com.google.code.gson:gson:2.8.5")
    implementation("org.yaml:snakeyaml:1.24")
}


tasks {
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

