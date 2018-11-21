package com.github.oowekyala.ijcc.reference

import com.github.oowekyala.ijcc.lang.psi.JccLiteralRegularExpression
import com.github.oowekyala.ijcc.lang.psi.JccRegexprSpec
import com.github.oowekyala.ijcc.lang.psi.impl.JccFileImpl
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
        val file = element.containingFile as JccFileImpl
        file.processDeclarations(processor, ResolveState.initial(), element, element)
        return processor.result()
    }

    override fun getVariants(): Array<Any> =
        (element.containingFile as JccFileImpl).globalTokenSpecs.toTypedArray()

}