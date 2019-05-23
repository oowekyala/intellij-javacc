package com.github.oowekyala.ijcc.ide.quickdoc

import com.github.oowekyala.ijcc.ide.quickdoc.HtmlUtil.angles
import com.github.oowekyala.ijcc.ide.quickdoc.HtmlUtil.bold
import com.github.oowekyala.ijcc.ide.quickdoc.HtmlUtil.psiLink
import com.github.oowekyala.ijcc.ide.quickdoc.JccDocUtil.buildQuickDoc
import com.github.oowekyala.ijcc.ide.quickdoc.JccDocUtil.linkRefToLexicalState
import com.github.oowekyala.ijcc.lang.model.RegexKind
import com.github.oowekyala.ijcc.lang.model.Token
import com.github.oowekyala.ijcc.lang.psi.*
import com.github.oowekyala.ijcc.util.foreachAndBetween
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
        isIgnoreCase = token.isIgnoreCase,
        states = token.lexicalStatesOrEmptyForAll
    ) {
        token.regularExpression?.accept(RegexDocVisitor(it))
    }


    @TestOnly
    fun makeDocImpl(name: String?,
                    kind: RegexKind,
                    isExplicit: Boolean,
                    isIgnoreCase: Boolean,
                    states: List<String>,
                    expansion: (StringBuilder) -> Unit) = buildQuickDoc {
        definition {
            val nameOrNot = name?.let { bold(angles(it)) } ?: "(unnamed)"

            val label = if (isExplicit) nameOrNot else "$nameOrNot " + bold("(implicit)")

            "$kind\t$label"
        }

        sections {
            buildSection("Expansion") {
                expansion(this)
            }
            section("Case-sensitive") { if (isIgnoreCase) "false" else "true" }
            buildSection("Lexical states") {
                states.let {
                    if (it.isEmpty()) append("All")
                    else it.joinTo(this) { name ->
                        val ref = linkRefToLexicalState(name)
                        psiLink(linkTarget = ref, linkText = name)
                    }
                }
            }
        }
    }


    /** Limit to the number of character descriptors expanded. */
    private const val MaxChars = 10


    class RegexDocVisitor(private val sb: StringBuilder) : RegexLikeDFVisitor() {

        override fun visitLiteralRegexUnit(o: JccLiteralRegexUnit) {
            sb.append(HtmlUtil.escapeHtml(o.text))
        }

        override fun visitLiteralRegularExpression(o: JccLiteralRegularExpression) {
            val reffed = o.typedReference.resolveToken(exact = true)?.let { JccDocUtil.linkRefToToken(it) }
            if (reffed != null) {
                psiLink(builder = sb, linkTarget = reffed, linkText = HtmlUtil.escapeHtml(o.text))
            } else {
                o.unit.accept(this)
            }
        }

        override fun visitNamedRegularExpression(o: JccNamedRegularExpression) {
            o.regexElement?.accept(this)
        }

        override fun visitEofRegularExpression(o: JccEofRegularExpression) {
            sb.append("&lt;EOF&gt;")
        }

        override fun visitTokenReferenceRegexUnit(o: JccTokenReferenceRegexUnit) {
            val reffed = o.typedReference.resolveToken()

            // make the linktext be the literal if needed.
            val linkText = reffed?.asStringToken?.text?.let(HtmlUtil::escapeHtml) ?: angles(o.name!!)

            psiLink(builder = sb, linkTarget = reffed?.let { JccDocUtil.linkRefToToken(it) }, linkText = linkText)
        }

        override fun visitRefRegularExpression(o: JccRefRegularExpression) {
            o.unit.accept(this)
        }

        override fun visitContainerRegularExpression(o: JccContainerRegularExpression) {
            o.regexElement?.accept(this)
        }


        override fun visitRegexSequenceElt(o: JccRegexSequenceElt) {
            o.regexUnitList.foreachAndBetween({ sb.append(" ") }) { it.accept(this) }
        }

        override fun visitRegexAlternativeElt(o: JccRegexAlternativeElt) {
            o.regexElementList.foreachAndBetween({ sb.append(" | ") }) { it.accept(this) }
        }


        override fun visitCharacterListRegexUnit(o: JccCharacterListRegexUnit) {
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

        override fun visitParenthesizedRegexUnit(o: JccParenthesizedRegexUnit) {
            sb.append("( ")
            o.regexElement.accept(this)
            sb.append(" )")
            o.occurrenceIndicator?.run { sb.append(text) }
        }

        override fun visitCharacterDescriptor(o: JccCharacterDescriptor) {
            sb.append(o.text.replace("\\s*", "").let(HtmlUtil::escapeHtml))
        }
    }
}
