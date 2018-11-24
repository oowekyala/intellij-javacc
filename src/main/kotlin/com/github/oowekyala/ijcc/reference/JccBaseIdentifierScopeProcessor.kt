package com.github.oowekyala.ijcc.reference

import com.github.oowekyala.ijcc.lang.psi.JccIdentifier
import com.github.oowekyala.ijcc.lang.psi.JccIdentifierOwner
import com.github.oowekyala.ijcc.lang.psi.JccNamedRegularExpression
import com.github.oowekyala.ijcc.lang.psi.JccNonTerminalProduction
import com.intellij.psi.PsiElement
import com.intellij.psi.ResolveState
import com.intellij.psi.scope.PsiScopeProcessor


sealed class JccBaseIdentifierScopeProcessor(val searchedName: String) : PsiScopeProcessor {

    var result: JccIdentifier? = null
        protected set(value) {
            field = value
        }

    override fun execute(element: PsiElement, state: ResolveState): Boolean {
        if (element is JccIdentifierOwner && matches(element)) {
            result = element.nameIdentifier
            return false
        }
        return true
    }

    protected abstract fun matches(psiElement: JccIdentifierOwner): Boolean


}

class TerminalScopeProcessor(searchedName: String, val isRegexContext: Boolean)
    : JccBaseIdentifierScopeProcessor(searchedName) {


    override fun matches(element: JccIdentifierOwner): Boolean =
            element is JccNamedRegularExpression && element.name == searchedName
}

class NonTerminalScopeProcessor(searchedName: String) : JccBaseIdentifierScopeProcessor(searchedName) {

    override fun matches(element: JccIdentifierOwner): Boolean =
            element is JccNonTerminalProduction && element.name == searchedName
}