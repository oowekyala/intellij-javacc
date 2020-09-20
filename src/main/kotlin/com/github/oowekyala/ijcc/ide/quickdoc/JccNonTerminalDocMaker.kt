package com.github.oowekyala.ijcc.ide.quickdoc

import com.github.oowekyala.ijcc.ide.inspections.docIsNecessary
import com.github.oowekyala.ijcc.ide.quickdoc.HtmlUtil.psiLink
import com.github.oowekyala.ijcc.ide.quickdoc.JccDocUtil.SectionsBuilder
import com.github.oowekyala.ijcc.ide.quickdoc.JccDocUtil.buildQuickDoc
import com.github.oowekyala.ijcc.ide.structureview.createJavaMethodForNonterminal
import com.github.oowekyala.ijcc.lang.cfa.*
import com.github.oowekyala.ijcc.lang.model.Token
import com.github.oowekyala.ijcc.lang.psi.*
import com.github.oowekyala.ijcc.lang.psi.impl.jccEltFactory
import com.github.oowekyala.ijcc.util.foreachAndBetween
import com.intellij.psi.PsiSubstitutor
import com.intellij.psi.util.PsiFormatUtil
import com.intellij.psi.util.PsiFormatUtilBase
import org.intellij.lang.annotations.Language

/**
 * @author Clément Fournier
 * @since 1.0
 */
object JccNonTerminalDocMaker {

    const val BnfSectionName = "BNF"
    const val StartSectionName = "FIRST set"
    const val CompactStartSectionName = "Condensed FIRST set"
    const val JJTreeSectionName = "JJTree node"

    fun makeDoc(prod: JccJavacodeProduction): String = buildQuickDoc {
        buildDefinition {
            appendHeader(prod.header)
        }
        sections {
            emptySection("(JAVACODE)")
            jjtreeSection(prod)
        }
    }

    private fun StringBuilder.appendHeader(header: JccJavaNonTerminalProductionHeader) {
        val psiMethod = header.project.jccEltFactory.createJavaMethodForNonterminal(header)

        PsiFormatUtil.formatMethod(
            psiMethod,
            PsiSubstitutor.EMPTY,
            PsiFormatUtilBase.SHOW_NAME or PsiFormatUtilBase.SHOW_TYPE or PsiFormatUtilBase.SHOW_PARAMETERS,
            PsiFormatUtilBase.SHOW_TYPE or PsiFormatUtilBase.SHOW_NAME
        ).let {
            append(it)
        }

    }

    @Language("HTML")
    fun makeDoc(prod: JccBnfProduction): String {
        return buildQuickDoc {
            buildDefinition {
                appendHeader(prod.header)
            }
            sections {
                buildSection(BnfSectionName, sectionDelim = " ::=") {
                    prod.expansion?.let { ExpansionMinifierVisitor(this).startOn(it) }
                }
                jjtreeSection(prod)
            }
            sections {
                val condensed = prod.expansion?.firstSet(groupUnary = true) ?: emptySet()
                val full = prod.expansion?.firstSet(groupUnary = false) ?: emptySet()
                if (condensed != full) {
                    buildSection(CompactStartSectionName) {
                        startSetSection(condensed)
                    }
                }
                buildSection(StartSectionName) {
                    startSetSection(full)
                }
            }
        }
    }

    private fun StringBuilder.startSetSection(startSet: Set<AtomicUnit>) {
        append("&nbsp;&nbsp;")
        startSet.foreachAndBetween(delim = { append("<br>| ") }) {
            when (it) {
                is AtomicToken          -> {
                    val str = it.token.asStringToken
                    when {
                        str != null           -> psiLink(
                            builder = this,
                            linkTarget = JccDocUtil.linkRefToToken(it.token),
                            linkTextUnescaped = str.text
                        )
                        it.token.name != null -> psiLink(
                            builder = this,
                            linkTarget = JccDocUtil.linkRefToToken(it.token),
                            linkTextUnescaped = "<${(it.token.name!!)}>"
                        )
                        else                  -> it.token.regularExpression?.accept(JccTerminalDocMaker.RegexDocVisitor(this))
                    }
                }
                is AtomicProduction     ->
                    psiLink(
                        builder = this,
                        linkTarget = JccDocUtil.linkRefToProd(it.production),
                        linkTextUnescaped = "${it.production.name}()"
                    )
                is AtomicUnresolved     -> it.ref.accept(JccTerminalDocMaker.RegexDocVisitor(this))
                is AtomicUnresolvedProd -> it.ref.accept(ExpansionMinifierVisitor(this))
            }
        }
    }


