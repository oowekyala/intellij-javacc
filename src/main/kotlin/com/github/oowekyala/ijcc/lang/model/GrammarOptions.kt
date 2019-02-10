package com.github.oowekyala.ijcc.lang.model

import com.github.oowekyala.ijcc.lang.psi.JccOptionSection
import com.github.oowekyala.ijcc.lang.psi.JccParserDeclaration
import com.github.oowekyala.ijcc.lang.psi.getBindingFor

/**
 * Toplevel model object representing the options bundle
 * pertaining to a grammar.
 *
 * @author Clément Fournier
 * @since 1.0
 */
class GrammarOptions(private val options: JccOptionSection?, private val parserDeclaration: JccParserDeclaration?) {

    val parserQualifiedName: String
        get() {
            val pack = parserPackage
            return if (pack.isEmpty()) parserSimpleName
            else "$pack.$parserSimpleName"
        }

    // TODO parse from the PARSER def
    val parserPackage: String  by lazy {
        parserDeclaration?.text?.let { packageRegex.find(it) }?.groups?.get(1)?.value ?: ""
    }
    val parserSimpleName: String  by lazy {
        parserDeclaration?.text?.let { classRegex.find(it) }?.groups?.get(1)?.value ?: ""
    }

    val nodePackage: String by lazy { getOptionValueOrDefault(JjtOption.NODE_PACKAGE) }

    val outputDirectory: String by lazy { getOptionValueOrDefault(JccOption.OUTPUT_DIRECTORY) }

    val isDefaultVoid: Boolean by lazy { getOptionValueOrDefault(JjtOption.NODE_DEFAULT_VOID) }

    val nodePrefix: String by lazy { getOptionValueOrDefault(JjtOption.NODE_PREFIX) }

    val lookahead: Int by lazy { getOptionValueOrDefault(JccOption.LOOKAHEAD) }

    private fun <T : Any> getOptionValueOrDefault(genericOption: GenericOption<T>): T =
        genericOption.getValue(options?.getBindingFor(genericOption), this)

    companion object {
        private val packageRegex = Regex("\\bpackage\\s+([.\\w]+)")
        private val classRegex = Regex("\\bclass\\s+(\\w+)")

        /** Indexes all known JavaCC or JJTree options by their name.*/
        val knownOptions: Map<String, GenericOption<*>> =
            JccOption.values.plus(JjtOption.values).associateBy { it.name }

    }
}
