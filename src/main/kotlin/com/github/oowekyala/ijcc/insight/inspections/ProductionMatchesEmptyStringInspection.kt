package com.github.oowekyala.ijcc.insight.inspections

import com.github.oowekyala.ijcc.lang.psi.*
import com.github.oowekyala.ijcc.util.filterMapAs
import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.SuppressionUtil
import com.intellij.psi.PsiFile
import com.intellij.util.ThreeState.*

/**
 * @author Clément Fournier
 * @since 1.0
 */
class ProductionMatchesEmptyStringInspection : JavaccInspectionBase(DisplayName) {

    override fun runForWholeFile(): Boolean = true

    private fun JccFile.determineNullable(): Set<JccBnfProduction> {
        val pending = nonTerminalProductions.filterMapAs<JccBnfProduction>().toMutableSet()

        val nullables = mutableSetOf<JccBnfProduction>()

        var changed = true

        while (changed) {
            changed = false

            for (prod in pending) {

                if (prod.expansion?.isEmptyMatchPossible(nullables) == true) {
                    nullables += prod
                    prod.isNullable = YES
                    changed = true
                }
            }

            pending.removeAll(nullables)
        }

        pending.forEach { it.isNullable = NO }

        return nullables
    }

    override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
        if (file !is JccFile) return null
        if (SuppressionUtil.inspectionResultSuppressed(file, this)) return null


        file.determineNullable()

        return emptyArray()
    }


    companion object {
        const val DisplayName = "Choice branch matches the empty string"
    }
}


/**
 * Returns true if this expansion can expand to the empty string, returns false otherwise.
 * To determine nullability of a production, use [JccNonTerminalProduction.isNullable] instead.
 *
 * @param nullables current known nullable productions. If null then a best guess will be made (when invoked from the highlight visitor)
 */
fun JccExpansion.isEmptyMatchPossible(nullables: Set<JccNonTerminalProduction>? = null): Boolean = when (this) {
    is JccParserActionsUnit          -> true
    is JccLocalLookahead             -> true
    is JccOptionalExpansionUnit      -> true
    is JccRegexpExpansionUnit        -> false
    is JccScopedExpansionUnit        -> expansionUnit.isEmptyMatchPossible()
    is JccAssignedExpansionUnit      -> assignableExpansionUnit?.isEmptyMatchPossible() == true
    is JccParenthesizedExpansionUnit -> occurrenceIndicator.let {
        it is JccZeroOrOne || it is JccZeroOrMore
                || expansion?.isEmptyMatchPossible() == true // test it whether there is a + or nothing
    }
    is JccExpansionSequence          -> expansionUnitList.all { it.isEmptyMatchPossible() }
    is JccExpansionAlternative       -> expansionList.any { it.isEmptyMatchPossible() }
    is JccTryCatchExpansionUnit      -> expansion?.isEmptyMatchPossible() == true
    is JccNonTerminalExpansionUnit   ->
        if (nullables == null)
            typedReference.resolveProduction()?.let {
                // only do this greedy approach if nullable is null
                it is JccBnfProduction && it.computeNullability()
            } ?: false
        // TODO when isReferenceTo is optimised we can instead do nullables.any { typedReference.isReferenceTo(it) }
        else typedReference.resolveProduction()?.let { nullables.contains(it) } == true
    else                             -> false
}

private fun JccBnfProduction.computeNullability(): Boolean {
    if (isNullable == UNSURE) {
        val computed = expansion?.isEmptyMatchPossible()
        if (computed != null) {
            isNullable = if (computed) YES else NO
        } // else stays unsure
    }
    return isNullable == YES
}