package com.github.oowekyala.ijcc.reference

import com.github.oowekyala.ijcc.lang.psi.JccIdentifier
import com.github.oowekyala.ijcc.lang.psi.JccNamedRegularExpression
import com.github.oowekyala.ijcc.lang.psi.JccNonTerminalProduction
import com.intellij.psi.PsiElement
import com.intellij.psi.ResolveState
import com.intellij.psi.scope.PsiScopeProcessor


sealed class JccScopeProcessor(val searchedName: String, private val isTerminal: Boolean) :
    PsiScopeProcessor {


    private var foundIdentifier: JccIdentifier? = null

    override fun execute(element: PsiElement, state: ResolveState): Boolean {
        if (!isTerminal && element is JccNonTerminalProduction && element.name == searchedName) {
            foundIdentifier = element.nameIdentifier
            return false
        }

        if (isTerminal && element is JccNamedRegularExpression && element.name == searchedName) {
            foundIdentifier = element.nameIdentifier
            return false
        }

        return true
    }

    fun result(): JccIdentifier? = foundIdentifier
}

class TerminalScopeProcessor(searchedName: String, val isRegexContext: Boolean) : JccScopeProcessor(searchedName, true)
class NonTerminalScopeProcessor(searchedName: String) : JccScopeProcessor(searchedName, false)