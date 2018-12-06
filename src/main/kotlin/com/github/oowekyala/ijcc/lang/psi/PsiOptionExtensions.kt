package com.github.oowekyala.ijcc.lang.psi

import com.github.oowekyala.ijcc.insight.model.GenericOption
import com.github.oowekyala.ijcc.insight.model.JavaccConfig
import com.github.oowekyala.ijcc.insight.model.JccOptionType

/**
 * Gets the binding for the given option value if present.
 */
fun <T : Any> JccOptionSection.getBindingFor(genericOption: GenericOption<T>): JccOptionBinding? =
        optionBindingList.firstOrNull { it.name == genericOption.name }


/** Returns the string value for presentation. */
val JccOptionValue.stringValue: String
    get() {
        val int = integerLiteral
        val string = stringLiteral
        val bool = booleanLiteral

        return when {
            int != null    -> int.text
            string != null -> string.text.removeSurrounding("\"")
            bool != null   -> bool.text
            else           -> "??"
        }
    }


val JccOptionValue.optionType: JccOptionType.BaseOptionType<*>
    get() {
        val int = integerLiteral
        val string = stringLiteral
        val bool = booleanLiteral

        return when {
            int != null    -> JccOptionType.BaseOptionType.INTEGER
            string != null -> JccOptionType.BaseOptionType.STRING
            bool != null   -> JccOptionType.BaseOptionType.BOOLEAN
            else           -> throw IllegalStateException()
        }
    }


val JccOptionBinding.modelOption: GenericOption<*>?
    get() = JavaccConfig.knownOptions[name]

/** Returns true if types match. */
fun <T : Any> JccOptionBinding.matchesType(expectedType: JccOptionType<T>): Boolean =
        expectedType.projection == optionValue?.optionType

/** Returns the string value for presentation. */
val JccOptionBinding.stringValue: String
    get() = optionValue?.stringValue ?: ""