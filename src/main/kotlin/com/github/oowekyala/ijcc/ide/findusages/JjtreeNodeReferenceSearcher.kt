package com.github.oowekyala.ijcc.ide.findusages

import com.github.oowekyala.ijcc.ide.refs.JjtNodePolyReference
import com.github.oowekyala.ijcc.lang.psi.*
import com.intellij.openapi.application.QueryExecutorBase
import com.intellij.psi.PsiReference
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.util.Processor

/**
 * @author Clément Fournier
 * @since 1.1
 */
object JjtreeNodeReferenceSearcher : QueryExecutorBase<PsiReference, ReferencesSearch.SearchParameters>(true) {

    override fun processQuery(queryParameters: ReferencesSearch.SearchParameters,
                              consumer: Processor<in PsiReference>) {

        val toSearch = queryParameters.elementToSearch as? JccIdentifier ?: return

        if (!toSearch.isJjtreeNodeIdentifier) return

        val owner = toSearch.firstAncestorOrNull<JjtNodeClassOwner>() ?: return

        findReferencesTo(owner, toSearch.name).all {
            // "all" stops on the first "false" result
            consumer.process(it)
        }
    }

    private fun findReferencesTo(origin: JjtNodeClassOwner,
                                 rawName: String): Sequence<JjtNodePolyReference> =
        origin.containingFile
            .syntaxGrammar
            .getJjtreeDeclsForRawName(rawName)
            .asSequence()
            .mapNotNull { it.declarator }
            .filter { it != origin }
            .mapNotNull { it.typedReference }


}