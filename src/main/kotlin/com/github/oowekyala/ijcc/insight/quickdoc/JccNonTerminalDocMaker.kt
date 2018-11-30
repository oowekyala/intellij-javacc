package com.github.oowekyala.ijcc.insight.quickdoc

import com.github.oowekyala.ijcc.lang.psi.*
import com.github.oowekyala.ijcc.lang.psi.impl.JccElementFactory
import com.github.oowekyala.ijcc.util.foreachAndBetween
import com.intellij.codeInsight.documentation.DocumentationManager
import com.intellij.lang.documentation.DocumentationMarkup
import com.intellij.psi.PsiSubstitutor
import com.intellij.psi.util.PsiFormatUtil
import com.intellij.psi.util.PsiFormatUtilBase
import org.intellij.lang.annotations.Language

/**
 * @author Clément Fournier
 * @since 1.0
 */
object JccNonTerminalDocMaker {


    fun makeDoc(prod: JccJavacodeProduction): String {
        return buildString {
            append(DocumentationMarkup.DEFINITION_START)
            appendHeader(prod.header)

            append(DocumentationMarkup.DEFINITION_END)
            append(DocumentationMarkup.SECTIONS_START)

            append(DocumentationMarkup.SECTION_HEADER_START).append("(JAVACODE)")
                .append(DocumentationMarkup.SECTION_SEPARATOR)
            append(DocumentationMarkup.SECTION_END)
            append(DocumentationMarkup.SECTIONS_END)
        }
    }

    private fun StringBuilder.appendHeader(header: JccJavaNonTerminalProductionHeader) {
        val psiMethod = JccElementFactory.createJavaMethodForNonterminal(header.project, header)

        PsiFormatUtil.formatMethod(
            psiMethod,
            PsiSubstitutor.EMPTY,
            PsiFormatUtilBase.SHOW_NAME or PsiFormatUtilBase.SHOW_TYPE or PsiFormatUtilBase.SHOW_PARAMETERS,
            PsiFormatUtilBase.SHOW_TYPE or PsiFormatUtilBase.SHOW_NAME
        ).let {
            this.append(it)
        }

    }

    @Language("HTML")
    fun makeDoc(prod: JccBnfProduction): String {
        return buildString {
            append(DocumentationMarkup.DEFINITION_START)
//            append("(BNF) ")
            appendHeader(prod.header)

            append(DocumentationMarkup.DEFINITION_END)
            append(DocumentationMarkup.SECTIONS_START)

            append(DocumentationMarkup.SECTION_HEADER_START).append("BNF:")
                .append(DocumentationMarkup.SECTION_SEPARATOR)
                .append("<p>")
            prod.expansion?.run { this.accept(ExpansionDocVisitor(this@buildString)) }
            append(DocumentationMarkup.SECTION_END)
            append(DocumentationMarkup.SECTIONS_END)
        }
    }

    // TODO this should be formatted to remove unnecessary parentheses and stuff
    private class ExpansionDocVisitor(private val sb: StringBuilder) : DepthFirstVisitor() {

        override fun visitExpansionAlternative(o: JccExpansionAlternative) {
            o.expansionList.foreachAndBetween({ sb.append(" | ") }) { it.accept(this) }
        }

        override fun visitExpansionSequence(o: JccExpansionSequence) {
            o.expansionUnitList.foreachAndBetween({ sb.append(" ") }) { it.accept(this) }
        }

        override fun visitRegexpLike(o: JccRegexpLike) {
            o.accept(JccTerminalDocMaker.RegexDocVisitor(sb))
        }

        override fun visitLiteralRegularExpression(o: JccLiteralRegularExpression) {
            val reffed: JccRegexprSpec? = o.reference?.resolve()

            if (reffed != null) {
                DocumentationManager.createHyperlink(
                    sb,
                    JccDocUtil.getLinkRefTo(reffed),
                    o.text,
                    false
                )
            } else {
                sb.append(o.text)
            }
        }

        override fun visitOptionalExpansionUnit(o: JccOptionalExpansionUnit) {
            sb.append("[")
            o.expansion?.accept(this)
            sb.append("]")
        }

        override fun visitTryCatchExpansionUnit(o: JccTryCatchExpansionUnit) {
            o.expansion?.accept(this)
        }

        override fun visitParserActionsUnit(o: JccParserActionsUnit) {
            // nothing
        }

        override fun visitLocalLookahead(o: JccLocalLookahead) {
            // nothing
        }

        override fun visitNonTerminalExpansionUnit(o: JccNonTerminalExpansionUnit) {
            val reffed = o.reference.resolveProduction()

            DocumentationManager.createHyperlink(
                sb,
                reffed?.let { JccDocUtil.getLinkRefTo(it) },
                "${o.name}()",
                false
            )
        }

        override fun visitAssignedExpansionUnit(o: JccAssignedExpansionUnit) {
            // ignore lhs
            if (o.regularExpression != null) {
                o.regularExpression?.accept(this)
            } else if (o.nonTerminalExpansionUnit != null) {
                o.nonTerminalExpansionUnit?.accept(this)
            }
        }


        override fun visitParenthesizedExpansionUnit(o: JccParenthesizedExpansionUnit) {
            sb.append("( ")
            o.expansion?.accept(this)
            sb.append(" )")
            o.occurrenceIndicator?.run { sb.append(text) }
        }
    }

}