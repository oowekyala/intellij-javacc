package com.github.oowekyala.ijcc.ide.completion

import com.github.oowekyala.ijcc.lang.model.RegexKind
import com.github.oowekyala.ijcc.lang.psi.*
import com.intellij.codeInsight.completion.*
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
object JccCompletionContributor : CompletionContributor() {

    init {
        val placePattern: PsiElementPattern.Capture<PsiElement> = psiElement()
            .inFile(PlatformPatterns.instanceOf(JccFile::class.java))
            .andNot(psiElement().inside(PsiComment::class.java))


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
                    JccRegularExprProduction::class.java,
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

}


