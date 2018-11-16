package com.github.oowekyala.gark87.idea.javacc.structureview

import com.github.oowekyala.idea.javacc.psi.DeclarationForStructureView
import com.github.oowekyala.idea.javacc.psi.JavaccFileImpl
import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.ide.structureView.impl.common.PsiTreeElementBase
import com.intellij.openapi.util.Key
import com.intellij.psi.PsiElement
import com.intellij.psi.ResolveState
import com.intellij.psi.scope.PsiScopeProcessor
import java.util.*

/**
 * Root of the structure view.
 *
 * @author Clément Fournier
 * @since 1.0
 */
class JavaccFileTreeElement(file: JavaccFileImpl) : PsiTreeElementBase<JavaccFileImpl>(file) {

    private val declarations = ArrayList<StructureViewTreeElement>()

    init {
        val result = mutableListOf<DeclarationForStructureView>()
        // find declarations
        file.processDeclarations(DeclarationResolver { result.add(it) }, ResolveState.initial(), file, file)
        for (id in result) {
            declarations.add(JavaccLeafElement(id))
        }
    }

    override fun getChildrenBase(): Collection<StructureViewTreeElement> = declarations

    override fun getPresentableText(): String? = value!!.name

    private class DeclarationResolver(private val onFind: (DeclarationForStructureView) -> Unit) : PsiScopeProcessor {

        override fun execute(psiElement: PsiElement, resolveState: ResolveState): Boolean {
            if (psiElement is DeclarationForStructureView) {
                onFind(psiElement)
            }
            return false
        }

        override fun <T> getHint(tKey: Key<T>): T? = null

        override fun handleEvent(event: PsiScopeProcessor.Event, o: Any?) = System.err.println(event.toString())
    }
}
