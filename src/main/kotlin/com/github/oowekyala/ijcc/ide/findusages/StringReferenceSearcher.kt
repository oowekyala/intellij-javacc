package com.github.oowekyala.ijcc.ide.findusages

import com.github.oowekyala.ijcc.ide.refs.JccBnfStringLiteralReference
import com.github.oowekyala.ijcc.lang.psi.*
import com.intellij.openapi.application.QueryExecutorBase
import com.intellij.psi.PsiReference
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

        findReferencesTo(toSearch).all {
            // "all" stops on the first "false" result
            consumer.process(it)
        }

    }

    /*
        FIXME
            this is probably very inefficient, build index in LexicalGrammar (or stubs!)
            we can't use the optimised word scan because it doesn't pick up on non-alphanumeric characters

    */
    private fun findReferencesTo(target: JccRegularExpressionOwner): Sequence<JccBnfStringLiteralReference> =
        target.containingFile
            .allProductions()
            .flatMap { it.descendantSequence(includeSelf = true) }
            .filterIsInstance<JccRegularExpressionOwner>()
            .mapNotNull { it.regularExpression.asSingleLiteral() }
            // filter out declaration
            .filter { it != target.definedToken.takeIf { it.isExplicit }?.asStringToken }
            .mapNotNull { it.typedReference }
            .filter { it.isReferenceTo(target) }

}