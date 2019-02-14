package com.github.oowekyala.ijcc.ide.completion

import com.github.oowekyala.ijcc.lang.JccTypes
import com.github.oowekyala.ijcc.lang.model.GrammarOptions
import com.github.oowekyala.ijcc.lang.model.JccOptionType.BaseOptionType.BOOLEAN
import com.github.oowekyala.ijcc.lang.model.RegexKind
import com.github.oowekyala.ijcc.lang.psi.*
import com.intellij.codeInsight.TailType
import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.completion.impl.CamelHumpMatcher
import com.intellij.codeInsight.completion.simple.BracesTailType
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.codeInsight.lookup.TailTypeDecorator
import com.intellij.patterns.ElementPattern
import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.patterns.PsiElementPattern
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext


/**
 *
 * FIXME!!
 *
 * @author Clément Fournier
 * @since 1.1
 */
class JccCompletionContributor : CompletionContributor() {

    init {
        val placePattern: PsiElementPattern.Capture<PsiElement> = psiElement()
            .inFile(PlatformPatterns.instanceOf(JccFile::class.java))
            .andNot(psiElement().inside(PsiComment::class.java))

        val optionValuePattern =
            psiElement().withAncestor(2, psiElement(JccOptionBinding::class.java))
                .afterSibling(
                    psiElement(JccTypes.JCC_EQ)
                )

        val optionNamePattern =
            psiElement().atStartOf(psiElement(JccOptionBinding::class.java))
                .andNot(psiElement().inside(PsiComment::class.java))
                .andNot(optionValuePattern)

        val bnfColonPattern =
            psiElement(JccTypes.JCC_COLON).withParent(JccBnfProduction::class.java)

        optionNamePattern.completeWith {
            result.withPrefixMatcher(CamelHumpMatcher("")).addAllElements(OptionVariants)
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

    //
    //    override fun duringCompletion(context: CompletionInitializationContext) {
    //        val psiFile = context.file as? JccFile ?: return
    //
    //        val element = psiFile.findElementAt(context.startOffset)
    //        if (element?.isOfType(JCC_IDENT) == true) {
    //            context.dummyIdentifier = "x"
    //            context.offsetMap.addOffset(START_OFFSET, element.textOffset-1)// = element.textOffset - 1
    //        }
    //    }

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


        val OptionVariants: List<TailTypeDecorator<LookupElementBuilder>> =
            GrammarOptions.knownOptions.map { (name, opt) ->
                LookupElementBuilder.create(name)
                    .withBoldness(true)
                    .withTypeText("(${opt.expectedType}) = ${opt.staticDefaultValue.presentable()}", true)
                    .withTail(TailType.EQ)
            }

        private fun Any?.presentable(): String = when (this) {
            is String -> "\"${this}\""
            else      -> toString()
        }

        val BoolOptionValueVariants =
            listOf("true", "false")
                .map { LookupElementBuilder.create(it).withBoldness(true) }
                .map { TailTypeDecorator.withTail(it, TailType.SEMICOLON) }

    }

}


