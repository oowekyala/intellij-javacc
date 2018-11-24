package com.github.oowekyala.ijcc.inspections

import com.intellij.openapi.diagnostic.Logger

/**
 * @author Clément Fournier
 * @since 1.0
 */
abstract class BaseInspectionCompanion {

    protected val LOG = Logger.getInstance("#${javaClass.enclosingClass.name}")


}