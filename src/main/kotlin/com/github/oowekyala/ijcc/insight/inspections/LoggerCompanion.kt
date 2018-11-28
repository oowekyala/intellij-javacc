package com.github.oowekyala.ijcc.insight.inspections

import com.intellij.openapi.diagnostic.Logger

/**
 * @author Clément Fournier
 * @since 1.0
 */
interface LoggerCompanion {

    val LOG
        get() = Logger.getInstance("#${javaClass.enclosingClass.name}")

}