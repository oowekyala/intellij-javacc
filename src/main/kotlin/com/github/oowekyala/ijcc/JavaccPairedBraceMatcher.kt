package com.github.oowekyala.ijcc

import com.github.oowekyala.ijcc.lang.JavaccTypes.*
import com.github.oowekyala.ijcc.lang.psi.JccJavaBlock
import com.github.oowekyala.ijcc.lang.psi.JccNonTerminalProduction
import com.intellij.lang.BracePair
import com.intellij.lang.PairedBraceMatcher
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.parentOfType

/**
 * @author Clément Fournier
 * @since 1.0
 */
class JavaccPairedBraceMatcher : PairedBraceMatcher {
    override fun getCodeConstructStart(file: PsiFile?, openingBraceOffset: Int): Int {
        val element = file?.findElementAt(openingBraceOffset)
        if (element == null || element is PsiFile) return openingBraceOffset

        var parent = element.parent
        if (parent is JccJavaBlock) {
            parent = parent.getParent()
            if (parent is JccNonTerminalProduction) {
                return parent.textRange.startOffset
            }
        } else if (parent is JccNonTerminalProduction) {
            return parent.textRange.startOffset
        }
        return openingBraceOffset
    }


    override fun getPairs(): Array<BracePair> = Companion.pairs

    override fun isPairedBracesAllowedBeforeType(lbraceType: IElementType, contextType: IElementType?): Boolean = true

    companion object {
        private val pairs = arrayOf(
            BracePair(JCC_LPARENTH, JCC_RPARENTH, true), // TODO structural?
            BracePair(JCC_LBRACE, JCC_RBRACE, true),
            BracePair(JCC_LBRACKET, JCC_RBRACKET, false),
            BracePair(JCC_LT, JCC_GT, false)
        )
    }
}