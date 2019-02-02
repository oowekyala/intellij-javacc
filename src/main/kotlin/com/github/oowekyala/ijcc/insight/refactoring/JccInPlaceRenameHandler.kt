package com.github.oowekyala.ijcc.insight.refactoring

import com.github.oowekyala.ijcc.lang.psi.JccNamedRegularExpression
import com.intellij.openapi.editor.Editor
import com.intellij.psi.*
import com.intellij.refactoring.RefactoringActionHandler
import com.intellij.refactoring.rename.inplace.VariableInplaceRenameHandler
import com.intellij.refactoring.rename.inplace.VariableInplaceRenamer

/**
 * @author Clément Fournier
 * @since 1.0
 */
open class JccInPlaceRenameHandler : VariableInplaceRenameHandler() {


    override fun createRenamer(elementToRename: PsiElement, editor: Editor): VariableInplaceRenamer? {
        val currentElementToRename = elementToRename as PsiNameIdentifierOwner
        val currentName = currentElementToRename.nameIdentifier?.text ?: ""
        return RenamerImpl(currentElementToRename, editor, currentName, currentName)
    }

    public override fun isAvailable(element: PsiElement?, editor: Editor, file: PsiFile) =
            editor.settings.isVariableInplaceRenameEnabled && element != null && isInplaceRenameAvailable(element)

    companion object {
        fun isInplaceRenameAvailable(element: PsiElement): Boolean = when (element) {
            is JccNamedRegularExpression -> true
            else                         -> false
        }
    }

    protected open class RenamerImpl : VariableInplaceRenamer {
        constructor(elementToRename: PsiNamedElement, editor: Editor) : super(elementToRename, editor)
        constructor(
            elementToRename: PsiNamedElement,
            editor: Editor,
            currentName: String,
            oldName: String
        ) : super(elementToRename, editor, editor.project!!, currentName, oldName)

        override fun acceptReference(reference: PsiReference): Boolean {
            val refElement = reference.element
            val textRange = reference.rangeInElement
            val referenceText = refElement.text.substring(textRange.startOffset, textRange.endOffset) //.unquote()
            return referenceText == myElementToRename.name
        }

        override fun createInplaceRenamerToRestart(variable: PsiNamedElement,
                                                   editor: Editor,
                                                   initialName: String): VariableInplaceRenamer {
            return RenamerImpl(variable, editor, initialName, myOldName)
        }
    }
}