package com.github.oowekyala.ijcc.lang.psi.impl

import com.github.oowekyala.ijcc.JavaccFileType
import com.github.oowekyala.ijcc.JavaccLanguage
import com.github.oowekyala.ijcc.lang.psi.*
import com.github.oowekyala.ijcc.reference.JccStringTokenReferenceProcessor
import com.github.oowekyala.ijcc.reference.NonTerminalScopeProcessor
import com.github.oowekyala.ijcc.reference.TerminalScopeProcessor
import com.github.oowekyala.ijcc.util.filterMapAs
import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.ResolveState
import com.intellij.psi.scope.PsiScopeProcessor
import com.intellij.psi.tree.IFileElementType
import com.intellij.util.containers.stream
import java.util.stream.Stream
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


    /**
     * Named regexes of the TOKEN kind defined globally in the file.
     */
    val globalNamedTokens: List<JccNamedRegularExpression>
        get() = globalTokensStream().map { it.regularExpression }.filterMapAs<JccNamedRegularExpression>().toList()

    /**
     * Regexpr specs of the TOKEN kind defined globally in the file.
     */
    val globalTokenSpecs: List<JccRegexprSpec>
        get() = globalTokensStream().toList()

    private fun globalTokensStream(): Stream<JccRegexprSpec> =
        findChildrenByClass(JccRegularExprProduction::class.java).stream()
            .filter { it.regexprKind.text == "TOKEN" }
            .flatMap { it.regexprSpecList.stream() }


    override fun processDeclarations(
        processor: PsiScopeProcessor,
        state: ResolveState,
        lastParent: PsiElement?,
        place: PsiElement
    ): Boolean {
        return when (processor) {
            is NonTerminalScopeProcessor        -> executeUntilFound(nonTerminalProductions, state, processor)
            is TerminalScopeProcessor           -> executeUntilFound(globalNamedTokens, state, processor)
            is JccStringTokenReferenceProcessor -> executeUntilFound(globalTokenSpecs.asReversed(), state, processor)
            else                                -> true
        }
    }

    private fun executeUntilFound(list: List<PsiElement>, state: ResolveState, processor: PsiScopeProcessor): Boolean {
        for (spec in list) {
            if (!processor.execute(spec, state)) return false
        }
        return true
    }


    companion object {
        /**
         * Element type.
         */
        val TYPE = IFileElementType("JCC_FILE", JavaccLanguage)
    }
}