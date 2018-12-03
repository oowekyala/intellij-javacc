package com.github.oowekyala.ijcc.lang.psi.manipulators

import com.github.oowekyala.ijcc.lang.psi.JccOptionValue
import com.github.oowekyala.ijcc.lang.psi.impl.JccElementFactory
import com.intellij.openapi.util.TextRange
import com.intellij.psi.AbstractElementManipulator
import com.intellij.util.IncorrectOperationException

/**
 * @author Clément Fournier
 * @since 1.0
 */
class JccOptionValueManipulator : AbstractElementManipulator<JccOptionValue>() {
    @Throws(IncorrectOperationException::class)
    override fun handleContentChange(
        opt: JccOptionValue,
        range: TextRange,
        newContent: String
    ): JccOptionValue? {
        val oldText = opt.text
        val newText = oldText.substring(0, range.startOffset) + newContent +
                oldText.substring(range.endOffset)
        return opt.replace(
            JccElementFactory.createOptionValue(
                opt.project,
                newText
            )
        ) as JccOptionValue?
    }
}