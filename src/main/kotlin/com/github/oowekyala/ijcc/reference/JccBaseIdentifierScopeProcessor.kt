package com.github.oowekyala.ijcc.reference

import com.github.oowekyala.ijcc.lang.psi.JccNonTerminalProduction
import com.github.oowekyala.ijcc.lang.psi.JccRegexprSpec
import com.intellij.psi.PsiElement
import com.intellij.psi.ResolveState
import com.intellij.psi.scope.PsiScopeProcessor


class TerminalScopeProcessor(val searchedName: String, val isRegexContext: Boolean) : PsiScopeProcessor {

    var result: JccRegexprSpec? = null
        private set(value) {
            field = value
        }

    override fun execute(element: PsiElement, state: ResolveState): Boolean {
        if (element is JccRegexprSpec && element.name == searchedName) {
            result = element
            return false
        }
        return true
    }

}

class NonTerminalScopeProcessor(val searchedName: String) : PsiScopeProcessor {

    var result: JccNonTerminalProduction? = null
        private set(value) {
            field = value
        }

    override fun execute(element: PsiElement, state: ResolveState): Boolean {
        if (element is JccNonTerminalProduction && element.name == searchedName) {
            result = element
            return false
        }
        return true
    }
}