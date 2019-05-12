package com.github.oowekyala.ijcc.ide.intentions

import com.github.oowekyala.ijcc.lang.psi.JccOptionValue
import com.github.oowekyala.ijcc.lang.psi.impl.jccEltFactory
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager

/**
 * @author Clément Fournier
 */
class ReplaceOptionValueIntention(private val replacement: String) :
    JccSelfTargetingEditorIntentionBase<JccOptionValue>(
        JccOptionValue::class.java,
        "Replace value with $replacement"
    ) {

    override fun isApplicableTo(element: JccOptionValue): Boolean = true

    override fun run(project: Project, editor: Editor, element: JccOptionValue): () -> Unit {

        val elt = project.jccEltFactory.createOptionValue(replacement)

        return {
            element.replace(elt)
            PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(editor.document)
        }
    }

}