    class ExpansionMinifierVisitor(private val sb: StringBuilder) : DepthFirstVisitor() {

        // spread out the top level alternatives
        var spreadAlternatives: Boolean = false


        fun startOn(jccExpansion: JccExpansion) {

            fun skipUninteresting(exp: JccExpansion): JccExpansion? = when {
                exp is JccParenthesizedExpansionUnit && !exp.docIsNecessary()                         ->
                    exp.expansion?.let { skipUninteresting(it) }
                exp is JccScopedExpansionUnit                                                         -> skipUninteresting(
                    exp.expansionUnit
                )
                exp is JccExpansionSequence && exp.expansionUnitList.count { it.isDocumented() } == 1 ->
                    skipUninteresting(exp.expansionUnitList.first { it.isDocumented() })
                else                                                                                  -> exp
            }

            val root = skipUninteresting(jccExpansion) ?: return

            if (root is JccExpansionAlternative)
                spreadAlternatives = true

            jccExpansion.accept(this)
        }

        private fun JccExpansion.isDocumented(): Boolean = when (this) {
            is JccParserActionsUnit    -> false
            is JccLocalLookaheadUnit   -> false
            is JccExpansionSequence    -> expansionUnitList.any { it.isDocumented() }
            is JccExpansionAlternative -> expansionList.any { it.isDocumented() }
            else                       -> true
        }

        override fun visitExpansionAlternative(o: JccExpansionAlternative) {
            val (prefix, delim) = if (spreadAlternatives) {
                spreadAlternatives = false
                Pair("&nbsp;&nbsp;", "<br/>| ")
            } else Pair("", " | ")

            sb.append(prefix)
            // filtering avoids adding superfluous spaces
            o.expansionList.filter { it.isDocumented() }.foreachAndBetween({ sb.append(delim) }) { it.accept(this) }
        }

        override fun visitExpansionSequence(o: JccExpansionSequence) {
            // filtering avoids adding superfluous spaces
            o.expansionUnitList.filter { it.isDocumented() }.foreachAndBetween({ sb.append(" ") }) { it.accept(this) }
        }

        override fun visitRegexExpansionUnit(o: JccRegexExpansionUnit) {
            o.regularExpression.accept(JccTerminalDocMaker.RegexDocVisitor(sb))
        }

        override fun visitLiteralRegexUnit(o: JccLiteralRegexUnit) {
            val reffed: Token? = o.typedReference?.resolveToken(exact = true)

            if (reffed != null) {
                psiLink(
                    builder = sb,
                    linkTarget = JccDocUtil.linkRefToToken(reffed),
                    linkTextUnescaped = o.text,
                    isCodeLink = true
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

        override fun visitLocalLookaheadUnit(o: JccLocalLookaheadUnit) {
            // nothing
        }

        override fun visitNonTerminalExpansionUnit(o: JccNonTerminalExpansionUnit) {
            val reffed = o.typedReference.resolveProduction()

            psiLink(builder = sb, linkTarget = reffed?.let { JccDocUtil.linkRefToProd(it) }, linkTextUnescaped = "${o.name}()")
        }

        override fun visitAssignedExpansionUnit(o: JccAssignedExpansionUnit) {
            // ignore lhs
            o.assignableExpansionUnit?.accept(this)
        }


        override fun visitParenthesizedExpansionUnit(o: JccParenthesizedExpansionUnit) {
            if (o.myIsUnnecessary()) {
                o.expansion?.accept(this)
                return
            }
            // else

            sb.append("( ")
            o.expansion?.accept(this)
            sb.append(" )")
            o.occurrenceIndicator?.run { sb.append(text) }
        }

        private companion object {

            fun JccParenthesizedExpansionUnit.myIsUnnecessary(): Boolean = !docIsNecessary()


        }
    }

}


internal fun SectionsBuilder.jjtreeSection(owner: JjtNodeClassOwner) {
    buildSection(JccNonTerminalDocMaker.JJTreeSectionName) {

        owner.nodeQualifiedName?.let { qname ->
            val simpleName = qname.split(".").last()
            psiLink(builder = this, linkTarget = qname, linkTextUnescaped = simpleName)
        } ?: append("none")

    }
}
