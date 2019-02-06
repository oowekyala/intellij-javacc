package com.github.oowekyala.ijcc.ide.refs

import com.github.oowekyala.ijcc.lang.psi.JccRegexSpec
import com.intellij.psi.PsiElement
import com.intellij.psi.ResolveState
import com.intellij.psi.scope.PsiScopeProcessor


class TerminalScopeProcessor(val searchedName: String) : PsiScopeProcessor {

    var result: JccRegexSpec? = null
        private set(value) {
            field = value
        }

    override fun execute(element: PsiElement, state: ResolveState): Boolean {
        if (element is JccRegexSpec && element.name == searchedName) {
            result = element
            return false
        }
        return true
    }

}

