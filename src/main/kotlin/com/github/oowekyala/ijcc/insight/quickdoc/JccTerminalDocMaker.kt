package com.github.oowekyala.ijcc.insight.quickdoc

import com.github.oowekyala.ijcc.lang.psi.*
import com.github.oowekyala.ijcc.model.LexicalState
import com.github.oowekyala.ijcc.util.foreachAndBetween
import com.intellij.codeInsight.documentation.DocumentationManager
import com.intellij.lang.documentation.DocumentationMarkup
import org.intellij.lang.annotations.Language

/**
 * @author Clément Fournier
 * @since 1.0
 */
object JccTerminalDocMaker {

    @Language("HTML")
    fun makeDoc(named: JccNamedRegularExpression): String {
        val spec = named.parent as? JccRegexprSpec


        return buildString {
            append(DocumentationMarkup.DEFINITION_START)

            val (tokenType, states) = if (spec != null) {
                val prod = spec.production
                Pair(prod.regexprKind.text + " ", lexicalStatesOf(prod))
            } else Pair("", "")

            append(tokenType).append("&lt;").append(named.name).append("&gt;")

            append(DocumentationMarkup.DEFINITION_END)
            append(DocumentationMarkup.SECTIONS_START)

            if (states.isNotEmpty()) {
                append(DocumentationMarkup.SECTION_HEADER_START).append("Lexical states")
                    .append(DocumentationMarkup.SECTION_SEPARATOR)
                    .append("<p>").append(states).append(DocumentationMarkup.SECTION_END)
            }
            append(DocumentationMarkup.SECTION_HEADER_START).append("Definition")
                .append(DocumentationMarkup.SECTION_SEPARATOR)
                .append("<p>")
            RegexDocVisitor(this).visitNamedRegularExpression(named)
            append(DocumentationMarkup.SECTION_END)
            append(DocumentationMarkup.SECTIONS_END)
        }
    }


    /** Limit to the number of character descriptors expanded. */
    private const val MaxChars = 10


    class RegexDocVisitor(private val sb: StringBuilder) : RegexLikeDFVisitor() {

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
            val reffed = o.reference.resolve()

            // make the linktext be the literal if needed.
            val linkText = reffed?.asSingleLiteral()?.text ?: "&lt;${o.name}&gt;"

            DocumentationManager.createHyperlink(
                sb,
                reffed?.let { JccDocUtil.getLinkRefTo(it) },
                linkText,
                false
            )
        }

        override fun visitInlineRegularExpression(o: JccInlineRegularExpression) {
            o.regexpElement?.accept(this)
        }


        override fun visitRegexpSequence(o: JccRegexpSequence) {
            o.regexpUnitList.foreachAndBetween({ sb.append(" ") }) { it.accept(this) }
        }

        override fun visitRegexpAlternative(o: JccRegexpAlternative) {
            o.regexpElementList.foreachAndBetween({ sb.append(" | ") }) { it.accept(this) }
        }


        override fun visitCharacterList(o: JccCharacterList) {
            if (o.isNegated) sb.append('~')
            sb.append('[')
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
            o.occurrenceIndicator?.run { sb.append(text) }
            o.repetitionRange?.run { sb.append(text) }
        }

        override fun visitCharacterDescriptor(o: JccCharacterDescriptor) {
            sb.append(o.text.replace("\\s*", ""))
        }
    }


    private fun lexicalStatesOf(prod: JccRegularExprProduction): String = prod.lexicalStateList.let {
        it?.identifierList?.let {
            if (it.isEmpty()) "all"
            else it.joinToString(separator = ", ") { it.name }
        } ?: LexicalState.DefaultStateName
    }

}