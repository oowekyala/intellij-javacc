package com.github.oowekyala.ijcc.ide.inspections

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile

/**
 * @author Clément Fournier
 * @since 1.2
 */
class WrongNatureMetaInspection : JccInspectionBase(DisplayName) {


    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return super.buildVisitor(holder, isOnTheFly)
    }


    companion object {
        const val DisplayName = "JJTree constructs in .jj file"
    }
}