package com.github.oowekyala.ijcc.ide.findusages

import com.github.oowekyala.ijcc.lang.psi.*
import com.intellij.codeInsight.highlighting.HighlightUsagesHandlerBase
import com.intellij.codeInsight.highlighting.HighlightUsagesHandlerFactoryBase
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.util.Consumer

/**
 * Highlights usages of string tokens when caret is on a literal regexp.
 *
 * @author Clément Fournier
 * @since 1.1
 */
class JccHighlightStringTokenUsagesHandlerFactory : HighlightUsagesHandlerFactoryBase() {

    override fun createHighlightUsagesHandler(editor: Editor,
                                              file: PsiFile,
                                              target: PsiElement): HighlightUsagesHandlerBase<*>? {

        if (file !is JccFile) return null

        return target.ancestors(includeSelf = true)
            .filterIsInstance<JccLiteralRegularExpression>()
            .firstOrNull()
            ?.let {
                JccHighlightStringTokenUsagesHandler(editor, file, it.unit)
            }
    }


}

class JccHighlightStringTokenUsagesHandler(editor: Editor,
                                           file: PsiFile,
                                           private val literal: JccLiteralRegexUnit)

    : HighlightUsagesHandlerBase<JccLiteralRegexUnit>(editor, file) {


    override fun getTargets(): List<JccLiteralRegexUnit> = listOf(literal)

    override fun selectTargets(targets: List<JccLiteralRegexUnit>,
                               selectionConsumer: Consumer<List<JccLiteralRegexUnit>>) =
            selectionConsumer.consume(targets)

    override fun computeUsages(targets: List<JccLiteralRegexUnit>) {
        myFile as JccFile

        val token = targets[0].typedReference.resolveToken(exact = true) ?: return


        myFile.nonTerminalProductions
            .filterIsInstance<JccBnfProduction>()
            .flatMap { it.expansion?.descendantSequence(includeSelf = true) ?: emptySequence() }
            .filterIsInstance<JccRegexExpansionUnit>()
            .map { it.regularExpression }
            .filterIsInstance<JccLiteralRegularExpression>()
            .filter {
                it.typedReference.resolveToken(exact = true) == token
            }.forEach {
                addOccurrence(it)
            }
    }


}