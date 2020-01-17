package com.github.oowekyala.ijcc.ide.completion

import com.intellij.codeInsight.TailType
import com.intellij.openapi.editor.Editor

/**
 * @author Clément Fournier
 * @since 1.1
 */
data class MultiCharTailType(private val tail: String) : TailType() {

    override fun processTail(editor: Editor, tailOffset: Int): Int {

        val document = editor.document

        var finalOffset = tailOffset
        for (char in tail) {

            finalOffset = when (char) {
                document.charsSequence[tailOffset] -> moveCaret(editor, finalOffset, 1)
                else                               -> insertChar(editor, finalOffset, char)
            }
        }

        return finalOffset
    }
}
