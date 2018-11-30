// This is a generated file. Not intended for manual editing.
package com.github.oowekyala.ijcc.lang.psi

import com.github.oowekyala.ijcc.model.GenericOption
import com.github.oowekyala.ijcc.model.JavaccConfig

interface JccOptionSection : JavaccPsiElement {

    val optionBindingList: List<JccOptionBinding>

    @JvmDefault
    fun <T> getOverriddenOptionValue(genericOption: GenericOption<T>): T? =
            optionBindingList.firstOrNull { it.name == genericOption.name }?.stringValue?.let {
                genericOption.type.parseStringValue(it)
            }

}
