package com.github.oowekyala.ijcc.model

import com.github.oowekyala.ijcc.lang.psi.JccOptionSection
import com.github.oowekyala.ijcc.lang.psi.JccParserDeclaration

/**
 * Toplevel model object representing the options bundle
 * pertaining to a grammar.
 *
 * @author Clément Fournier
 * @since 1.0
 */
class JavaccConfig(private val options: JccOptionSection?, private val parserDeclaration: JccParserDeclaration) {


    // TODO parse from the PARSER def
    val parserPackage: String = packageRegex.find(parserDeclaration.text)?.groups?.get(1)?.value ?: ""

    val nodePackage: String get() = getOptionValueOrDefault(JjtOption.NODE_PACKAGE)

    val outputDirectory: String get() = getOptionValueOrDefault(JccOption.OUTPUT_DIRECTORY)

    val isDefaultVoid: Boolean get() = getOptionValueOrDefault(JjtOption.NODE_DEFAULT_VOID)

    val nodePrefix: String get() = getOptionValueOrDefault(JjtOption.NODE_PREFIX)

    val lookahead: Int get() = getOptionValueOrDefault(JccOption.LOOKAHEAD)

    private fun <T : Any> getOptionValueOrDefault(genericOption: GenericOption<T>): T =
            options?.getOverriddenOptionValue(genericOption) ?: genericOption.getDefaultValue(this)

    companion object {
        val packageRegex = Regex("\\bpackage\\s+([.\\w]+)")


    }
}