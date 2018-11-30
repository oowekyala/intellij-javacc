// This is a generated file. Not intended for manual editing.
package com.github.oowekyala.ijcc.lang.psi.impl

import com.github.oowekyala.ijcc.lang.JavaccTypes
import com.github.oowekyala.ijcc.lang.psi.*
import com.github.oowekyala.ijcc.util.filterMapAs
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElementVisitor

class JccAssignedExpansionUnitImpl(node: ASTNode) : JccExpansionUnitImpl(node), JccAssignedExpansionUnit {
    override val nonTerminalExpansionUnit: JccNonTerminalExpansionUnit?
        get() = childrenSequence().filterMapAs<JccNonTerminalExpansionUnit>().firstOrNull()
    override val javaAssignmentLhs: JccJavaAssignmentLhs
        get() = findNotNullChildByType(JavaccTypes.JCC_JAVA_ASSIGNMENT_LHS)

    override val regularExpression: JccRegularExpression?
        get() = childrenSequence().filterMapAs<JccRegularExpression>().firstOrNull()

    override fun accept(visitor: JccVisitor) {
        visitor.visitAssignedExpansionUnit(this)
    }

    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is JccVisitor)
            accept(visitor)
        else
            super.accept(visitor)
    }


}
