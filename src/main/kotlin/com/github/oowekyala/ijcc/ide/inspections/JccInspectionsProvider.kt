package com.github.oowekyala.ijcc.ide.inspections

import com.intellij.codeInspection.InspectionToolProvider

/**
 * @author Clément Fournier
 * @since 1.0
 */
object JccInspectionsProvider : InspectionToolProvider {
    override fun getInspectionClasses(): Array<Class<out JccInspectionBase>> = arrayOf(
        TokenCanNeverBeMatchedInspection::class.java,
        BnfStringCanNeverBeMatchedInspection::class.java,
        UnnamedRegexInspection::class.java,
        UnnecessaryAngledBracesRegexInspection::class.java,
        JccUnnecessaryParenthesesInspection::class.java,
        SuspiciousNodeDescriptorExprInspection::class.java,
        UnusedProductionInspection::class.java,
        UnusedPrivateRegexInspection::class.java,
        EmptyParserActionsInspection::class.java,
        ConsecutiveParserActionsInspection::class.java,
        LookaheadIsNotAtChoicePointInspection::class.java,
        ActionWithinLookaheadInspection::class.java,
        LeftRecursiveProductionInspection::class.java,
        LoopInRegexInspection::class.java,
        RegexMayMatchEmptyStringInspection::class.java
    )
}
