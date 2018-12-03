package com.github.oowekyala.ijcc.lang.refs

import com.github.oowekyala.ijcc.lang.JavaccTypes
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceRegistrar
import com.intellij.psi.impl.source.resolve.reference.CommentsReferenceContributor

/**
 * @author Clément Fournier
 * @since 1.0
 */
object JccReferenceContributor : PsiReferenceContributor() {
    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {

        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(JavaccTypes.JCC_DOC_COMMENT),
            CommentsReferenceContributor.COMMENTS_REFERENCE_PROVIDER_TYPE.provider
        )

    }
}