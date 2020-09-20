package com.github.oowekyala.ijcc.ide.structureview

import com.github.oowekyala.ijcc.lang.psi.*
import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.ide.structureView.impl.common.PsiTreeElementBase
import com.intellij.ide.structureView.impl.java.JavaClassTreeElement
import com.intellij.ide.util.treeView.smartTree.SortableTreeElement
import com.intellij.navigation.ItemPresentation
import com.intellij.pom.Navigatable
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiNamedElement
import javax.swing.Icon

/**
 * One element of the structure view. This class is used for all elements, regardless of their type.
 */
class JccStructureTreeElement(
    element: JccPsiElement,
    val children: List<StructureViewTreeElement>
) : PsiTreeElementBase<JccPsiElement>(element), SortableTreeElement, Navigatable {

    constructor(element: JccPsiElement) : this(element, emptyList())

    override fun getAlphaSortKey(): String = when (element) {
        is JccOptionSection -> "aaaaaaa"
        is JccTokenManagerDecls -> "aaaaaZZ"
        is JccRegexProduction -> "aaaaZZZ"
        else                    -> element!!.presentableText
    }

    override fun getPresentableText(): String? = element!!.presentableText

    override fun getLocationString(): String? = element!!.locationString

    override fun getIcon(open: Boolean): Icon? = element!!.presentationIcon

    override fun getPresentation(): ItemPresentation = element!!.presentationForStructure

    override fun getChildrenBase(): Collection<StructureViewTreeElement> = children

}

class JccJavaClassTreeElementWrapper(klass: PsiClass, val jccFile: JccFile) :
    PsiTreeElementBase<PsiClass>(klass), SortableTreeElement {

    private val ideDefault = JavaClassTreeElement(klass, false)

    override fun getPresentableText(): String? = "parser class ${element!!.name}"

    override fun getIcon(open: Boolean): Icon? = ideDefault.getIcon(open)

    override fun getAlphaSortKey(): String = "aaaaaaZ"

    override fun getChildrenBase(): Collection<StructureViewTreeElement> {
        val base = ideDefault.childrenBase.toMutableList()
        base.removeIf { elt ->
            // remove methods which are productions
            val psi = elt.value as? PsiMethod ?: return@removeIf false
            jccFile.syntaxGrammar.getProductionByNameMulti(psi.name).isNotEmpty()
        }

        base.removeIf { elt ->
            // remove everything that starts with jj_, those are internal goo
            val psi = elt.value as? PsiNamedElement ?: return@removeIf false
            psi.name?.startsWith("jj_") ?: false
        }

        return base
    }
}

