package com.github.oowekyala.ijcc.inspections

import com.github.oowekyala.ijcc.JavaccLanguage
import com.intellij.codeInspection.ex.BaseLocalInspectionTool

/**
 * Base class for inspections.
 *
 * @author Clément Fournier
 * @since 1.0
 */
abstract class JavaccInspectionBase(private val myDisplayName: String) : BaseLocalInspectionTool() {

    override fun getDisplayName(): String = myDisplayName
    override fun getShortName(): String = myDisplayName.replace("\\s*", "")
    override fun getGroupDisplayName(): String = JavaccLanguage.displayName
}