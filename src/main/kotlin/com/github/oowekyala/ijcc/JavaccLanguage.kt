package com.github.oowekyala.ijcc

import com.intellij.lang.Language

/**
 * JavaCC language. JJTree uses the same language instead of a dialect,
 * splitting them would have caused a lot of duplication.
 *
 * @author Clément Fournier
 * @since 1.0
 */
object JavaccLanguage : Language("JavaCC") {

    override fun isCaseSensitive(): Boolean = true
}