package com.github.oowekyala.ijcc.lang.psi.impl

import com.github.oowekyala.ijcc.lang.psi.JccIdentifier
import com.intellij.psi.PsiElement


/**
 * @author Clément Fournier
 * @since 1.0
 */
object JavaccPsiImplUtil {

    fun getName(elt: JccIdentifier): String? = elt.text

    fun setName(elt: JccIdentifier, name: String): PsiElement {
        return elt.replace(JccElementFactory.createIdentifier(elt.project, name))
    }

}