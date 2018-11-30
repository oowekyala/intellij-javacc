package com.github.oowekyala.ijcc.lang.parser

import kotlin.reflect.KClass


const val TestResourcesPath = "src/test/resources/"


fun KClass<*>.dataPath(addFixture: Boolean = true) =
        this.java.`package`.name
            .replace('.', '/')
            .let { if (addFixture) it.plus("/fixtures") else it }