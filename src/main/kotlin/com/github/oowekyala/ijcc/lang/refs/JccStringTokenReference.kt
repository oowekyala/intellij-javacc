package com.github.oowekyala.ijcc.lang.refs

import com.github.oowekyala.ijcc.insight.model.ExplicitToken
import com.github.oowekyala.ijcc.insight.model.LexicalState
import com.github.oowekyala.ijcc.insight.model.Token
import com.github.oowekyala.ijcc.lang.psi.JccLiteralRegexpUnit
import com.github.oowekyala.ijcc.lang.psi.JccRegexprSpec
import com.github.oowekyala.ijcc.lang.psi.enclosingToken
import com.github.oowekyala.ijcc.util.JavaccIcons
import com.intellij.codeInsight.lookup.LookupElementBuilder
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

        val consideredStates = element.enclosingToken.lexicalStatesOrEmptyForAll

        val matchedTokens = grammar.lexicalStates
            .asSequence()
            .filter { consideredStates.contains(it.name) }
            .mapNotNull { it.matchLiteral(element) } // this finds only string tokens
            .filterIsInstance<ExplicitToken>()
            .map { PsiElementResolveResult(it.spec) }
            .distinct()

        return matchedTokens.toList().toTypedArray()
    }

    private val variants = element.containingFile
        .lexicalGrammar
        .allTokens
        .filter { !it.isPrivate }
        .mapNotNull { token ->
            val asString = token.asStringToken ?: return@mapNotNull null

            LookupElementBuilder
                .create(asString.text)
                .withIcon(JavaccIcons.TOKEN)
                .withTypeText(token.let {
                    buildString {
                        if (it.lexicalStatesOrEmptyForAll != LexicalState.JustDefaultState) {
                            // <A, B>
                            it.lexicalStatesOrEmptyForAll.joinTo(
                                this,
                                separator = ", ",
                                prefix = "<",
                                postfix = ">"
                            )
                        }
                        it.lexicalStateTransition?.let {
                            append(" -> ")
                            append(it)
                        }
                    }
                }, true)
        }
        .toList().toTypedArray<Any>()

    override fun getVariants(): Array<Any> = variants

}