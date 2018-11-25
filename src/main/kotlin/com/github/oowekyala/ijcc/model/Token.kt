package com.github.oowekyala.ijcc.model

import com.github.oowekyala.ijcc.inspections.LoggerCompanion
import com.github.oowekyala.ijcc.lang.psi.JccNamedRegularExpression
import com.github.oowekyala.ijcc.lang.psi.JccRegexprSpec
import com.github.oowekyala.ijcc.lang.psi.toPattern

/**
 * @author Clément Fournier
 * @since 1.0
 */
data class Token(val regexKind: RegexKind, val regexprSpec: JccRegexprSpec) {


    /** Returns the regex */
    val pattern: Regex? = regexprSpec.regularExpression.toPattern()
    val prefixPattern: Regex? = regexprSpec.regularExpression.toPattern(prefixMatch = true)

    val name: String? = regexprSpec.regularExpression.let { it as? JccNamedRegularExpression }?.name

    companion object : LoggerCompanion
}