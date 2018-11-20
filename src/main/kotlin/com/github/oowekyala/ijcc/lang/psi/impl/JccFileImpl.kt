package com.github.oowekyala.ijcc.lang.psi.impl

import com.github.oowekyala.ijcc.JavaccFileType
import com.github.oowekyala.ijcc.JavaccLanguage
import com.github.oowekyala.ijcc.lang.psi.*
import com.github.oowekyala.ijcc.reference.NonTerminalScopeProcessor
import com.github.oowekyala.ijcc.reference.TerminalScopeProcessor
import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.ResolveState
import com.intellij.psi.scope.PsiScopeProcessor
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.containers.stream
import kotlin.streams.toList


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


    val globalTerminalSpecs: List<JccNamedRegularExpression>
        get() =
            findChildrenByClass(JccRegularExprProduction::class.java).stream()
                .flatMap { PsiTreeUtil.findChildrenOfType(it, JccNamedRegularExpression::class.java).stream() }.toList()


    override fun processDeclarations(
        processor: PsiScopeProcessor,
        state: ResolveState,
        lastParent: PsiElement?,
        place: PsiElement
    ): Boolean {
        when (processor) {
            is NonTerminalScopeProcessor -> nonTerminalProductions.forEach { processor.execute(it, state) }
            is TerminalScopeProcessor    -> globalTerminalSpecs.forEach { processor.execute(it, state) }
            else                         -> return true
        }
        return false
    }


    companion object {
        /**
         * Element type.
         */
        val TYPE = IFileElementType("JCC_FILE", JavaccLanguage)
    }
}