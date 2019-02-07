package com.github.oowekyala.ijcc.ide.highlight

import com.github.oowekyala.ijcc.lang.model.LexicalState
import com.github.oowekyala.ijcc.lang.model.Token
import com.github.oowekyala.ijcc.lang.psi.JccLiteralRegexUnit

/**
 * @author Clément Fournier
 * @since 1.1
 */
object JccErrorMessages {


    fun duplicateStringToken(regex: JccLiteralRegexUnit, state: LexicalState, token: Token): String {

        val statePart = state.name.takeUnless { it == LexicalState.DefaultStateName }?.let { " in state $it" }.orEmpty()

        return "Duplicate definition of string token ${regex.text} (" +
                when {
                    !token.isExplicit -> "implicitly defined" + token.line?.let { " at line $it" }.orEmpty() + ""
                    else              -> token.name?.let {
                        "see <$it>" + (", which is case-insensitive".takeIf { token.isIgnoreCase } ?: "")
                    } ?: "unnamed"
                } +
                ")" + statePart
    }


}