package com.github.oowekyala.ijcc.ide.quickdoc

import com.github.oowekyala.ijcc.ide.quickdoc.HtmlUtil.bold
import com.github.oowekyala.ijcc.ide.quickdoc.HtmlUtil.psiLink
import com.github.oowekyala.ijcc.ide.quickdoc.JccDocUtil.buildQuickDoc
import com.github.oowekyala.ijcc.lang.model.LexicalState
import org.jetbrains.annotations.TestOnly

/**
 * @author Clément Fournier
 * @since 1.2
 */
object JccLexicalStateDocMaker {

    fun makeDoc(lexicalState: LexicalState): String =
        makeDocImpl(
            lexicalStateName = lexicalState.name,
            successors = lexicalState.successors.map { it.name },
            predecessors = lexicalState.predecessors.map { it.name },
            numTokens = lexicalState.tokens.size
        )

    @TestOnly
    fun makeDocImpl(lexicalStateName: String,
                    successors: List<String>,
                    predecessors: List<String>,
                    numTokens: Int) = buildQuickDoc {
        definition {
            "Lexical state " + bold(lexicalStateName)
        }

        sections {
            buildSection("Successors") {
                successors.joinTo(this) {
                    psiLink(
                        linkTextUnescaped = it,
                        linkTarget = JccDocUtil.linkRefToLexicalState(it)
                    )
                }
            }
            buildSection("Predecessors") {
                predecessors.joinTo(this) {
                    psiLink(
                        linkTextUnescaped = it,
                        linkTarget = JccDocUtil.linkRefToLexicalState(it)
                    )
                }
            }

            section("Size in tokens") { numTokens.toString() }
        }
    }
}
