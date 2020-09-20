package com.github.oowekyala.ijcc.ide.structureview

import com.github.oowekyala.ijcc.lang.psi.JccOptionSection
import com.github.oowekyala.ijcc.lang.psi.JccPsiElement
import com.github.oowekyala.ijcc.lang.psi.JccRegexProduction
import com.github.oowekyala.ijcc.lang.psi.JccTokenManagerDecls
import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.ide.structureView.impl.java.JavaClassTreeElement
import com.intellij.ide.util.treeView.smartTree.SortableTreeElement
import com.intellij.ide.util.treeView.smartTree.TreeElement
import com.intellij.navigation.ItemPresentation
import com.intellij.pom.Navigatable
import com.intellij.psi.PsiClass

/**
 * One element of the structure view. This class is used for all elements, regardless of their type.
 *
 * TODO represent synthetic members as non navigatable?
 *
 */
class JccStructureTreeElement(val element: JccPsiElement,
                              children: List<TreeElement>)
    : StructureViewTreeElement, SortableTreeElement, Navigatable by element {

    constructor(element: JccPsiElement) : this(element, emptyList())

    private val myChildren: Array<out TreeElement> = children.toTypedArray()

    override fun getChildren(): Array<out TreeElement> = myChildren

    override fun getValue(): Any = element

    override fun getAlphaSortKey(): String = when (element) {
        is JccOptionSection -> "aaaaaaa"
        is JccTokenManagerDecls -> "aaaaaZZ"
        is JccRegexProduction -> "aaaaZZZ"
        else                    -> element.presentableText
    }


    override fun getPresentation(): ItemPresentation = element.presentationForStructure

}

class JccJavaClassTreeElementWrapper(val psiClass: JavaClassTreeElement, val name: String) :
    StructureViewTreeElement by psiClass, SortableTreeElement {

    constructor(klass: PsiClass) : this(JavaClassTreeElement(klass, false), klass.name ?: "anon")

    override fun getPresentation(): ItemPresentation =
        psiClass.presentation.let {
            PresentationData("parser class $name", it.locationString, it.getIcon(true), null)
        }

    override fun getAlphaSortKey(): String =
        "aaaaaaZ"
}

