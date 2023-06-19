package com.github.oowekyala.ijcc

import com.github.oowekyala.ijcc.icons.JccCoreIcons
import com.intellij.lang.Language
import com.intellij.openapi.fileTypes.LanguageFileType
import javax.swing.Icon

abstract class BaseJccFileType(lang: Language = JavaccLanguage) : LanguageFileType(lang) {
    override fun getDisplayName(): String = description
}

/**
 * @since inception
 */
class JavaccFileType : BaseJccFileType() {
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
class JjtreeFileType : BaseJccFileType() {
    override fun getIcon(): Icon = JccCoreIcons.JJTREE_FILE

    override fun getName(): String = "JJTREE_GRAMMAR"

    override fun getDefaultExtension(): String = "jjt"

    override fun getDescription(): String = "JJTree grammar"
}

/**
 * This file type is available for Javacc 21.
 *
 * @since 1.6
 */
class Javacc21FileType : BaseJccFileType(CongoccLanguage) {
    override fun getIcon(): Icon = JccCoreIcons.JAVACC_FILE

    override fun getName(): String = "JAVACC21_GRAMMAR"

    override fun getDefaultExtension(): String = "ccc"

    override fun getDescription(): String = "CongoCC grammar"
}
