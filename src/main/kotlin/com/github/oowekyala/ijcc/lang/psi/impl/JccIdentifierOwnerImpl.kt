package com.github.oowekyala.ijcc.lang.psi.impl

import com.github.oowekyala.ijcc.lang.psi.JccIdentifierOwner
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement

/**
 * An element that has an identifier.
 *
 * @author Clément Fournier
 * @since 1.0
 */
abstract class JccIdentifierOwnerImpl(node: ASTNode) : JavaccPsiElementImpl(node), JccIdentifierOwner {


    override fun getName(): String? = nameIdentifier?.name

    override fun setName(name: String): PsiElement {
        nameIdentifier?.name = name
        return this
    }

}