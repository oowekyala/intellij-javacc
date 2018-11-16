package com.github.oowekyala.gark87.idea.javacc.psi.reference

import com.github.oowekyala.gark87.idea.javacc.psi.Identifier
import com.github.oowekyala.gark87.idea.javacc.psi.JavaccDeclarationElement
import java.util.*

/**
 * @author gark87
 */
class JavaCCResolveProcessor(private val myName: String, myTypes: EnumSet<DeclarationType>) : JavaCCScopeProcessor(myTypes) {

    val result: Identifier?
        get() = if (candidates.isEmpty()) {
            null
        } else candidates[0]

    override fun isValid(decl: JavaccDeclarationElement): Boolean = decl.identifier?.name == myName

    override fun keepLooking(decl: JavaccDeclarationElement): Boolean = !isValid(decl)
}
