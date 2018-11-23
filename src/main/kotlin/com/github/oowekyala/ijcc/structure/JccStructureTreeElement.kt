package com.github.oowekyala.ijcc.structure

import com.github.oowekyala.ijcc.lang.psi.*
import com.github.oowekyala.ijcc.lang.psi.impl.JccElementFactory
import com.github.oowekyala.ijcc.util.JavaccIcons
import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.ide.structureView.impl.common.PsiTreeElementBase
import com.intellij.ide.util.treeView.smartTree.SortableTreeElement
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiSubstitutor
import com.intellij.psi.util.PsiFormatUtil
import com.intellij.psi.util.PsiFormatUtilBase
import com.intellij.util.PlatformIcons
import com.intellij.util.ui.UIUtil
import javax.swing.Icon

/**
 * One element of the structure view. This class is used for all elements, regardless of their type.
 */
class JccStructureTreeElement(element: JavaccPsiElement)
    : PsiTreeElementBase<JavaccPsiElement>(element), SortableTreeElement {

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
        }.map { JccStructureTreeElement(it) }
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

        builder.append('<')

        val regex = spec.regularExpression
        if (regex is JccNamedRegularExpression) {
            builder.append(regex.name)
            if (regex.regularExpression is JccLiteralRegularExpression) {
                builder.append(": ").append(regex.regularExpression.text)
            }
        } else if (regex is JccLiteralRegularExpression) {
            builder.append(regex.text)
        } else if (regex is JccInlineRegularExpression) {
            if (regex.regularExpression is JccLiteralRegularExpression) {
                builder.append(regex.regularExpression.text)
            }
        }

        builder.append(">")
        return builder.toString()
    }

    override fun getLocationString(): String? {
        val element = element
        if (element is JccRegexprSpec) {
            val outboundState = element.lexicalState
            if (outboundState != null) {
                return "${UIUtil.rightArrow()} ${outboundState.name}"
            }
        } else if (element is JccRegularExprProduction) {
            val states = element.lexicalStateList
            if (states != null) {
                val identList = states.identifierList

                return if (identList.isEmpty()) {
                    "<*>"
                } else {
                    identList.joinToString(separator = ", ", prefix = "<", postfix = ">") { it.name }
                }
            }
        }
        return null
    }

    private fun getRegexpProductionDisplayName(prod: JccRegularExprProduction): String {

        val builder = StringBuilder()

        builder.append(prod.regexprKind.text)



        return builder.toString()
    }

    private fun getProductionDisplayName(nonTerminalProduction: JccNonTerminalProduction): String {
        val header = nonTerminalProduction.header

        val psiMethod = JccElementFactory.createJavaMethodForNonterminal(
            header.project,
            header
        )

        val dumb = DumbService.isDumb(psiMethod.project)
        val method = PsiFormatUtil.formatMethod(
            psiMethod,
            PsiSubstitutor.EMPTY,
            PsiFormatUtilBase.SHOW_NAME or PsiFormatUtilBase.TYPE_AFTER or PsiFormatUtilBase.SHOW_PARAMETERS or if (dumb) 0 else PsiFormatUtilBase.SHOW_TYPE,
            if (dumb) PsiFormatUtilBase.SHOW_NAME else PsiFormatUtilBase.SHOW_TYPE
        )
        return StringUtil.replace(StringUtil.replace(method, ":void", ""), ":", ": ")
    }

    override fun getIcon(open: Boolean): Icon? {
        val element = element ?: return null
        return when (element) {
            is JccJavaccOptions         -> PlatformIcons.PACKAGE_ICON
            is JccOptionBinding         -> PlatformIcons.ANNOTATION_TYPE_ICON
            is JccRegexprSpec           -> JavaccIcons.TERMINAL
            is JccRegularExprProduction -> JavaccIcons.TERMINAL
            is JccNonTerminalProduction -> JavaccIcons.NONTERMINAL
            else                        -> element.getIcon(0) // this isn't implemented by our classes
        }
    }
}