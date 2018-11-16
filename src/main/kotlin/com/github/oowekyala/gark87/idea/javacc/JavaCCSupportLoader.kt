package com.github.oowekyala.gark87.idea.javacc

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.ApplicationComponent
import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.fileTypes.LanguageFileType

class JavaCCSupportLoader : ApplicationComponent {

    override fun initComponent() {
        ApplicationManager.getApplication().runWriteAction {
            extensions.forEach {
                FileTypeManager.getInstance().registerFileType(JAVA_CC, it)
            }
        }
    }

    override fun disposeComponent() {}

    override fun getComponentName(): String = "JavaCC support loader"

    companion object {
        val JAVA_CC: LanguageFileType = JavaCCFileType()
        private val extensions = arrayOf("jj", "jjt")
    }
}
   
