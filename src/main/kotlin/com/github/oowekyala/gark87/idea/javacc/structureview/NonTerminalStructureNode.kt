package com.github.oowekyala.gark87.idea.javacc.structureview

import com.github.oowekyala.gark87.idea.javacc.psi.NonTerminalProduction
import com.github.oowekyala.gark87.idea.javacc.util.JavaCCIcons
import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.ide.structureView.impl.common.PsiTreeElementBase
import javax.swing.Icon

/**
 * @author Clément Fournier
 * @since 1.0
 */
class NonTerminalStructureNode(private val nonTerminalProduction: NonTerminalProduction) :
    PsiTreeElementBase<NonTerminalProduction>(nonTerminalProduction), JavaccStructureViewElement {
    override fun getChildrenBase(): MutableCollection<StructureViewTreeElement> = mutableListOf()


    override fun getPresentableText(): String? = nonTerminalProduction.identifier?.name?.plus("()")

    override fun getIcon(open: Boolean): Icon? = JavaCCIcons.NONTERMINAL.icon

}