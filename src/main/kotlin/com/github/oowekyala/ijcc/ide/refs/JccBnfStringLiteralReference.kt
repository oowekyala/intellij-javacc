package com.github.oowekyala.ijcc.ide.refs

import com.github.oowekyala.ijcc.lang.model.LexicalState
import com.github.oowekyala.ijcc.lang.model.RegexKind
import com.github.oowekyala.ijcc.lang.model.Token
import com.github.oowekyala.ijcc.lang.psi.JccLiteralRegexUnit
import com.github.oowekyala.ijcc.lang.psi.JccRegexExpansionUnit
import com.github.oowekyala.ijcc.lang.psi.JccRegexSpec
import com.github.oowekyala.ijcc.util.JavaccIcons
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import java.util.*

/**
 * Reference from a literal regex written in BNF to its [Token]. Regexes in BNF are always in the
 * default state.
 *
 * If there exists a [JccRegexSpec] in the default state defined as *exactly* this string,
 * the reference points to that regex. Otherwise a new string token is synthesized by JavaCC
 * (see documentation on [Token]). Then this reference's [resolve] points to the [JccRegexExpansionUnit]
 * generating it.
 *
 * @author Clément Fournier
 * @since 1.0
 */
class JccBnfStringLiteralReference(element: JccLiteralRegexUnit) :
    PsiReferenceBase<JccLiteralRegexUnit>(element) {

    /**
     * Resolves the token that matches this literal. If [exact], only exact
     * string literals will be returned (meaning [Token.asStringToken] is non-null).
     * Otherwise full regex will be used.
     */
    fun resolveToken(exact: Boolean): Token? {
        val file = element.containingFile
        val grammar = file.lexicalGrammar

        return grammar.defaultState.matchLiteral(
            element,
            exact,
            consideredRegexKinds = EnumSet.allOf(RegexKind::class.java)
        )
    }

    override fun resolve(): PsiElement? = resolveToken(exact = true)?.psiElement


    /**
     * Enables autocompletion. Only tokens from the default state are considered.
     */
    override fun getVariants(): Array<Any> = element.containingFile
        .lexicalGrammar
        .defaultState
        .tokens
        .filter { !it.isPrivate }
        .mapNotNull { token ->
            val asString = token.getAsStringToken() ?: return@mapNotNull null

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
        .toList().toTypedArray()

}