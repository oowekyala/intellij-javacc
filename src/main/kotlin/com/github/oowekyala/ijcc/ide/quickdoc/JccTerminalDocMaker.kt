package com.github.oowekyala.ijcc.ide.quickdoc

import com.github.oowekyala.ijcc.lang.model.RegexKind
import com.github.oowekyala.ijcc.lang.model.Token
import com.github.oowekyala.ijcc.ide.quickdoc.HtmlUtil.angles
import com.github.oowekyala.ijcc.ide.quickdoc.HtmlUtil.bold
import com.github.oowekyala.ijcc.ide.quickdoc.JccDocUtil.buildQuickDoc
import com.github.oowekyala.ijcc.lang.psi.*
import com.github.oowekyala.ijcc.util.foreachAndBetween
import com.intellij.codeInsight.documentation.DocumentationManager
import org.intellij.lang.annotations.Language
import org.jetbrains.annotations.TestOnly

/**
 * @author Clément Fournier
 * @since 1.0
 */
object JccTerminalDocMaker {


    @Language("HTML")
    fun makeDoc(token: Token): String = makeDocImpl(
        name = token.name,
        kind = token.regexKind,
        isExplicit = token.isExplicit,
        states = token.lexicalStatesOrEmptyForAll
    ) {
        token.regularExpression?.accept(RegexDocVisitor(it))
    }


    @TestOnly
    fun makeDocImpl(name: String?,
                             kind: RegexKind,
                             isExplicit: Boolean,
                             states: List<String>,
                             expansion: (StringBuilder) -> Unit) = buildQuickDoc {
        definition {
            val nameOrNot = name?.let { bold(angles(it)) } ?: "(unnamed)"

            val label = if (isExplicit) nameOrNot else "$nameOrNot " + bold("(implicit)")

            "$kind\t$label"
        }

        sections {
            section("Lexical states") {
                states.let {
                    if (it.isEmpty()) "All" else it.joinToString(separator = ", ")
                }
            }
            buildSection("Expansion") {
                expansion(this)
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
}