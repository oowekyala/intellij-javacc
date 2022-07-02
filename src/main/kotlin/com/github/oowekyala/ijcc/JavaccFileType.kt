package com.github.oowekyala.ijcc

import com.github.oowekyala.ijcc.icons.JccCoreIcons
import com.intellij.openapi.fileTypes.LanguageFileType
import javax.swing.Icon

abstract class BaseJccFileType : LanguageFileType(JavaccLanguage) {
    override fun getDisplayName(): String = getDescription()
}

/**
 * @since inception
 */
object JavaccFileType : BaseJccFileType() {
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
object JjtreeFileType : BaseJccFileType() {
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
object JjtricksFileType : BaseJccFileType() {
    override fun getIcon(): Icon = JccCoreIcons.JJTREE_FILE

    override fun getName(): String = "JJTRICKS_GRAMMAR"

    override fun getDefaultExtension(): String = "jjtx"

    override fun getDescription(): String = "JJTricks grammar"
}

/**
 * This file type is available for Javacc 21.
 *
 * @since 1.6
 */
object Javacc21FileType : BaseJccFileType() {
    override fun getIcon(): Icon = JccCoreIcons.JAVACC_FILE

    override fun getName(): String = "JAVACC21_GRAMMAR"

    override fun getDefaultExtension(): String = "javacc"

    override fun getDescription(): String = "JavaCC 21 grammar"
}
