package com.github.oowekyala.ijcc.insight.quickdoc

import com.github.oowekyala.ijcc.lang.psi.*
import com.github.oowekyala.ijcc.model.LexicalState
import com.intellij.codeInsight.documentation.DocumentationManager
import com.intellij.lang.documentation.AbstractDocumentationProvider
import com.intellij.lang.documentation.DocumentationMarkup.*
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager

/**
 * Documentation extension point.
 *
 * @author Clément Fournier
 * @since 1.0
 */
class JccDocumentationProvider : AbstractDocumentationProvider() {

    override fun generateDoc(element: PsiElement?, originalElement: PsiElement?): String? {
        val relevantNode = element?.parentSequence(includeSelf = true)
            ?.first { it is JccRegexprSpec || it is JccNonTerminalProduction }
            ?: return null

        return when (relevantNode) {
            is JccNamedRegularExpression -> makeDoc(relevantNode)
            else                         -> null
        }
    }


    override fun getDocumentationElementForLink(psiManager: PsiManager?,
                                                link: String?,
                                                context: PsiElement?): PsiElement? =
            JccDocUtil.findLinkTarget(psiManager, link, context)
    //
    //    override fun getUrlFor(element: PsiElement?, originalElement: PsiElement?): MutableList<String> =
    //            JccDocUtil.getLinkRefTo(element)?.let { mutableListOf(it) } ?: mutableListOf()


    companion object {

        fun makeDoc(named: JccNamedRegularExpression): String {
            val spec = named.parent as? JccRegexprSpec


            return buildString {
                append(DEFINITION_START)

                val (tokenType, states) = if (spec != null) {
                    val prod = spec.parent as JccRegularExprProduction
                    Pair(prod.regexprKind.text + " ", lexicalStatesOf(prod))
                } else Pair("", "")

                append(tokenType).append("&lt;").append(named.name).append("&gt;")

                append(DEFINITION_END)
                append(SECTIONS_START)

                if (states.isNotEmpty()) {
                    append(SECTION_HEADER_START).append("Lexical states").append(SECTION_SEPARATOR)
                        .append("<p>").append(states).append(SECTION_END)
                }
                append(SECTION_HEADER_START).append("Definition").append(SECTION_SEPARATOR)
                    .append("<p>")
                RegexDocVisitor(this).visitNamedRegularExpression(named)
                append(SECTION_END)
                append(SECTIONS_END)
            }
        }

        private class RegexDocVisitor(private val sb: StringBuilder) : RegexLikeDFVisitor() {
            override fun visitLiteralRegularExpression(o: JccLiteralRegularExpression) {
                sb.append(o.text)
            }

            override fun visitNamedRegularExpression(o: JccNamedRegularExpression) {
                o.regexpElement?.accept(this)
            }

            override fun visitEofRegularExpression(o: JccEofRegularExpression) {
                sb.append("&lt;EOF&gt;")
            }

            override fun visitRegularExpressionReference(o: JccRegularExpressionReference) {
                DocumentationManager.createHyperlink(
                    sb,
                    JccDocUtil.getLinkRefTo(o.reference?.resolve()),
                    "&lt;${o.name}&gt;",
                    false
                )
            }

            override fun visitInlineRegularExpression(o: JccInlineRegularExpression) {
                o.regexpElement?.accept(this)
            }


            override fun visitRegexpSequence(o: JccRegexpSequence) {
                o.regexpUnitList.foreachAndBetween({ sb.append(" ") }) { it.accept(this) }
            }


            fun <T> Iterable<T>.foreachAndBetween(delim: () -> Unit, main: (T) -> Unit) {
                val iterator = iterator()

                if (iterator.hasNext())
                    main(iterator.next())
                while (iterator.hasNext()) {
                    delim()
                    main(iterator.next())
                }
            }

            override fun visitRegexpAlternative(o: JccRegexpAlternative) {
                o.regexpElementList.foreachAndBetween({ sb.append(" | ") }) { it.accept(this) }
            }

            override fun visitCharacterList(o: JccCharacterList) {
                if (o.isNegated) sb.append('~')
                sb.append('[')
                val MaxChars = 10
                val chars = o.characterDescriptorList
                if (chars.size > MaxChars) {
                    sb.append("...]")
                } else {
                    o.characterDescriptorList.forEach { it.accept(this) }
                    sb.append(']')
                }
            }

            override fun visitParenthesizedRegexpUnit(o: JccParenthesizedRegexpUnit) {
                sb.append("( ")
                o.regexpElement.accept(this)
                sb.append(" )")
                val occurrenceIndicator = o.lastChildNoWhitespace
                if (occurrenceIndicator != null) {
                    sb.append(occurrenceIndicator.text)
                }
            }

            override fun visitCharacterDescriptor(o: JccCharacterDescriptor) {
                sb.append(o.text.replace("\\s*", ""))
            }
        }


        fun lexicalStatesOf(prod: JccRegularExprProduction): String = prod.lexicalStateList.let {
            it?.identifierList?.let {
                if (it.isEmpty()) "all"
                else it.joinToString(separator = ", ") { it.name }
            } ?: LexicalState.DefaultStateName
        }


    }


}