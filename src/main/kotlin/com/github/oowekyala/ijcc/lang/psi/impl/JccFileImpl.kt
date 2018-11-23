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
import com.intellij.util.containers.stream
import java.util.stream.Stream
import kotlin.streams.toList


/**
 * File implementation.
 *
 * @author Clément Fournier
 * @since 1.0
 */
class JccFileImpl(fileViewProvider: FileViewProvider) : PsiFileBase(fileViewProvider, JavaccLanguage), JccFile {
    override val regexpProductions: List<JccRegularExprProduction>
        get() = findChildrenByClass(JccRegularExprProduction::class.java).asList()

    override fun getFileType(): FileType = JavaccFileType

    override val parserDeclaration: JccParserDeclaration
        get() = findChildByClass(JccParserDeclaration::class.java)!!

    override val nonTerminalProductions: List<JccNonTerminalProduction>
        get() = findChildrenByClass(JccNonTerminalProduction::class.java).toList()

    override val globalNamedTokens: List<JccNamedRegularExpression>
        get() = globalTokensStream().map { it.regularExpression }.filterMapAs<JccNamedRegularExpression>().toList()

    override val globalTokenSpecs: List<JccRegexprSpec>
        get() = globalTokensStream().toList()

    override val options: JccOptionSection?
        get() = findChildByClass(JccOptionSection::class.java)

    private fun globalTokensStream(): Stream<JccRegexprSpec> =
            findChildrenByClass(JccRegularExprProduction::class.java).stream()
                .filter { it.regexprKind.text == "TOKEN" }
                .flatMap { it.regexprSpecList.stream() }


    override fun getContainingFile(): JccFile = this

    override fun processDeclarations(processor: PsiScopeProcessor,
                                     state: ResolveState,
                                     lastParent: PsiElement?,
                                     place: PsiElement): Boolean {
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


}