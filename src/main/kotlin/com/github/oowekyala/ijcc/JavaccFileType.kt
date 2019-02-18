package com.github.oowekyala.ijcc

import com.github.oowekyala.ijcc.icons.JccIcons
import com.intellij.openapi.fileTypes.FileTypeConsumer
import com.intellij.openapi.fileTypes.FileTypeFactory
import com.intellij.openapi.fileTypes.LanguageFileType
import javax.swing.Icon

/**
 * @since inception
 */
object JavaccFileType : LanguageFileType(JavaccLanguage) {
    override fun getIcon(): Icon = JccIcons.JAVACC_FILE

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
    override fun getIcon(): Icon = JccIcons.JJTREE_FILE

    override fun getName(): String = "JJTREE_GRAMMAR"

    override fun getDefaultExtension(): String = "jjt"

    override fun getDescription(): String = "JJTree grammar"
}


/** FileTypeFactory extension point. */
class JavaccFileTypeFactory : FileTypeFactory() {
    override fun createFileTypes(consumer: FileTypeConsumer) {
        consumer.consume(JavaccFileType, "jj")
        consumer.consume(JjtreeFileType, "jjt")
    }
}