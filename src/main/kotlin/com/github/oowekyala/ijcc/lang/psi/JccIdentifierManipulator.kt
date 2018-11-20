package com.github.oowekyala.ijcc.lang.psi

import com.github.oowekyala.ijcc.lang.psi.impl.JccElementFactory
import com.intellij.openapi.util.TextRange
import com.intellij.psi.AbstractElementManipulator
import com.intellij.util.IncorrectOperationException


/**
 * @author Clément Fournier
 * @since 1.0
 */
class JccIdentifierManipulator : AbstractElementManipulator<JccIdentifier>() {
    @Throws(IncorrectOperationException::class)
    override fun handleContentChange(identifier: JccIdentifier, range: TextRange, newContent: String): JccIdentifier? {
        val oldText = identifier.text
        val newText = oldText.substring(0, range.startOffset) + newContent +
                oldText.substring(range.endOffset)
        return identifier.replace(JccElementFactory.createIdentifier(identifier.project, newText)) as JccIdentifier?
    }
}