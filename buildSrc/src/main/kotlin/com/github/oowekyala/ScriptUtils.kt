package com.github.oowekyala

import org.gradle.api.Project

const val IjccPackage = "/com/github/oowekyala/ijcc"


fun Project.ijccResource(suffix: String) = buildDir.absolutePath + "/resources/main" + IjccPackage + suffix
