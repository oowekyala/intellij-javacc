package com.github.oowekyala.ijcc.structure

import com.github.oowekyala.ijcc.lang.psi.JccNonTerminalProduction
import com.github.oowekyala.ijcc.util.JavaccIcons
import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.ide.structureView.impl.common.PsiTreeElementBase
import javax.swing.Icon

/**
 * @author Clément Fournier
 * @since 1.0
 */
class NonTerminalStructureNode(private val nonTerminalProduction: JccNonTerminalProduction) :
    PsiTreeElementBase<JccNonTerminalProduction>(nonTerminalProduction),
    JavaccStructureViewElement {
    override fun getChildrenBase(): MutableCollection<StructureViewTreeElement> = mutableListOf()


    override fun getPresentableText(): String? = nonTerminalProduction.name.plus("()")

    override fun getIcon(open: Boolean): Icon? = JavaccIcons.NONTERMINAL

}