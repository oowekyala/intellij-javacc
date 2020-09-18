package com.github.oowekyala.ijcc.util

import com.intellij.openapi.diagnostic.Logger

/**
 * Base class for a hidden logger object enclosed in the relevant class.
 * Usage:
 *
 *         private object Log : EnclosedLogger()
 *         // ...
 *         {
 *           Log { debug("bla bla") }
 *         }
 */
abstract class EnclosedLogger {

    private val logger = javaClass.enclosingClass
        .let { it?.name ?: javaClass.name }
        .let { Logger.getInstance(it) }

    operator fun invoke(block: Logger.() -> Unit) = logger.block()

}