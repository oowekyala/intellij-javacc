package com.github.oowekyala.ijcc.ide.completion

import com.github.oowekyala.ijcc.lang.JccTypes
import com.github.oowekyala.ijcc.lang.model.GrammarOptions
import com.github.oowekyala.ijcc.lang.model.JccOptionType.BaseOptionType.BOOLEAN
import com.github.oowekyala.ijcc.lang.model.RegexKind
import com.github.oowekyala.ijcc.lang.psi.*
import com.github.oowekyala.ijcc.util.runIt
import com.intellij.codeInsight.TailType
import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.completion.impl.CamelHumpMatcher
import com.intellij.codeInsight.completion.simple.BracesTailType
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.codeInsight.lookup.TailTypeDecorator
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
                psiElement(JccTypes.JCC_IDENT).withAncestor(
                    2,
                    psiElement(JccOptionBinding::class.java)
                )
                    .andNot(psiElement().inside(PsiComment::class.java))
                    .andNot(optionValuePattern)

        // Option names
        extend(
            CompletionType.BASIC,
            optionNamePattern,
            object : CompletionProvider<CompletionParameters>() {
                override fun addCompletions(parameters: CompletionParameters,
                                            context: ProcessingContext?,
                                            result: CompletionResultSet) {

                    result.withPrefixMatcher(CamelHumpMatcher("")).addAllElements(OptionVariants)
                }
            })
        // Option values
        extend(
            CompletionType.BASIC,
            optionValuePattern,
            object : CompletionProvider<CompletionParameters>() {
                override fun addCompletions(parameters: CompletionParameters,
                                            context: ProcessingContext?,
                                            result: CompletionResultSet) {

                    val parent = parameters.position.parent as? JccOptionBinding ?: return

                    when (parent.modelOption?.expectedType) {
                        BOOLEAN -> result.addAllElements(BoolOptionValueVariants)
                    }
                }
            })

        extend(CompletionType.BASIC, placePattern, object : CompletionProvider<CompletionParameters>() {
            override fun addCompletions(parameters: CompletionParameters,
                                        context: ProcessingContext,
                                        result: CompletionResultSet) {


                val position = parameters.position
                val parent = PsiTreeUtil.getParentOfType<PsiElement>(
                    position,
                    JccNonTerminalProduction::class.java,
                    JccGrammarFileRoot::class.java,
                    JccTokenManagerDecls::class.java,
                    JccRegexProduction::class.java,
                    JccFile::class.java
                )

                when (parent) {
                    is JccGrammarFileRoot, is JccFile -> {
                        // topLevel

                        for (regexKind in RegexKind.values()) {
                            result.addElement(
                                TailTypeDecorator.withTail(
                                    LookupElementBuilder.create(regexKind.name + " : ").withBoldness(true),
                                    BracesTailType()
                                )
                            )
                        }
                    }
                }
            }
        })
    }

    override fun fillCompletionVariants(parameters: CompletionParameters, result: CompletionResultSet) {
        super.fillCompletionVariants(parameters, result)
    }

    override fun beforeCompletion(context: CompletionInitializationContext) {
        val psiFile = context.file as? JccFile ?: return

        //        context.dummyIdentifier = ""

        psiFile.findElementAt((context.startOffset - 1).coerceAtLeast(0))
            ?.takeIf { it.isOfType(JccTypes.JCC_IDENT) }
            ?.runIt { context.offsetMap.addOffset(CompletionInitializationContext.START_OFFSET, it.textOffset) }


    }

    companion object {

        val OptionVariants: List<TailTypeDecorator<LookupElementBuilder>> =
                GrammarOptions.knownOptions.map { (name, opt) ->
                    LookupElementBuilder.create(name)
                        .withBoldness(true)
                        .withTypeText("(${opt.expectedType}) = ${opt.staticDefaultValue.present()}", true)
                }.map { TailTypeDecorator.withTail(it, TailType.EQ) }

        private fun Any?.present(): String = when (this) {
            is String -> "\"${this}\""
            else      -> toString()
        }

        val BoolOptionValueVariants =
                listOf("true", "false")
                    .map { LookupElementBuilder.create(it).withBoldness(true) }
                    .map { TailTypeDecorator.withTail(it, TailType.SEMICOLON) }

    }

}


