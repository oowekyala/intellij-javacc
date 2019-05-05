package com.github.oowekyala.ijcc.lang.injection

import com.github.oowekyala.ijcc.lang.psi.JccGrammarFileRoot
import com.intellij.openapi.util.Key


private fun getLinearStructureFor(grammarFileRoot: JccGrammarFileRoot): LinearInjectedStructure =
    TreeLineariserVisitor.linearise(InjectedTreeBuilderVisitor.getInjectedSubtreeFor(grammarFileRoot))

private val LinearStructureKey = Key.create<LinearInjectedStructure>("linearInjectedStructure")

val JccGrammarFileRoot.linearInjectedStructure: LinearInjectedStructure
    get() = getUserData(LinearStructureKey)
        ?: getLinearStructureFor(this)
            .also {
                putUserData(LinearStructureKey, it)
            }

