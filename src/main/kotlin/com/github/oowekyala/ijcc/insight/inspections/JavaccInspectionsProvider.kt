package com.github.oowekyala.ijcc.insight.inspections

import com.intellij.codeInspection.InspectionToolProvider

/**
 * @author Clément Fournier
 * @since 1.0
 */
object JavaccInspectionsProvider : InspectionToolProvider {
    override fun getInspectionClasses(): Array<Class<out Any>> = arrayOf(
        TokenCanNeverBeMatchedInspection::class.java,
        UnnamedRegexInspection::class.java,
        UnnecessaryInlineRegexInspection::class.java
    )
}