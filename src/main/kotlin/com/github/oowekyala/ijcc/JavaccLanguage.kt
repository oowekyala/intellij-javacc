package com.github.oowekyala.ijcc

import com.intellij.lang.Language

/**
 * JavaCC language. JJTree uses the same language instead of a dialect,
 * splitting them would probably cause a lot of duplication atm. There's
 * two file types though, [JavaccFileType] and [JjtreeFileType].
 *
 * @author Clément Fournier
 * @since 1.0
 */
class JavaccLanguage private constructor() : Language("JavaCC") {

    override fun isCaseSensitive(): Boolean = true

    companion object {
        val INSTANCE get() = Language.findInstance(JavaccLanguage::class.java)
        fun newInstance() = JavaccLanguage()
        val displayName get() = INSTANCE.displayName
    }
}

class CongoccLanguage private constructor() : Language(JavaccLanguage.INSTANCE, "CongoCC") {

    override fun isCaseSensitive(): Boolean = true

    companion object {
        val INSTANCE get() = Language.findInstance(CongoccLanguage::class.java)
        fun newInstance() = CongoccLanguage()
        val displayName get() = INSTANCE.displayName
    }
}
