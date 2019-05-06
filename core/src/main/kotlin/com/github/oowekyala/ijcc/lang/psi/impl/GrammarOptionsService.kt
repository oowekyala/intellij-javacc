package com.github.oowekyala.ijcc.lang.psi.impl

import com.github.oowekyala.ijcc.lang.model.IGrammarOptions
import com.github.oowekyala.ijcc.lang.model.InlineGrammarOptions
import com.intellij.openapi.components.ServiceManager

/**
 * @author Clément Fournier
 */
open class GrammarOptionsService {


    open fun buildOptions(jccFileImpl: JccFileImpl): IGrammarOptions =
        InlineGrammarOptions(jccFileImpl)


    companion object {
        @JvmStatic
        fun getInstance(): GrammarOptionsService =
            ServiceManager.getService(GrammarOptionsService::class.java) ?: DEFAULT

        private val DEFAULT = GrammarOptionsService()
    }


}
