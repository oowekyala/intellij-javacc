package com.github.oowekyala.ijcc.ide.completion

//import com.intellij.codeInsight.lookup.BracesTailType
import com.github.oowekyala.ijcc.icons.JccIcons
import com.github.oowekyala.ijcc.icons.icon
import com.github.oowekyala.ijcc.ide.completion.JccPatterns.jjtreeHashPattern
import com.github.oowekyala.ijcc.ide.completion.JccPatterns.optionNamePattern
import com.github.oowekyala.ijcc.ide.completion.JccPatterns.optionValuePattern
import com.github.oowekyala.ijcc.ide.completion.JccPatterns.placePattern
import com.github.oowekyala.ijcc.ide.quickdoc.realOrFakeOptionNodeFor
import com.github.oowekyala.ijcc.lang.model.InlineGrammarOptions
import com.github.oowekyala.ijcc.lang.model.JccOptionType.BaseOptionType.BOOLEAN
import com.github.oowekyala.ijcc.lang.model.RegexKind
import com.github.oowekyala.ijcc.lang.psi.*
import com.github.oowekyala.ijcc.util.runIt
import com.intellij.codeInsight.CharTailType
import com.intellij.codeInsight.TailType
import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.EqTailType
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.ElementPattern
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext


/**
 * Custom completions.
 *
 * @author Clément Fournier
 * @since 1.1
 */
class JccCompletionContributor : CompletionContributor() {

    init {

        optionNamePattern.completeWith {

            val file = parameters.originalFile as? JccFile ?: return@completeWith

            val fileNature = file.grammarNature

            val alreadyThere = parameters
                .position
                .firstAncestorOrNull<JccOptionSection>()!!
                .optionBindingList
                .mapNotNull { it.modelOption?.name }


            InlineGrammarOptions.knownOptions
                .filterKeys { it !in alreadyThere }
                .filterValues { it.supports(fileNature) }
                .map { (name, opt) ->
                    LookupElementBuilder.create(name)
                        .withPsiElement(file.realOrFakeOptionNodeFor(name))
                        .withIcon(opt.supportedNature.icon)
                        // .withBoldness(true)
                        .withTypeText("(${opt.expectedType})", true)
                        .withInsertHandler { ctx, _ ->
                            val editor = ctx.editor
                            EqTailType.INSTANCE.processTail(editor, editor.caretModel.offset)
                            ctx.setAddCompletionChar(false)

                            TailType.SEMICOLON.processTail(editor, editor.caretModel.offset)
                            ctx.setAddCompletionChar(false)
                            ctx.commitDocument()

                            editor.caretModel.moveToOffset(editor.caretModel.offset - 1)

                        }
                }
                .let(result::addAllElements)
        }

        optionValuePattern.completeWith {
            val parent = parameters.position.parent.parent as? JccOptionBinding ?: return@completeWith
            // FIXME doesn't work
            when (parent.modelOption?.expectedType) {
                BOOLEAN -> result.addAllElements(BoolOptionValueVariants)
                else -> {
                    // todo
                }
            }
        }

        jjtreeHashPattern.completeWith {
            val file = parameters.originalPosition?.containingFile as? JccFile ?: return@completeWith


            val nodeOwner =
                parameters.originalPosition // leaf
                    ?.parent // JccIdent
                    ?.let { it.parent as? JccJjtreeNodeDescriptor }
                    ?.parent as? JjtNodeClassOwner
                    ?: return@completeWith

            file.allJjtreeDecls
                .forEach { (nodeName, declarators) ->
                    makeJjtreeLookupElt(nodeName, declarators)
                        .withPriority(declarators.size.toDouble())
                        .runIt {
                            if (nodeOwner !in declarators) {
                                // don't self complete
                                result.addElement(it)
                            }
                        }
                }

            if (nodeOwner is JccNonTerminalProduction) {
                result.addElement(VoidJjtreeAnnotVariant)

                val self =
                    makeJjtreeLookupElt(nodeName = nodeOwner.name, declarators = emptyList())
                        .withBoldness(true)
                        .withTailText("(...)")
                        .withTail(CallParensTailType)
                        .withPriority(3.0)

                result.addElement(self)
            }
        }

        // Regex production completion
        placePattern.completeWith {
            val position = parameters.position
            val parent = PsiTreeUtil.getParentOfType<PsiElement>(
                position,
                JccNonTerminalProduction::class.java,
                JccGrammarFileRoot::class.java,
                JccTokenManagerDecls::class.java,
                JccRegexProduction::class.java,
                JccOptionSection::class.java,
                JccFile::class.java
            )

            val accepted =
                when (parent) {
                    // could be an unclosed one
                    is JccNonTerminalProduction       -> parent.javaBlock == null
                    // root
                    is JccGrammarFileRoot, is JccFile -> true
                    else                              -> false
                }

            if (accepted) result.addAllElements(RegexProdVariants)
        }
    }

    private fun makeJjtreeLookupElt(nodeName: String,
                                    declarators: List<JjtNodeClassOwner>): LookupElementBuilder {
        return LookupElementBuilder.create(nodeName)
            .withPresentableText("#$nodeName")
            .withPsiElement(declarators.firstOrNull())
            .withIcon(JccIcons.JJTREE_NODE)
    }

    private fun ElementPattern<out PsiElement>.completeWith(completionType: CompletionType? = CompletionType.BASIC,
                                                            provideCompletion: ExtendCtx.() -> Unit) {

        super.extend(completionType, this, object : CompletionProvider<CompletionParameters>() {
            override fun addCompletions(parameters: CompletionParameters,
                                        context: ProcessingContext,
                                        result: CompletionResultSet) {
                ExtendCtx(parameters, context, result).provideCompletion()
            }

        })
    }

    private data class ExtendCtx(val parameters: CompletionParameters,
                                 val context: ProcessingContext?,
                                 val result: CompletionResultSet)

    companion object {

        val RegexProdVariants: List<LookupElement> =
            RegexKind.values().map {
                LookupElementBuilder.create(it.name + " : ")
                    .withPresentableText(it.name)
                    .withTailText(" : { ... }", true)
                    .withBoldness(true)
                    // FIXME .withTail(BracesTailType())
            }

        val BoolOptionValueVariants =
            listOf("true", "false")
                .map { LookupElementBuilder.create(it).withBoldness(true) }

        val VoidJjtreeAnnotVariant: LookupElement =
            LookupElementBuilder.create("void").withPresentableText("#void").withBoldness(true)


    }

}

object CallParensTailType : CharTailType('(') {

}


