package com.github.oowekyala.ijcc.ide.intentions

import com.github.oowekyala.ijcc.lang.psi.JccPsiElement
import com.github.oowekyala.ijcc.lang.psi.ancestors
import com.github.oowekyala.ijcc.lang.psi.containsInside
import com.intellij.codeInsight.FileModificationService
import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.codeInspection.IntentionWrapper
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil

/**
 * @author Clément Fournier
 * @since 1.0
 */

@Suppress("EqualsOrHashCode")
abstract class SelfTargetingIntention<TElement : PsiElement>(
    val elementType: Class<TElement>,
    private var text: String,
    private val familyName: String = text
) : IntentionAction {

    protected val defaultText: String = text

    protected fun setText(text: String) {
        this.text = text
    }

    final override fun getText() = text
    final override fun getFamilyName() = familyName

    abstract fun isApplicableTo(element: TElement, caretOffset: Int): Boolean

    abstract fun applyTo(project: Project,
                         editor: Editor?,
                         element: TElement)

    private fun getTarget(editor: Editor, file: PsiFile): TElement? {
        val offset = editor.caretModel.offset
        val leaf1 = file.findElementAt(offset)
        val leaf2 = file.findElementAt(offset - 1)
        val commonParent = if (leaf1 != null && leaf2 != null) PsiTreeUtil.findCommonParent(leaf1, leaf2) else null

        var elementsToCheck: Sequence<PsiElement> = emptySequence()
        if (leaf1 != null) {
            elementsToCheck += leaf1.ancestors(includeSelf = true).takeWhile { it != commonParent }
        }
        if (leaf2 != null) {
            elementsToCheck += leaf2.ancestors(includeSelf = true).takeWhile { it != commonParent }
        }
        if (commonParent != null && commonParent !is PsiFile) {
            elementsToCheck += commonParent.ancestors(includeSelf = true)
        }

        for (element in elementsToCheck) {
            @Suppress("UNCHECKED_CAST")
            if (elementType.isInstance(element) && isApplicableTo(element as TElement, offset)) {
                return element
            }
            if (!allowCaretInsideElement(element) && element.textRange.containsInside(offset)) break
        }
        return null
    }

    protected open fun allowCaretInsideElement(element: PsiElement): Boolean = true

    final override fun isAvailable(project: Project, editor: Editor, file: PsiFile): Boolean =
            getTarget(editor, file) != null


    final override fun invoke(project: Project, editor: Editor?, file: PsiFile) {
        editor ?: return
        PsiDocumentManager.getInstance(project).commitAllDocuments()
        val target = getTarget(editor, file) ?: return
        if (!FileModificationService.getInstance().preparePsiElementForWrite(target)) return
        applyTo(project, editor, target)
    }

    override fun startInWriteAction() = true

    override fun toString(): String = getText()

    override fun equals(other: Any?): Boolean {
        // Nasty code because IntentionWrapper itself does not override equals
        if (other is IntentionWrapper) return this == other.action
        return other is SelfTargetingIntention<*> && javaClass == other.javaClass && text == other.text
    }

    // Intentionally missed hashCode (IntentionWrapper does not override it)
}

abstract class SelfTargetingRangeIntention<TElement : PsiElement>(
    elementType: Class<TElement>,
    text: String,
    familyName: String = text
) : SelfTargetingIntention<TElement>(elementType, text, familyName) {

    abstract fun applicabilityRange(element: TElement): TextRange?

    final override fun isApplicableTo(element: TElement, caretOffset: Int): Boolean {
        val range = applicabilityRange(element) ?: return false
        return range.containsOffset(caretOffset)
    }
}

abstract class SelfTargetingOffsetIndependentIntention<TElement : PsiElement>(
    elementType: Class<TElement>,
    text: String,
    familyName: String = text
) : SelfTargetingRangeIntention<TElement>(elementType, text, familyName) {

    abstract fun isApplicableTo(element: TElement): Boolean

    final override fun applicabilityRange(element: TElement): TextRange? {
        return if (isApplicableTo(element)) element.textRange else null
    }
}