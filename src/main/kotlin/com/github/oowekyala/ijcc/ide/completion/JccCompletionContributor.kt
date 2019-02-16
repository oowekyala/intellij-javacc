package com.github.oowekyala.ijcc.ide.completion

import com.github.oowekyala.ijcc.ide.completion.JccPatterns.optionNamePattern
import com.github.oowekyala.ijcc.ide.completion.JccPatterns.optionValuePattern
import com.github.oowekyala.ijcc.ide.completion.JccPatterns.placePattern
import com.github.oowekyala.ijcc.ide.quickdoc.realOrFakeOptionNodeFor
import com.github.oowekyala.ijcc.lang.model.GrammarOptions
import com.github.oowekyala.ijcc.lang.model.JccOptionType.BaseOptionType.BOOLEAN
import com.github.oowekyala.ijcc.lang.model.RegexKind
import com.github.oowekyala.ijcc.lang.model.presentValue
import com.github.oowekyala.ijcc.lang.psi.*
import com.intellij.codeInsight.TailType
import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.completion.simple.BracesTailType
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.codeInsight.lookup.TailTypeDecorator
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


            GrammarOptions.knownOptions
                .filterKeys { it !in alreadyThere }
                .filterValues { it.supportedNature <= fileNature }
                .map { (name, opt) ->
                    LookupElementBuilder.create(name)
                        .withPsiElement(file.realOrFakeOptionNodeFor(name))
                        .withIcon(opt.supportedNature.icon)
                        // .withBoldness(true)
                        .withTypeText("(${opt.expectedType}) = ${opt.staticDefaultValue.presentValue()}", true)
                        .withTail(TailType.EQ)
                }
                .let(result::addAllElements)
        }

        optionValuePattern.completeWith {
            val parent = parameters.position.parent as? JccOptionBinding ?: return@completeWith

            when (parent.modelOption?.expectedType) {
                BOOLEAN -> result.addAllElements(BoolOptionValueVariants)
            }
        }

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

    private fun ElementPattern<out PsiElement>.completeWith(completionType: CompletionType? = CompletionType.BASIC,
                                                            provideCompletion: ExtendCtx.() -> Unit) {

        super.extend(completionType, this, object : CompletionProvider<CompletionParameters>() {
            override fun addCompletions(parameters: CompletionParameters,
                                        context: ProcessingContext?,
                                        result: CompletionResultSet) {
                ExtendCtx(parameters, context, result).provideCompletion()
            }

        })
    }

    private data class ExtendCtx(val parameters: CompletionParameters,
                                 val context: ProcessingContext?,
                                 val result: CompletionResultSet)

    companion object {

        val RegexProdVariants: List<TailTypeDecorator<LookupElementBuilder>> =
            RegexKind.values().map {
                LookupElementBuilder.create(it.name + " : ")
                    .withPresentableText(it.name)
                    .withTailText(" : { ... }", true)
                    .withBoldness(true)
                    .withTail(BracesTailType())
            }

        val BoolOptionValueVariants =
            listOf("true", "false")
                .map { LookupElementBuilder.create(it).withBoldness(true) }
                .map { TailTypeDecorator.withTail(it, TailType.SEMICOLON) }

    }

}


