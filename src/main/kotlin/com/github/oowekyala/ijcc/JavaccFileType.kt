package com.github.oowekyala.ijcc

import com.github.oowekyala.ijcc.util.JavaccIcons
import com.intellij.openapi.fileTypes.FileTypeConsumer
import com.intellij.openapi.fileTypes.FileTypeFactory
import com.intellij.openapi.fileTypes.LanguageFileType
import javax.swing.Icon

/**
 * File type implementation.
 *
 * @author Clément Fournier
 * @since 1.0
 */
object JavaccFileType : LanguageFileType(JavaccLanguage) {
    override fun getIcon(): Icon = JavaccIcons.JAVACC_FILE

    override fun getName(): String = "JavaCC grammar file"

    override fun getDefaultExtension(): String = "jj"

    override fun getDescription(): String = "JavaCC grammar file"
}

/** FileTypeFactory extension point. */
class JavaccFileTypeFactory : FileTypeFactory() {
    override fun createFileTypes(consumer: FileTypeConsumer) = consumer.consume(JavaccFileType, "jj;jjt")
}