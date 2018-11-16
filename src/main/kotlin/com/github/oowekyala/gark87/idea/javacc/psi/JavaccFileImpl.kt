package com.github.oowekyala.gark87.idea.javacc.psi

import com.github.oowekyala.idea.javacc.JavaCCSupportLoader
import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.ResolveState
import com.intellij.psi.scope.PsiScopeProcessor

//import org.gark87.idea.javacc.psi.JavaCCInput;

/**
 * JavaCC file.
 *
 * @author gark87
 */
class JavaccFileImpl(provider: FileViewProvider) : PsiFileBase(provider, JavaCCElementTypes.LANG) {

    private val javaccInput: JavaccInput?
        get() = children.first { it is JavaccInput } as? JavaccInput

    override fun getFileType(): FileType = JavaCCSupportLoader.JAVA_CC

    override fun processDeclarations(processor: PsiScopeProcessor, state: ResolveState, lastParent: PsiElement?, place: PsiElement): Boolean {
        val input = javaccInput ?: return true
        for (production in input.productions) {
            val regExpProduction = production.regexpProduction
            if (regExpProduction != null) {
                for (regExp in regExpProduction.allRegExpSpec) {
                    if (processor.execute(regExp, state)) {
                        return false
                    }
                }
            }
            val nonTerminal = production.nonTerminalProduction
            if (nonTerminal != null && processor.execute(nonTerminal, state)) {
                return false
            }
        }
        return true
    }
}