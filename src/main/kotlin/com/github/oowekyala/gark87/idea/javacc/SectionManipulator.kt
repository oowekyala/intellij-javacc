package com.github.oowekyala.gark87.idea.javacc


import com.github.oowekyala.gark87.idea.javacc.psi.Identifier
import com.intellij.openapi.util.TextRange
import com.intellij.psi.AbstractElementManipulator
import com.intellij.util.IncorrectOperationException

/**
 * @author gark87
 */
class SectionManipulator : AbstractElementManipulator<Identifier>() {
    @Throws(IncorrectOperationException::class)
    override fun handleContentChange(identifier: Identifier, range: TextRange, newContent: String): Identifier? {
        val oldText = identifier.text
        val newText = oldText.substring(0, range.startOffset) + newContent +
                      oldText.substring(range.endOffset)
        return identifier.replaceWithText(newText) as Identifier
    }
}
