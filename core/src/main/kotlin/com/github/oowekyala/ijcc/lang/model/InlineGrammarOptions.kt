package com.github.oowekyala.ijcc.lang.model

import com.github.oowekyala.ijcc.lang.psi.JccFile
import com.github.oowekyala.ijcc.lang.psi.JccOptionBinding
import com.github.oowekyala.ijcc.lang.psi.getBindingFor


/**
 * Toplevel model object representing the options bundle
 * pertaining to a grammar.
 *
 * @author Clément Fournier
 * @since 1.0
 */
class InlineGrammarOptions(file: JccFile) : BaseCachedModelObject(file), IGrammarOptions {

    override val inlineBindings: InlineGrammarOptions = this

    override val isTrackTokens: Boolean by lazy { getOptionValueOrDefault(JjtOption.TRACK_TOKENS) }

    val jjtNodeFactory: String by lazy {
        getOptionValueOrDefault(JjtOption.NODE_FACTORY)
    }

    val jjtMulti: Boolean by lazy {
        getOptionValueOrDefault(JjtOption.MULTI)
    }

    val jjtNodeClass: String by lazy {
        getOptionValueOrDefault(JjtOption.NODE_CLASS)
    }

    override val grammarName: String? = null

    override val nodeTakesParserArg: Boolean by lazy {
        getOptionValueOrDefault(JjtOption.NODE_USES_PARSER)
    }

    val jjtCustomNodeHooks: Boolean by lazy {
        getOptionValueOrDefault(JjtOption.NODE_SCOPE_HOOK)
    }

    val isPublicSupportClasses : Boolean by lazy {
        getOptionValueOrDefault(JccOption.SUPPORT_CLASS_VISIBILITY_PUBLIC)
    }

    val isLegacyGen: Boolean by lazy {
        getOptionValueOrDefault(JccOption.JAVA_TEMPLATE_TYPE).equals("classic", ignoreCase = true)
    }


    val rootNodeClass : String by lazy { getOptionValueOrDefault(JjtOption.NODE_CLASS) }

    override val nodePackage: String by lazy { getOptionValueOrDefault(JjtOption.NODE_PACKAGE) }

    val outputDirectory: String by lazy { getOptionValueOrDefault(JccOption.OUTPUT_DIRECTORY) }

    override val isDefaultVoid: Boolean by lazy { getOptionValueOrDefault(JjtOption.NODE_DEFAULT_VOID) }

    override val nodePrefix: String by lazy { getOptionValueOrDefault(JjtOption.NODE_PREFIX) }

    val lookahead: Int by lazy { getOptionValueOrDefault(JccOption.LOOKAHEAD) }

    val allOptionsBindings: List<JccOptionBinding> = file.options?.optionBindingList ?: emptyList()

    private fun <T : Any> getOptionValueOrDefault(genericOption: GenericOption<T>): T =
        genericOption.getValue(file.options?.getBindingFor(genericOption), this)

    companion object {

        /** Indexes all known JavaCC or JJTree options by their name.*/
        val knownOptions: Map<String, GenericOption<*>> =
            JccOption.values.plus(JjtOption.values).associateBy { it.name }

        private val packageRegex = Regex("\\bpackage\\s+([.\\w]+)")

    }
}
