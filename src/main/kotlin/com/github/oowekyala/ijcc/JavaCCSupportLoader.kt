package com.github.oowekyala.ijcc

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.ApplicationComponent
import com.intellij.openapi.fileTypes.FileTypeManager

class JavaCCSupportLoader : ApplicationComponent {

    override fun initComponent() {
        ApplicationManager.getApplication().runWriteAction {
            extensions.forEach {
                FileTypeManager.getInstance().registerFileType(JavaccFileType, it)
            }
        }
    }

    override fun disposeComponent() {}

    override fun getComponentName(): String = "JavaCC support loader"

    companion object {
        private val extensions = arrayOf("jj", "jjt")
    }
}
   
