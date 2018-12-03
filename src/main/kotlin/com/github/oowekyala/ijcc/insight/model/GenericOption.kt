package com.github.oowekyala.ijcc.insight.model

import com.github.oowekyala.ijcc.lang.psi.JccOptionBinding

/**
 * Generic option for JavaCC or its preprocessors.
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
    val staticDefaultValue: T?) {

    // TODO maybe support a "since version" attribute

    /** Name of this option. */
    abstract val name: String

    /** Gets the value of this option from an binding. If it's null then the default value is used. */
    open fun getValue(optionBinding: JccOptionBinding?, config: JavaccConfig): T =
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
    open fun getActualValue(overriddenValue: T?, config: JavaccConfig): T = when (overriddenValue) {
        null, staticDefaultValue -> defaultValueFallback(config)
        else                     -> overriddenValue
    }

    /** Must be implemented if [staticDefaultValue] is null. */
    protected open fun defaultValueFallback(config: JavaccConfig): T =
            staticDefaultValue ?: TODO("Should have been implemented!")

}