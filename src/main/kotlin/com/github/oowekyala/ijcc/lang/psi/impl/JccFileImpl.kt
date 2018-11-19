package com.github.oowekyala.ijcc.lang.psi.impl

import com.github.oowekyala.ijcc.JavaccFileType
import com.github.oowekyala.ijcc.JavaccLanguage
import com.github.oowekyala.ijcc.lang.psi.JccNonTerminalProduction
import com.github.oowekyala.ijcc.lang.psi.JccParserDeclaration
import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider
import com.intellij.psi.tree.IFileElementType


/**
 * File implementation.
 *
 * @author Clément Fournier
 * @since 1.0
 */
class JccFileImpl(fileViewProvider: FileViewProvider) : PsiFileBase(fileViewProvider, JavaccLanguage) {

    override fun getFileType(): FileType = JavaccFileType

    val parserDeclaration: JccParserDeclaration
        get() = findChildByClass(JccParserDeclaration::class.java)!!

    val nonTerminalProductions: List<JccNonTerminalProduction>
        get() = findChildrenByClass(JccNonTerminalProduction::class.java).toList()




    companion object {
        /**
         * Element type.
         */
        val TYPE = IFileElementType("JCC_FILE", JavaccLanguage)
    }
}