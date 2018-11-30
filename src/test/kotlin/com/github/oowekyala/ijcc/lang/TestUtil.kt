package com.github.oowekyala.ijcc.lang

import kotlin.reflect.KClass


const val TestResourcesPath = "src/test/resources/"


fun KClass<*>.dataPath(vararg addSegments: String) =
        this.java.`package`.name
            .replace('.', '/')
            .let { "$it/${addSegments.joinToString(separator = "/")}" }