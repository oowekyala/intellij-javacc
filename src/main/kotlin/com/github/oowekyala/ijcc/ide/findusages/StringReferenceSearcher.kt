package com.github.oowekyala.ijcc.ide.findusages

import com.github.oowekyala.ijcc.lang.model.Token
import com.github.oowekyala.ijcc.lang.psi.*
import com.intellij.openapi.application.QueryExecutorBase
import com.intellij.psi.PsiReference
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.SearchRequestCollector
import com.intellij.psi.search.UsageSearchContext
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.util.Processor

/**
 * @author Clément Fournier
 * @since 1.1
 */
object StringReferenceSearcher : QueryExecutorBase<PsiReference, ReferencesSearch.SearchParameters>(true) {
    override fun processQuery(queryParameters: ReferencesSearch.SearchParameters,
                              consumer: Processor<in PsiReference>) {

        val toSearch = queryParameters.elementToSearch as? JccRegularExpressionOwner ?: return

        val token = toSearch.definedToken

        val unit = token.asStringToken ?: return

//        JccHighlightStringTokenUsagesHandler.findReferencesTo(token, toSearch.containingFile) {
//            consumer.process(it.typedReference)
//        }

        addStringReferencesUsages(token, unit, toSearch.containingFile, queryParameters.optimizer)
    }

    private fun addStringReferencesUsages(token: Token,
                                          unit: JccLiteralRegexUnit,
                                          file: JccFile,
                                          collector: SearchRequestCollector) {

        collector.searchWord(
            unit.text, // search for the string token
            GlobalSearchScope.fileScope(file),
            UsageSearchContext.ANY,
            true, // caseSensitive
            token.psiElement!!
        )
    }

}