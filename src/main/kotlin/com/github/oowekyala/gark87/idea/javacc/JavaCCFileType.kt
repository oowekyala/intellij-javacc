package com.github.oowekyala.gark87.idea.javacc

import com.github.oowekyala.gark87.idea.javacc.generated.JavaCCLanguage
import com.github.oowekyala.idea.javacc.util.JavaCCIcons
import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.openapi.fileTypes.SingleLazyInstanceSyntaxHighlighterFactory
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory
import javax.swing.Icon

/**
 * @author gark87
 */
class JavaCCFileType : LanguageFileType(com.github.oowekyala.gark87.idea.javacc.generated.JavaCCLanguage()) {
    init {
        SyntaxHighlighterFactory.LANGUAGE_FACTORY.addExplicitExtension(language, object : SingleLazyInstanceSyntaxHighlighterFactory() {
            override fun createHighlighter(): SyntaxHighlighter = JavaCCHighlighter()
        })
    }

    override fun getName(): String = "JavaCC"

    override fun getDescription(): String = "JavaCC *.jj files support"

    override fun getDefaultExtension(): String = "jj"

    override fun getIcon(): Icon? = JavaCCIcons.JAVACC_FILE.icon
}
