package com.github.oowekyala.ijcc.insight.quickdoc

import com.github.oowekyala.ijcc.insight.model.LexicalState
import com.github.oowekyala.ijcc.insight.quickdoc.HtmlUtil.angles
import com.github.oowekyala.ijcc.insight.quickdoc.HtmlUtil.bold
import com.github.oowekyala.ijcc.insight.quickdoc.JccDocUtil.buildQuickDoc
import com.github.oowekyala.ijcc.lang.psi.*
import com.github.oowekyala.ijcc.util.foreachAndBetween
import com.intellij.codeInsight.documentation.DocumentationManager
import org.intellij.lang.annotations.Language

/**
 * @author Clément Fournier
 * @since 1.0
 */
object JccTerminalDocMaker {

    @Language("HTML")
    fun makeDoc(spec: JccRegexprSpec): String = buildQuickDoc {
        val prod = spec.production

        definition {
            val name =
                    spec.regularExpression
                        .let { it as? JccNamedRegularExpression }
                        ?.name
                        ?.let { bold(angles(it)) } ?: "(unnamed)"

            "${prod.regexprKind.text}\t$name"
        }

        sections {
            section("Lexical states") {
                lexicalStatesOf(prod)
            }
            buildSection("Expansion") {
                spec.regularExpression.accept(RegexDocVisitor(this))
            }
        }
    }


    /** Limit to the number of character descriptors expanded. */
    private const val MaxChars = 10


    class RegexDocVisitor(private val sb: StringBuilder) : RegexLikeDFVisitor() {

        override fun visitLiteralRegexpUnit(o: JccLiteralRegexpUnit) {
            sb.append(o.text)
        }

        override fun visitLiteralRegularExpression(o: JccLiteralRegularExpression) {
            o.unit.accept(this)
        }

        override fun visitNamedRegularExpression(o: JccNamedRegularExpression) {
            o.regexpElement?.accept(this)
        }

        override fun visitEofRegularExpression(o: JccEofRegularExpression) {
            sb.append("&lt;EOF&gt;")
        }

        override fun visitTokenReferenceUnit(o: JccTokenReferenceUnit) {
            val reffed: JccRegexprSpec? = o.typedReference.resolveToken()

            // make the linktext be the literal if needed.
            val linkText = reffed?.asSingleLiteral()?.text ?: angles(o.nameIdentifier.name)

            DocumentationManager.createHyperlink(
                sb,
                reffed?.let { JccDocUtil.getLinkRefTo(it) },
                linkText,
                false
            )
        }

        override fun visitRegularExpressionReference(o: JccRegularExpressionReference) {
            o.unit.accept(this)
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
        }

        override fun visitCharacterDescriptor(o: JccCharacterDescriptor) {
            sb.append(o.text.replace("\\s*", ""))
        }
    }


    private fun lexicalStatesOf(prod: JccRegularExprProduction): String = prod.lexicalStateList.let {
        it?.identifierList?.let {
            if (it.isEmpty()) "All"
            else it.joinToString(separator = ", ") { it.name }
        } ?: LexicalState.DefaultStateName
    }

}