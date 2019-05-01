package com.github.oowekyala.ijcc.lang.model

import com.github.oowekyala.ijcc.ide.quickdoc.HtmlUtil
import com.github.oowekyala.ijcc.ide.quickdoc.JccDocUtil
import com.github.oowekyala.ijcc.lang.psi.JccOptionBinding
import com.github.oowekyala.ijcc.lang.psi.matchesType
import com.github.oowekyala.ijcc.lang.psi.stringValue
import com.github.oowekyala.ijcc.util.ResourcePrefix
import java.io.IOException

/**
 * Generic option for JavaCC or its preprocessors.
 * All options are available in [InlineGrammarOptions.knownOptions]
 *
 * @param T the type of literal to expect
 *
 * @author Clément Fournier
 * @since 1.0
 */
abstract class GenericOption<T : Any>(
    /** Type of value the option expects. */
    val expectedType: JccOptionType<T>,
    /**
     * Static default value used by JavaCC to represent a
     * default. See [getActualValue].
     */
    val staticDefaultValue: T?,
    /**
     * Max supported grammar nature.
     */
    val supportedNature: GrammarNature) {

    // TODO maybe support a "since version" attribute

    /** Name of this option. */
    abstract val name: String

    /** Gets the value of this option from an binding. If it's null then the default value is used. */
    open fun getValue(optionBinding: JccOptionBinding?, config: IGrammarOptions): T =
        optionBinding
            ?.takeIf { it.matchesType(expectedType) }
            ?.let { expectedType.projection.parseStringValue(optionBinding.stringValue) }
            .let { getActualValue(it, config) }


    /**
     * Gets the actual value used by JavaCC. E.g. the overridden value
     * may match the [staticDefaultValue] used by JavaCC, which JavaCC
     * interprets as meaning something else, eg defaulting to another
     * option, or some other thing.
     */
    open fun getActualValue(overriddenValue: T?, config: IGrammarOptions): T = when (overriddenValue) {
        null, staticDefaultValue -> contextualDefaultValue(config)
        else                     -> overriddenValue
    }


    /**
     * This is the actual default value used by JavaCC, which may depend
     * on other option bindings.
     *
     * Must be implemented if [staticDefaultValue] is null.
     */
    open fun contextualDefaultValue(config: IGrammarOptions): T =
        staticDefaultValue ?: TODO("Should have been implemented!")


    /**
     * Returns the documentable description of the option if it could be found.
     */
    val description: String? by lazy {

        // link options between them
        fun String.escapeMarkup(): String =
            replace(OptionLinkRegex) {
                val name = it.groupValues[1]
                HtmlUtil.psiLink(linkTarget = JccDocUtil.linkRefToOption(name), linkText = name)
            }

        try {
            // try jjtree first
            val resource = javaClass.classLoader.getResource("$ResourcePrefix/optionDescriptions/jjtree/$name.html")
                ?: javaClass.classLoader.getResource("$ResourcePrefix/optionDescriptions/$name.html")

            resource?.readText()?.escapeMarkup()
        } catch (e: IOException) {
            null
        }
    }

    companion object {
        private val OptionLinkRegex = Regex("""\{\s*option_link\s*(\w+)\s*}""")
    }
}
