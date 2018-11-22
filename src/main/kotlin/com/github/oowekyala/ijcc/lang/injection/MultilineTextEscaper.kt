package com.github.oowekyala.ijcc.lang.injection

import com.intellij.openapi.util.TextRange
import com.intellij.psi.LiteralTextEscaper
import com.intellij.psi.PsiLanguageInjectionHost

/**
 * @author Clément Fournier
 * @since 1.0
 */
class MultilineTextEscaper<T : PsiLanguageInjectionHost>(t: T) : LiteralTextEscaper<T>(t) {
    override fun isOneLine(): Boolean = false

    override fun decode(rangeInsideHost: TextRange, outChars: StringBuilder): Boolean {
        outChars.append(rangeInsideHost.substring(myHost.text))
        return true
    }

    override fun getOffsetInHost(offsetInDecoded: Int, rangeInsideHost: TextRange): Int {
        return rangeInsideHost.startOffset + offsetInDecoded
    }
}