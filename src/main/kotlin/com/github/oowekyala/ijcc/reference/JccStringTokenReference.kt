package com.github.oowekyala.ijcc.reference

import com.github.oowekyala.ijcc.lang.psi.JccLiteralRegularExpression
import com.github.oowekyala.ijcc.lang.psi.JccRegexprSpec
import com.intellij.psi.PsiPolyVariantReferenceBase
import com.intellij.psi.ResolveResult

/**
 * Reference from a literal regexp to a regexp spec covering its match.
 *
 * @author Clément Fournier
 * @since 1.0
 */
class JccStringTokenReference(element: JccLiteralRegularExpression) :
    PsiPolyVariantReferenceBase<JccLiteralRegularExpression>(element) {


    override fun multiResolve(incompleteCode: Boolean): Array<MyResolveResult> {
        val file = element.containingFile

        val grammar = file.lexicalGrammar

        val matchedTokens = grammar.lexicalStates
            .asSequence()
            .map { it.matchLiteral(element) }
            .filterNotNull()
            .map { MyResolveResult(it.regexprSpec) }
            .distinct()

        return matchedTokens.toList().toTypedArray()
    }

    override fun getVariants(): Array<Any> =
            element.containingFile
                .globalNamedTokens
                .filter { !it.isPrivate }.toList().toTypedArray()

    companion object {
        class MyResolveResult(private val regexprSpec: JccRegexprSpec) : ResolveResult {
            override fun getElement(): JccRegexprSpec = regexprSpec

            override fun isValidResult(): Boolean = true
        }
    }
}