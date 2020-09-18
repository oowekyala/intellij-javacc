package com.github.oowekyala.ijcc.lang.psi

import com.intellij.psi.PsiRecursiveVisitor

/**
 * @author Clément Fournier
 * @since 1.0
 */
open class DepthFirstVisitor : JccVisitor(), PsiRecursiveVisitor {

    override fun visitPsiElement(o: JccPsiElement) {
        o.acceptChildren(this)
    }

}
