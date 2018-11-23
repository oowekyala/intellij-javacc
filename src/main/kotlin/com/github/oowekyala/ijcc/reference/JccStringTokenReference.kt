package com.github.oowekyala.ijcc.reference

import com.github.oowekyala.ijcc.lang.psi.JccLiteralRegularExpression
import com.github.oowekyala.ijcc.lang.psi.JccRegexprSpec
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.ResolveState

/**
 * Reference from a literal regexp to a regexp spec covering its match.
 *
 * @author Clément Fournier
 * @since 1.0
 */
class JccStringTokenReference(element: JccLiteralRegularExpression) :
    PsiReferenceBase<JccLiteralRegularExpression>(element) {

    override fun resolve(): JccRegexprSpec? {
        val processor = JccStringTokenReferenceProcessor(element)
        val file = element.containingFile
        file.processDeclarations(processor, ResolveState.initial(), element, element)
        return processor.result()
    }

    override fun getVariants(): Array<Any> =
            // todo flatmap to the string tokens
            element.containingFile
                .globalNamedTokens
                .filter { !it.isPrivate }.toList().toTypedArray()

}