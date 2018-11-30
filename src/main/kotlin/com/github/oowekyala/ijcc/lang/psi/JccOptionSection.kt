// This is a generated file. Not intended for manual editing.
package com.github.oowekyala.ijcc.lang.psi

import com.github.oowekyala.ijcc.model.GenericOption

interface JccOptionSection : JavaccPsiElement {

    val optionBindingList: List<JccOptionBinding>

    /**
     * Gets the overriden option value if present. Returns null if the type is invalid,
     * or the option is not overridden.
     */
    @JvmDefault
    fun <T : Any> getOverriddenOptionValue(genericOption: GenericOption<T>): T? =
            optionBindingList.firstOrNull { it.name == genericOption.name }?.getValue(genericOption.type)

}
