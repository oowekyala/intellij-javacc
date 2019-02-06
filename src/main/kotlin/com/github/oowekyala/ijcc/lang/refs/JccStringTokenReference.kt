package com.github.oowekyala.ijcc.lang.refs

import com.github.oowekyala.ijcc.insight.model.ExplicitToken
import com.github.oowekyala.ijcc.insight.model.Token
import com.github.oowekyala.ijcc.lang.psi.JccLiteralRegexpUnit
import com.github.oowekyala.ijcc.lang.psi.JccRegexprSpec
import com.github.oowekyala.ijcc.lang.psi.enclosingToken
import com.github.oowekyala.ijcc.lang.psi.isPrivate
import com.intellij.psi.PsiElementResolveResult
import com.intellij.psi.PsiPolyVariantReferenceBase
import com.intellij.psi.ResolveResult

/**
 * Reference from a literal regexp to a regexp spec covering its match.
 *
 * If there exists a regexp spec defined as *exactly* this string, the reference
 * points to that regex. Otherwise a new string token is synthesized by JavaCC
 * (see documentation on [Token]), in which case this reference returns null.
 *
 * @author Clément Fournier
 * @since 1.0
 */
class JccStringTokenReference(element: JccLiteralRegexpUnit) :
    PsiPolyVariantReferenceBase<JccLiteralRegexpUnit>(element) {


    override fun resolve(): JccRegexprSpec? = super.resolve() as JccRegexprSpec?

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        val file = element.containingFile
        val grammar = file.lexicalGrammar

        val consideredState = element.enclosingToken.lexicalStateNames

        val matchedTokens = grammar.lexicalStates
            .asSequence()
            .filter { consideredState.contains(it.name) }
            .mapNotNull { it.matchLiteral(element) } // this finds only string tokens
            .filterIsInstance<ExplicitToken>()
            .map { PsiElementResolveResult(it.spec) }
            .distinct()

        return matchedTokens.toList().toTypedArray()
    }

    override fun getVariants(): Array<Any> =
            element.containingFile
                .globalNamedTokens
                .filter { !it.isPrivate }.toList().toTypedArray()

}