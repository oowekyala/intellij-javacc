package com.github.oowekyala.ijcc.ide.refs

import com.github.oowekyala.ijcc.ide.inspections.TokenCanNeverBeMatchedInspection
import com.github.oowekyala.ijcc.lang.model.ExplicitToken
import com.github.oowekyala.ijcc.lang.model.LexicalState
import com.github.oowekyala.ijcc.lang.model.SyntheticToken
import com.github.oowekyala.ijcc.lang.model.Token
import com.github.oowekyala.ijcc.lang.psi.JccLiteralRegexUnit
import com.github.oowekyala.ijcc.lang.psi.JccRegexExpansionUnit
import com.github.oowekyala.ijcc.lang.psi.JccRegexSpec
import com.github.oowekyala.ijcc.lang.psi.enclosingToken
import com.github.oowekyala.ijcc.util.JavaccIcons
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementResolveResult
import com.intellij.psi.PsiPolyVariantReferenceBase
import com.intellij.psi.ResolveResult

/**
 * Reference from a literal regex to a regex spec covering its match.
 *
 * If there exists a [JccRegexSpec] defined as *exactly* this string, the reference
 * points to that regex (there may be several). Otherwise a new string token is synthesized
 * by JavaCC (see documentation on [Token]). Then this reference's [resolve] points to the
 * [JccRegexExpansionUnit] generating it.
 *
 * This reference can be used both to find all *string tokens* that match
 * exactly the literal as described above, but also to find all tokens (in a more general way),
 * that will match the literal (which [TokenCanNeverBeMatchedInspection] is dependent on).
 *
 * @author Clément Fournier
 * @since 1.0
 */
class JccStringTokenReference(element: JccLiteralRegexUnit) :
    PsiPolyVariantReferenceBase<JccLiteralRegexUnit>(element) {

    fun resolveToken(exact: Boolean): Token? = multiResolveToken(exact).firstOrNull()

    override fun resolve(): PsiElement? = resolveToken(exact = true)?.psiElement

    /**
     * Resolves all tokens that match this literal. If [exact], only exact
     * string literals will be returned (meaning [Token.asStringToken] is non-null).
     */
    fun multiResolveToken(exact: Boolean): List<Token> {
        val file = element.containingFile
        val grammar = file.lexicalGrammar

        val consideredStates = element.enclosingToken.lexicalStatesOrEmptyForAll

        return grammar.lexicalStates
            .asSequence()
            .filter { consideredStates.isEmpty() || consideredStates.contains(it.name) }
            .mapNotNull { it.matchLiteral(element, exact) } // this finds only string tokens
            .toList()
    }


    /**
     * The returned array contains either a single [JccRegexExpansionUnit] ([SyntheticToken])
     * or one or more [JccRegexSpec] ([ExplicitToken]). This is because a synthetic token is
     * only generated if there is no matching explicit one.
     */
    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> =
            multiResolveToken(exact = true)
                .mapNotNull { it.psiElement }
                .map { PsiElementResolveResult(it, true) }
                .toList()
                .toTypedArray()

    /**
     * Enables autocompletion.
     */
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