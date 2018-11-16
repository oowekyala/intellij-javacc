package com.github.oowekyala.gark87.idea.javacc.psi.reference

import com.github.oowekyala.gark87.idea.javacc.psi.Identifier
import com.github.oowekyala.gark87.idea.javacc.psi.JavaccFileImpl
import com.github.oowekyala.gark87.idea.javacc.psi.JavaccScope
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.ResolveState
import com.intellij.psi.util.PsiTreeUtil
import java.util.*

/**
 * @author gark87
 */
class IdentifierReference(element: Identifier, private val myTypes: EnumSet<JavaCCScopeProcessor.DeclarationType>) : PsiReferenceBase<Identifier>(element) {

    override fun getVariants(): Array<out Any> {
        val processor = JavaCCScopeProcessor(myTypes)
        process(processor)
        return processor.getCandidates()
    }

    override fun resolve(): PsiElement? {
        val needle = canonicalText
        val processor = JavaCCResolveProcessor(needle, myTypes)
        process(processor)
        return processor.result
    }

    private fun process(processor: JavaCCScopeProcessor) {
        val element = element
        val scope = PsiTreeUtil.getParentOfType(element, JavaccScope::class.java, JavaccFileImpl::class.java)
        scope!!.processDeclarations(processor, ResolveState.initial(), element, element)
    }
}
