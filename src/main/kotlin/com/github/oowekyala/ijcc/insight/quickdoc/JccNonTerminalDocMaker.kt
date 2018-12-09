package com.github.oowekyala.ijcc.insight.quickdoc

import com.github.oowekyala.ijcc.insight.quickdoc.JccDocUtil.buildQuickDoc
import com.github.oowekyala.ijcc.lang.psi.*
import com.github.oowekyala.ijcc.lang.psi.impl.JccElementFactory
import com.github.oowekyala.ijcc.util.foreachAndBetween
import com.intellij.codeInsight.documentation.DocumentationManager
import com.intellij.psi.PsiSubstitutor
import com.intellij.psi.util.PsiFormatUtil
import com.intellij.psi.util.PsiFormatUtilBase
import org.intellij.lang.annotations.Language

/**
 * @author Clément Fournier
 * @since 1.0
 */
object JccNonTerminalDocMaker {


    fun makeDoc(prod: JccJavacodeProduction): String = buildQuickDoc {
        buildDefinition {
            appendHeader(prod.header)
        }
        sections {
            emptySection("(JAVACODE)")
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
        return buildQuickDoc {
            buildDefinition {
                appendHeader(prod.header)
            }
            sections {
                buildSection("BNF", sectionDelim = " ::=") {
                    prod.expansion?.let { ExpansionMinifierVisitor(this).startOn(it) }
                }
            }
        }
    }


    private class ExpansionMinifierVisitor(private val sb: StringBuilder) : DepthFirstVisitor() {

        // spread out the top level alternatives
        var spreadAlternatives: Boolean = false

        fun startOn(jccExpansion: JccExpansion) {
            if (jccExpansion is JccExpansionAlternative)
                spreadAlternatives = true

            jccExpansion.accept(this)
        }

        override fun visitExpansionAlternative(o: JccExpansionAlternative) {
            val (prefix, delim) = if (spreadAlternatives) {
                spreadAlternatives = false
                Pair("&nbsp;&nbsp;", "<br/>| ")
            } else Pair("", " | ")

            sb.append(prefix)
            o.expansionList.foreachAndBetween({ sb.append(delim) }) { it.accept(this) }
        }

        override fun visitExpansionSequence(o: JccExpansionSequence) {
            o.expansionUnitList.foreachAndBetween({ sb.append(" ") }) { it.accept(this) }
        }

        override fun visitRegexpExpansionUnit(o: JccRegexpExpansionUnit) {
            o.regularExpression.accept(JccTerminalDocMaker.RegexDocVisitor(sb))
        }

        override fun visitLiteralRegexpUnit(o: JccLiteralRegexpUnit) {
            val reffed: JccRegexprSpec? = o.typedReference?.resolve()

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
            val reffed = o.typedReference.resolveProduction()

            DocumentationManager.createHyperlink(
                sb,
                reffed?.let { JccDocUtil.getLinkRefTo(it) },
                "${o.name}()",
                false
            )
        }

        override fun visitAssignedExpansionUnit(o: JccAssignedExpansionUnit) {
            // ignore lhs
            o.assignableExpansionUnit?.accept(this)
        }


        override fun visitParenthesizedExpansionUnit(o: JccParenthesizedExpansionUnit) {
            sb.append("( ")
            o.expansion?.accept(this)
            sb.append(" )")
            o.occurrenceIndicator?.run { sb.append(text) }
        }
    }

}