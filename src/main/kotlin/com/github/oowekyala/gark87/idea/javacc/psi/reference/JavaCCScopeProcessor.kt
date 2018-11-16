package com.github.oowekyala.gark87.idea.javacc.psi.reference

import com.github.oowekyala.gark87.idea.javacc.psi.*
import com.intellij.openapi.util.Key
import com.intellij.psi.PsiElement
import com.intellij.psi.ResolveState
import com.intellij.psi.scope.PsiScopeProcessor
import java.util.*

/**
 * @author gark87
 */
open class JavaCCScopeProcessor(private val myTypes: EnumSet<DeclarationType>) : PsiScopeProcessor {
    protected val candidates = ArrayList<Identifier>()

    override fun execute(element: PsiElement, resolveState: ResolveState): Boolean {
        val declType = findDeclarationType(element)
        if (!myTypes.contains(declType)) {
            return false
        }
        val decl = element as JavaccDeclarationElement
        val identifier = decl.identifier
        if (identifier != null && isValid(decl)) {
            this.candidates.add(identifier)
            return !keepLooking(decl)
        }
        return false
    }

    protected open fun keepLooking(decl: JavaccDeclarationElement): Boolean = true

    protected open fun isValid(decl: JavaccDeclarationElement): Boolean = true

    private fun findDeclarationType(element: PsiElement): DeclarationType {
        if (element is VariableDeclarator || element is FormalParameter) {
            return DeclarationType.VARIABLE
        }
        return if (element is RegexpSpec) {
            DeclarationType.TOKEN
        } else DeclarationType.NONTERMINAL
    }

    override fun <T> getHint(tKey: Key<T>): T? = null

    override fun handleEvent(event: PsiScopeProcessor.Event, o: Any?) {}

    fun getCandidates(): Array<Identifier> = candidates.toTypedArray()

    enum class DeclarationType {
        NONTERMINAL, TOKEN, VARIABLE
    }

    companion object {
        val NONTERMINAL = EnumSet.of(DeclarationType.NONTERMINAL)
        val NONTERMINAL_OR_VAR = EnumSet.of(DeclarationType.NONTERMINAL, DeclarationType.VARIABLE)
        val NONTERMINAL_OR_TOKEN = EnumSet.of(DeclarationType.NONTERMINAL, DeclarationType.TOKEN)
        val TOKEN = EnumSet.of(DeclarationType.TOKEN)
        val VARIABLE = EnumSet.of(DeclarationType.VARIABLE)
    }
}
