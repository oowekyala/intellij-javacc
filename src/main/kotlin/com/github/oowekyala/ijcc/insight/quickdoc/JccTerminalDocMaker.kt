package com.github.oowekyala.ijcc.insight.quickdoc

import com.github.oowekyala.ijcc.insight.quickdoc.JccDocUtil.angles
import com.github.oowekyala.ijcc.insight.quickdoc.JccDocUtil.bold
import com.github.oowekyala.ijcc.lang.psi.*
import com.github.oowekyala.ijcc.model.LexicalState
import com.github.oowekyala.ijcc.util.foreachAndBetween
import com.intellij.codeInsight.documentation.DocumentationManager
import com.intellij.lang.documentation.DocumentationMarkup.*
import org.intellij.lang.annotations.Language

/**
 * @author Clément Fournier
 * @since 1.0
 */
object JccTerminalDocMaker {

    private fun makeDef(name: String, regexKind: String) = "$regexKind\t$name"

    @Language("HTML")
    fun makeDoc(spec: JccRegexprSpec): String {

        val (regexpKind, states) = run {
            val prod = spec.production
            Pair(prod.regexprKind.text, lexicalStatesOf(prod))
        }

        val name =
                spec.regularExpression
                    .let { it as? JccNamedRegularExpression }
                    ?.name
                    ?.let { bold(angles(it)) } ?: "(unnamed)"

        val definition = makeDef(name, regexpKind)
        val expansion = StringBuilder().also { spec.regularExpression.accept(RegexDocVisitor(it)) }.toString()

        return """
            $DEFINITION_START$definition$DEFINITION_END
            $SECTIONS_START
            ${SECTION_HEADER_START}Lexical states:$SECTION_SEPARATOR<p>$states$SECTION_END
            ${SECTION_HEADER_START}Expansion:$SECTION_SEPARATOR<p>$expansion$SECTION_END
            $SECTIONS_END
        """.trimIndent()

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
            val reffed: JccRegexprSpec? = o.reference.resolveToken()

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
            if (it.isEmpty()) "All"
            else it.joinToString(separator = ", ") { it.name }
        } ?: LexicalState.DefaultStateName
    }

}