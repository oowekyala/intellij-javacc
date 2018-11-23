package com.github.oowekyala.ijcc.structure

import com.github.oowekyala.ijcc.lang.psi.*
import com.github.oowekyala.ijcc.lang.psi.impl.JccElementFactory
import com.github.oowekyala.ijcc.lang.psi.impl.JccFileImpl
import com.github.oowekyala.ijcc.util.JavaccIcons
import com.intellij.ide.structureView.StructureViewModel
import com.intellij.ide.structureView.StructureViewModelBase
import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.ide.structureView.impl.common.PsiTreeElementBase
import com.intellij.ide.util.FileStructureFilter
import com.intellij.ide.util.treeView.smartTree.*
import com.intellij.openapi.actionSystem.Shortcut
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiSubstitutor
import com.intellij.psi.util.PsiFormatUtil
import com.intellij.psi.util.PsiFormatUtilBase.*
import com.intellij.util.PlatformIcons
import javax.swing.Icon

/**
 * Model for the structure view, implements logic for filter buttons and sorting.
 *
 * @author Clément Fournier
 * @since 1.0
 */
class JavaccFileStructureViewModel(psiFile: JccFileImpl)
    : StructureViewModelBase(psiFile, MyElement(psiFile)), StructureViewModel.ElementInfoProvider {

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

    //    override fun isSuitable(element: PsiElement?): Boolean {
    //        return element is BnfAttrs || element is BnfRule
    //    }

    override fun getFilters(): Array<Filter> = arrayOf(terminalFilter())

    companion object {

        class MyElement(element: PsiElement) : PsiTreeElementBase<PsiElement>(element), SortableTreeElement {

            override fun getAlphaSortKey(): String {
                return presentableText
            }

            override fun getChildrenBase(): Collection<StructureViewTreeElement> {
                val elt = element
                return when (elt) {
                    is JccFile                  -> elt.regexpProductions.plus(elt.nonTerminalProductions)
                    is JccRegularExprProduction -> elt.regexprSpecList
                    is JccJavaccOptions         -> elt.optionBindingList
                    else                        -> emptyList()
                }.map { MyElement(it) }
            }

            override fun getPresentableText(): String {
                val element = element
                return when (element) {
                    is JccJavaccOptions         -> "options" // TODO add parser class name
                    is JccOptionBinding         -> "${element.name} = ${element.stringValue}"

                    is JccRegularExprProduction -> getRegexpProductionDisplayName(element)
                    is JccRegexprSpec           -> getRegexpSpecDisplayName(element)
                    is JccNonTerminalProduction -> getProductionDisplayName(element)

                    is JccFile                  -> return element.name
                    else                        -> return "" + element!!
                }
            }


            private fun getRegexpSpecDisplayName(spec: JccRegexprSpec): String {
                val builder = StringBuilder()

                fun appendRegexpSpecSuffix(hasPrefix: Boolean, regex: JccRegularExpression) {
                    if (regex is JccLiteralRegularExpression) {
                        if (hasPrefix)
                            builder.append(": ")
                        builder.append(regex.text)
                    }
                }


                builder.append('<')

                val regex = spec.regularExpression
                if (regex is JccNamedRegularExpression) {
                    builder.append(regex.name)
                    appendRegexpSpecSuffix(true, regex.regularExpression)
                } else {
                    appendRegexpSpecSuffix(false, regex)
                }

                builder.append(">")

                val outboundState = spec.lexicalState
                if (outboundState != null) {
                    builder.append(" -> ").append(outboundState.name)
                }

                return builder.toString()
            }

            private fun getRegexpProductionDisplayName(prod: JccRegularExprProduction): String {
                val states = prod.lexicalStateList

                val builder = StringBuilder()

                builder.append(prod.regexprKind.text)

                if (states != null) {
                    val identList = states.identifierList

                    if (identList.isEmpty()) {
                        builder.append(" <*>")
                    } else {
                        identList.joinTo(builder, separator = ", ", prefix = " <", postfix = ">") { it.name }
                    }
                }

                return builder.toString()
            }

            private fun getProductionDisplayName(nonTerminalProduction: JccNonTerminalProduction): String {
                val header = nonTerminalProduction.header

                val psiMethod = JccElementFactory.createJavaMethodForNonterminal(header.project, header)

                val dumb = DumbService.isDumb(psiMethod.project)
                val method = PsiFormatUtil.formatMethod(
                    psiMethod,
                    PsiSubstitutor.EMPTY,
                    SHOW_NAME or TYPE_AFTER or SHOW_PARAMETERS or if (dumb) 0 else SHOW_TYPE,
                    if (dumb) SHOW_NAME else SHOW_TYPE
                )
                return StringUtil.replace(method, ":", ": ")
            }

            override fun getIcon(open: Boolean): Icon? {
                val element = element ?: return null
                return when (element) {
                    is JccJavaccOptions         -> PlatformIcons.PACKAGE_ICON
                    is JccOptionBinding         -> PlatformIcons.ANNOTATION_TYPE_ICON
                    is JccRegexprSpec           -> JavaccIcons.TERMINAL
                    is JccRegularExprProduction -> JavaccIcons.TERMINAL
                    is JccNonTerminalProduction -> JavaccIcons.NONTERMINAL
                    else                        -> element.getIcon(0)
                }
            }
        }

        // TODO sort in document order, see how Grammar-Kit did it
        // (or kotlin for that matter)
        // optionally sort alphabetically like Kotlin structure does

        //        private val documentOrderSorter = object : Sorter {}


        private fun terminalFilter(): Filter = object : FileStructureFilter {
            override fun getCheckBoxText(): String = "Show terminals"

            override fun getShortcut(): Array<Shortcut> = emptyArray()

            override fun isVisible(treeElement: TreeElement): Boolean =
                    when (treeElement.let { it as MyElement }.element) {
                        is JccRegexprSpec           -> false
                        is JccRegularExprProduction -> false
                        else                        -> true
                    }

            override fun isReverted(): Boolean = true

            override fun getPresentation(): ActionPresentation =
                    ActionPresentationData("Show terminals", "Show terminals (tokens)", JavaccIcons.TERMINAL)

            override fun getName(): String = "TerminalFilter"
        }
    }
}
