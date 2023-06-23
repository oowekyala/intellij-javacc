package com.github.oowekyala.ijcc.ide.refs

import com.github.oowekyala.ijcc.icons.JccIcons
import com.github.oowekyala.ijcc.ide.completion.withTail
import com.github.oowekyala.ijcc.ide.structureview.presentableText
import com.github.oowekyala.ijcc.ide.structureview.presentationIcon
import com.github.oowekyala.ijcc.lang.model.RegexKind
import com.github.oowekyala.ijcc.lang.psi.allJjtreeDecls
import com.github.oowekyala.ijcc.lang.psi.canReferencePrivate
import com.intellij.codeInsight.completion.simple.ParenthesesTailType
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.editor.Editor
import com.intellij.psi.codeStyle.CommonCodeStyleSettings

/**
 * @author Clément Fournier
 */
class IdeRefVariantsService : JccRefVariantService() {

    override fun nonterminalRefVariants(ref: JccNonTerminalReference): Array<Any> =
        ref.element.containingFile.nonTerminalProductions.map {
            LookupElementBuilder.create(it.name)
                .withPsiElement(it)
                .withPresentableText(it.presentableText)
                .withIcon(it.presentationIcon)
                .withTail("() ")
        }
            .toList()
            .plus(LookaheadLookupItem)
            .toTypedArray()


    override fun lexicalStateVariants(ref: JccLexicalStateReference): Array<Any> =
        ref.element.containingFile
            .lexicalGrammar
            .lexicalStates
            .asSequence()
            .mapNotNull {
                LookupElementBuilder.create(it.name)
                    .withPsiElement(it.declarationIdent)
                    .withIcon(JccIcons.LEXICAL_STATE)
            }
            .toList()
            .toTypedArray()

    override fun terminalVariants(ref: JccTerminalReference): Array<Any> {
        val canReferencePrivate = ref.element.canReferencePrivate

        return ref.element.containingFile
            .lexicalGrammar
            .allTokens
            .asSequence()
            .filter { canReferencePrivate || !it.isPrivate }
            .mapNotNull { token ->
                token.name?.let { name ->
                    LookupElementBuilder
                        .create(name)
                        .withIcon(token.psiElement?.presentationIcon)
                        .withTail("> ")

                }
            }.toList()
            .toTypedArray()
    }

    override fun stringLiteralVariants(ref: JccBnfStringLiteralReference): Array<Any> =
        ref.element.containingFile
            .lexicalGrammar
            .defaultState
            .let { dftState ->
                dftState.tokens
                    .asSequence()
                    .filter { !it.isPrivate && it.regexKind == RegexKind.TOKEN }
                    .mapNotNull { token ->
                        val asString = token.asStringToken ?: return@mapNotNull null

                        LookupElementBuilder
                            .create(asString.text.removeSuffix("\""))
                            .withPsiElement(token.psiElement)
                            .withPresentableText(asString.text)
                            .withIcon(JccIcons.TOKEN)
                            .withTypeText(token.let {
                                buildString {
                                    if (it.lexicalStatesOrEmptyForAll != listOfNotNull(dftState.name)) {
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
                            .withTail("\" ")
                    }.toList().toTypedArray()
            }


    override fun jjtreeNodeVariants(ref: JjtNodePolyReference): Array<Any> =
        ref.element.containingFile.allJjtreeDecls
            .asSequence()
            .sortedBy { it.value.size }
            .mapNotNull { (nodeName, declarators) ->
                LookupElementBuilder.create(nodeName)
                    .withPresentableText("#$nodeName")
                    .withPsiElement(declarators.firstOrNull())
                    .withIcon(JccIcons.JJTREE_NODE)
            }
            .toList()
            .toTypedArray()


    // TODO move to completion contributor with a proper pattern
    private val LookaheadLookupItem =
        LookupElementBuilder.create("LOOKAHEAD")
            .withBoldness(true)
            .withPresentableText("LOOKAHEAD")
            .withTailText("(...)", true)
            .withTail(object : ParenthesesTailType() {
                override fun isSpaceWithinParentheses(styleSettings: CommonCodeStyleSettings?,
                                                      editor: Editor?,
                                                      tailOffset: Int): Boolean = false

                override fun isSpaceBeforeParentheses(styleSettings: CommonCodeStyleSettings?,
                                                      editor: Editor?,
                                                      tailOffset: Int): Boolean = false
            })


}
