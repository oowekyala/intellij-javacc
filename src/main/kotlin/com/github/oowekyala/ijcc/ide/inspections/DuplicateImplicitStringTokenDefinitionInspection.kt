package com.github.oowekyala.ijcc.ide.inspections

import com.github.oowekyala.ijcc.lang.model.SyntheticToken
import com.github.oowekyala.ijcc.lang.psi.*
import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.codeInspection.SuppressionUtil
import com.intellij.psi.PsiFile

/**
 * @author Clément Fournier
 * @since 1.1
 */
class DuplicateImplicitStringTokenDefinitionInspection : JccInspectionBase(DisplayName) {


    override fun runForWholeFile(): Boolean = true


    override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
        if (file !is JccFile) return null
        if (SuppressionUtil.inspectionResultSuppressed(file, this)) return null

        val holder = ProblemsHolder(manager, file, isOnTheFly)

        file.lexicalGrammar
            .allTokens
            .filterIsInstance<SyntheticToken>()
            .filter { it.asStringToken != null }
            .forEach {

            }

        return holder.resultsArray
    }


    companion object {
        const val DisplayName = "Duplicated implicit token definition"
    }
}