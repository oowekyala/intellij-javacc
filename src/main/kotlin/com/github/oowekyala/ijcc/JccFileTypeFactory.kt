package com.github.oowekyala.ijcc

import com.intellij.openapi.fileTypes.FileTypeConsumer
import com.intellij.openapi.fileTypes.FileTypeFactory


/** FileTypeFactory extension point. */
class JavaccFileTypeFactory : FileTypeFactory() {
    override fun createFileTypes(consumer: FileTypeConsumer) {
        consumer.consume(JavaccFileType, "jj")
        consumer.consume(JjtreeFileType, "jjt")
        consumer.consume(JjtricksFileType, "jjtx")
        consumer.consume(Javacc21FileType, "javacc")
    }
}
