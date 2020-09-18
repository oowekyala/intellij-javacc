package com.github.oowekyala.ijcc.lang.psi

import com.github.oowekyala.ijcc.lang.model.GenericOption
import com.github.oowekyala.ijcc.lang.model.InlineGrammarOptions
import com.github.oowekyala.ijcc.lang.model.JccOptionType
import com.intellij.psi.PsiElement

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

val JccOptionBinding.namingLeaf: PsiElement
    get() = node.firstChildNode.psi!!


val JccOptionBinding.modelOption: GenericOption<*>?
    get() = InlineGrammarOptions.knownOptions[name]

/** Returns true if types match. */
fun <T : Any> JccOptionBinding.matchesType(expectedType: JccOptionType<T>): Boolean =
    expectedType.projection == optionValue?.optionType

/** Returns the string value for presentation. */
val JccOptionBinding.stringValue: String
    get() = optionValue?.stringValue ?: ""
