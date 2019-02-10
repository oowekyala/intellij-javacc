package com.github.oowekyala.ijcc.ide.highlight

import com.github.oowekyala.ijcc.lang.model.LexicalState
import com.github.oowekyala.ijcc.lang.model.RegexKind
import com.github.oowekyala.ijcc.lang.model.Token
import com.github.oowekyala.ijcc.lang.psi.JccLiteralRegexUnit
import org.jetbrains.annotations.TestOnly

/**
 * @author Clément Fournier
 * @since 1.1
 */
object JccErrorMessages {


    fun duplicateStringToken(regex: JccLiteralRegexUnit, state: LexicalState, token: Token): String =
        duplicateStringTokenImpl(
            regexText = regex.text,
            stateName = state.name,
            tokenIsExplicit = token.isExplicit,
            tokenLine = token.line,
            tokenName = token.name,
            tokenIsIgnoreCase = token.isIgnoreCase
        )

    @TestOnly
    fun duplicateStringTokenImpl(regexText: String,
                                 stateName: String,
                                 tokenIsExplicit: Boolean,
                                 tokenLine: Int?,
                                 tokenName: String?,
                                 tokenIsIgnoreCase: Boolean): String {

        val statePart = stateName.takeUnless { it == LexicalState.DefaultStateName }?.let { " in state $it" }.orEmpty()

        return "Duplicate definition of string token $regexText (" +
            when {
                !tokenIsExplicit -> "implicitly defined" + tokenLine?.let { " at line $it" }.orEmpty() + ""
                else             -> tokenName?.let {
                    "see <$it>" + (", which is case-insensitive".takeIf { tokenIsIgnoreCase } ?: "")
                } ?: "unnamed"
            } +
            ")" + statePart
    }

    /**
     * JavaCC warning:
     * 'String "foo" can never be matched due to presence of more general (IGNORE_CASE) regular expression'
     */
    fun stringLiteralMatchedbyIgnoreCaseCannotBeUsedInBnf(supersedingName: String?): String =
        "String is matched by an IGNORE_CASE regular expression and should refer to the token by name " +
            (supersedingName?.let { "(<$it>)" } ?: "(unnamed!)")

    fun stringLiteralWithIgnoreCaseIsPartiallySuperceded(superseding: Token): String =
        stringLiteralWithIgnoreCaseIsPartiallySupercededImpl(
            supersedingName = superseding.name,
            supersedingText = superseding.asStringToken!!.text,
            supersedingLine = superseding.line!!
        )

    @TestOnly
    fun stringLiteralWithIgnoreCaseIsPartiallySupercededImpl(supersedingName: String?,
                                                             supersedingText: String,
                                                             supersedingLine: Int): String =
        "String with IGNORE_CASE is partially superseded by token${supersedingName?.let { " <$it>" }.orEmpty()} ($supersedingText) at line $supersedingLine. "


    fun stringLiteralIsNotToken(regexText: String, actualRegexKind: RegexKind): String =
        "String token $regexText has been defined as a $actualRegexKind token"

    fun stringLiteralIsPrivate(regexText: String): String =
        "String token $regexText has been defined as a private (#) regular expression"

    fun undefinedTokenName(name: String): String = "Undefined lexical token name \"$name\""

}