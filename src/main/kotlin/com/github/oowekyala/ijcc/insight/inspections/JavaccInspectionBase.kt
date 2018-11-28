package com.github.oowekyala.ijcc.insight.inspections

import com.github.oowekyala.ijcc.JavaccLanguage
import com.intellij.codeInspection.InspectionProfileEntry
import com.intellij.codeInspection.ex.BaseLocalInspectionTool

/**
 * Base class for inspections.
 *
 * @author Clément Fournier
 * @since 1.0
 */
abstract class JavaccInspectionBase(private val myDisplayName: String)
    : BaseLocalInspectionTool() {
    override fun getID(): String = InspectionProfileEntry.getShortName(this::class.java.simpleName)
    override fun getDisplayName(): String = myDisplayName
    override fun getShortName(): String = id
    override fun getGroupDisplayName(): String = JavaccLanguage.displayName


}