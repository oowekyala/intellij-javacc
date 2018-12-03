package com.github.oowekyala.ijcc.insight.model

/**
 * @author Clément Fournier
 * @since 1.0
 */
interface GenericOption<T : Any> {

    val type: JccOptionType<T>

    val name: String

    val staticDefaultValue: T?

    fun getDefaultValue(config: JavaccConfig): T =
            staticDefaultValue ?: throw UnsupportedOperationException("Unimplemented!")

}