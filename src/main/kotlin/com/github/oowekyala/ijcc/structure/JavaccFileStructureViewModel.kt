package com.github.oowekyala.ijcc.structure

import com.github.oowekyala.ijcc.lang.psi.*
import com.github.oowekyala.ijcc.lang.psi.impl.JccFileImpl
import com.github.oowekyala.ijcc.util.JavaccIcons
import com.intellij.ide.structureView.StructureViewModel
import com.intellij.ide.structureView.StructureViewModelBase
import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.ide.util.FileStructureFilter
import com.intellij.ide.util.treeView.smartTree.ActionPresentation
import com.intellij.ide.util.treeView.smartTree.ActionPresentationData
import com.intellij.ide.util.treeView.smartTree.Filter
import com.intellij.ide.util.treeView.smartTree.TreeElement
import com.intellij.openapi.actionSystem.Shortcut

/**
 * Model for the structure view, implements logic for filter buttons and sorting.
 *
 * @author Clément Fournier
 * @since 1.0
 */
class JavaccFileStructureViewModel(psiFile: JccFileImpl)
    : StructureViewModelBase(psiFile, JccStructureTreeElement(psiFile)), StructureViewModel.ElementInfoProvider {

    /*
     * [sort alpha:off] [show lexical structure:on] [show options:off]// later [show parser structure] [show jjtree node structure]
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
     */

    init {
        withSuitableClasses(
            JccFileImpl::class.java,
            JccJavaccOptions::class.java,
            JccOptionBinding::class.java,

            // FUTURE JccParserDeclaration::class.java,

            JccRegularExprProduction::class.java,
            JccRegexprSpec::class.java,

            JccNonTerminalProduction::class.java
        )
    }


    override fun isAlwaysShowsPlus(element: StructureViewTreeElement): Boolean {
        val value = element.value
        return value is JccRegularExprProduction || value is JccJavaccOptions
    }

    override fun isAlwaysLeaf(element: StructureViewTreeElement): Boolean {
        val value = element.value
        return value is JccRegexprSpec || value is JccNonTerminalProduction
    }


    override fun getFilters(): Array<Filter> = arrayOf(terminalFilter())

    companion object {

        // optionally sort alphabetically like Kotlin structure does


        private fun terminalFilter(): Filter = object : FileStructureFilter {
            override fun getCheckBoxText(): String = "Show lexical structure"

            override fun getShortcut(): Array<Shortcut> = emptyArray()

            override fun isVisible(treeElement: TreeElement): Boolean =
                    when (treeElement.let { it as JccStructureTreeElement }.element) {
                        is JccRegexprSpec           -> false
                        is JccRegularExprProduction -> false
                        else                        -> true
                    }

            override fun isReverted(): Boolean = true

            override fun getPresentation(): ActionPresentation =
                    ActionPresentationData("Show lexical structure", "Show terminals (tokens)", JavaccIcons.TOKEN_HEADER)

            override fun getName(): String = "TerminalFilter"
        }
    }

}
