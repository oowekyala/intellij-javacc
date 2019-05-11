package com.github.oowekyala.ijcc

import com.github.oowekyala.ijcc.icons.JccCoreIcons
import com.intellij.openapi.fileTypes.LanguageFileType
import javax.swing.Icon

/**
 * @since inception
 */
object JavaccFileType : LanguageFileType(JavaccLanguage) {
    override fun getIcon(): Icon = JccCoreIcons.JAVACC_FILE

    override fun getName(): String = "JAVACC_GRAMMAR"

    override fun getDefaultExtension(): String = "jj"

    override fun getDescription(): String = "JavaCC grammar"
}

/**
 * This second file type is available for JJTree files.
 *
 * @since 1.2
 */
object JjtreeFileType : LanguageFileType(JavaccLanguage) {
    override fun getIcon(): Icon = JccCoreIcons.JJTREE_FILE

    override fun getName(): String = "JJTREE_GRAMMAR"

    override fun getDefaultExtension(): String = "jjt"

    override fun getDescription(): String = "JJTree grammar"
}

/**
 * This third file type is available for JJTricks files.
 *
 * @since 1.4
 */
object JjtricksFileType : LanguageFileType(JavaccLanguage) {
    override fun getIcon(): Icon = JccCoreIcons.JJTREE_FILE

    override fun getName(): String = "JJTRICKS_GRAMMAR"

    override fun getDefaultExtension(): String = "jjtx"

    override fun getDescription(): String = "JJTricks grammar"
}
