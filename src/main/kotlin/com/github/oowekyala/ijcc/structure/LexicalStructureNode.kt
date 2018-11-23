package com.github.oowekyala.ijcc.structure

import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.ide.util.treeView.smartTree.TreeElement
import com.intellij.navigation.ItemPresentation

/**
 * [sort alpha:off] [show lexical structure:on] // later [show parser structure] [show jjtree node structure]
 *
 * + Parser declaration
 *   - structure view of the compilation unit
 *
 * + (regexp prod icon) TOKEN <*>
 *   - (token icon) (public icon)   <TOKEN1 : "foo">
 *   - (token icon) (public icon)   <TOKEN2 : "bar">  -> <IN_XPATH_COMMENT>
 *   - (token icon) (private icon)  <TOKEN3 : "bar">  -> <IN_XPATH_COMMENT>
 * + (regexp prod icon) SKIP <LEXICAL_STATE>
 *
 * - (bnf production icon)      (public icon) // same presentation as a java method (argument types, return type)
 * - (javacode production icon) (public icon) // same presentation as a java method (argument types, return type)
 *
 *
 *
 *
 *
 * @author Clément Fournier
 * @since 1.0
 */
class LexicalStructureNode : StructureViewTreeElement {
    override fun navigate(requestFocus: Boolean) {
        // do nothing
    }

    override fun getPresentation(): ItemPresentation {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getChildren(): Array<TreeElement> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun canNavigate(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getValue(): Any {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun canNavigateToSource(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}