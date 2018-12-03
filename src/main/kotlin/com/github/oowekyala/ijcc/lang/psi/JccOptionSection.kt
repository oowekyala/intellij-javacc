// This is a generated file. Not intended for manual editing.
package com.github.oowekyala.ijcc.lang.psi

import com.github.oowekyala.ijcc.insight.model.GenericOption

interface JccOptionSection : JavaccPsiElement {

    val optionBindingList: List<JccOptionBinding>

    /**
     * Gets the binding for the given option value if present.
     */
    @JvmDefault
    fun <T : Any> getBindingFor(genericOption: GenericOption<T>): JccOptionBinding? =
            optionBindingList.firstOrNull { it.name == genericOption.name }

}
