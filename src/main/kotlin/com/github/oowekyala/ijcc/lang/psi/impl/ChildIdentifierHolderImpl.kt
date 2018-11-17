package com.github.oowekyala.ijcc.lang.psi.impl

import com.github.oowekyala.ijcc.lang.psi.JccIdentifier
import com.github.oowekyala.ijcc.lang.psi.JccIdentifierOwner
import com.intellij.lang.ASTNode

/**
 * @author Clément Fournier
 * @since 1.0
 */
abstract class ChildIdentifierHolderImpl(node: ASTNode) : JavaccPsiElementImpl(node), JccIdentifierOwner {

    override val nameIdentifier: JccIdentifier?
        get() = children.first { it is JccIdentifier } as? JccIdentifier

    override fun getName(): String? {
        return super<JccIdentifierOwner>.getName()
    }

}
