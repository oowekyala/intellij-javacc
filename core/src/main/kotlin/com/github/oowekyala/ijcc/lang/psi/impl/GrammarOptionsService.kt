package com.github.oowekyala.ijcc.lang.psi.impl

import com.github.oowekyala.ijcc.lang.model.IGrammarOptions
import com.github.oowekyala.ijcc.lang.model.InlineGrammarOptions
import com.intellij.openapi.project.Project

/**
 * @author Clément Fournier
 */
open class GrammarOptionsService {


    open fun buildOptions(jccFileImpl: JccFileImpl): IGrammarOptions =
        InlineGrammarOptions(jccFileImpl)

}

val Project.grammarOptionsService: GrammarOptionsService
    get() = getComponent(GrammarOptionsService::class.java)
