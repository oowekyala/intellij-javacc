package com.github.oowekyala.ijcc.lang.psi

import com.intellij.psi.PsiElement

/**
 * @author Clément Fournier
 * @since 1.0
 */
open class DepthFirstVisitor : JccVisitor() {

    override fun visitElement(o: PsiElement?) {
        o?.children?.forEach { it.accept(this) }
    }

}