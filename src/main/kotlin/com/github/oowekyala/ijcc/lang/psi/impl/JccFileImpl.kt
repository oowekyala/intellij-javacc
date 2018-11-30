package com.github.oowekyala.ijcc.lang.psi.impl

import com.github.oowekyala.ijcc.JavaccFileType
import com.github.oowekyala.ijcc.JavaccLanguage
import com.github.oowekyala.ijcc.lang.psi.*
import com.github.oowekyala.ijcc.model.LexicalGrammar
import com.github.oowekyala.ijcc.reference.NonTerminalScopeProcessor
import com.github.oowekyala.ijcc.reference.TerminalScopeProcessor
import com.github.oowekyala.ijcc.util.filterMapAs
import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.ResolveState
import com.intellij.psi.scope.PsiScopeProcessor


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

    override val nonTerminalProductions: Sequence<JccNonTerminalProduction>
        get() = findChildrenByClass(JccNonTerminalProduction::class.java).asSequence()

    override val globalNamedTokens: Sequence<JccNamedRegularExpression>
        get() = globalTokenSpecs.map { it.regularExpression }.filterMapAs()


    override val globalTokenSpecs: Sequence<JccRegexprSpec>
        get() = childrenSequence(reversed = false)
            .filterMapAs<JccRegularExprProduction>()
            .filter { it.regexprKind.text == "TOKEN" }
            .flatMap { it.childrenSequence().filterMapAs<JccRegexprSpec>() }

    override val options: JccOptionSection?
        get() = findChildByClass(JccOptionSection::class.java)

    override val lexicalGrammar: LexicalGrammar
        get() = LexicalGrammar(childrenSequence().filterMapAs())

    override fun getContainingFile(): JccFile = this

    override fun processDeclarations(processor: PsiScopeProcessor,
                                     state: ResolveState,
                                     lastParent: PsiElement?,
                                     place: PsiElement): Boolean {
        return when (processor) {
            is NonTerminalScopeProcessor -> processor.executeUntilFound(nonTerminalProductions, state)
            is TerminalScopeProcessor    -> {
                val seq = if (processor.isRegexContext) globalTokenSpecs else globalTokenSpecs.filter { !it.isPrivate }
                processor.executeUntilFound(seq, state)
            }
            else                         -> true
        }
    }

    private fun PsiScopeProcessor.executeUntilFound(list: Sequence<PsiElement>, state: ResolveState): Boolean {
        for (spec in list) {
            if (!execute(spec, state)) return false
        }
        return true
    }


}