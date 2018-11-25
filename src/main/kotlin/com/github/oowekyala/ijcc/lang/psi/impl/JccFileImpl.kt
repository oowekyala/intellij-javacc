package com.github.oowekyala.ijcc.lang.psi.impl

import com.github.oowekyala.ijcc.JavaccFileType
import com.github.oowekyala.ijcc.JavaccLanguage
import com.github.oowekyala.ijcc.lang.psi.*
import com.github.oowekyala.ijcc.model.LexicalGrammar
import com.github.oowekyala.ijcc.reference.NonTerminalScopeProcessor
import com.github.oowekyala.ijcc.reference.TerminalScopeProcessor
import com.github.oowekyala.ijcc.util.childrenSequence
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

    override val globalPublicNamedTokens: Sequence<JccNamedRegularExpression>
        get() = globalNamedTokens.filter { !it.isPrivate }


    override val globalTokenSpecs: Sequence<JccRegexprSpec>
        get() = childrenSequence(reversed = false)
            .filterMapAs<JccRegularExprProduction>()
            .filter { it.regexprKind.text == "TOKEN" }
            .flatMap { it.childrenSequence().filterMapAs<JccRegexprSpec>() }

    override val options: JccOptionSection?
        get() = findChildByClass(JccOptionSection::class.java)


    // TODO maybe rebuild that incrementally
    override val lexicalGrammar: LexicalGrammar by lazy { LexicalGrammar(childrenSequence().filterMapAs()) }

    override fun getContainingFile(): JccFile = this

    override fun processDeclarations(processor: PsiScopeProcessor,
                                     state: ResolveState,
                                     lastParent: PsiElement?,
                                     place: PsiElement): Boolean {
        return when (processor) {
            is NonTerminalScopeProcessor        -> executeUntilFound(nonTerminalProductions, state, processor)
            is TerminalScopeProcessor           -> {
                val seq = if (processor.isRegexContext) globalNamedTokens else globalPublicNamedTokens
                executeUntilFound(seq, state, processor)
            }
//            is JccStringTokenReferenceProcessor -> executeUntilFound(globalTokenSpecs, state, processor)
            else                                -> true
        }
    }

    private fun executeUntilFound(list: Sequence<PsiElement>,
                                  state: ResolveState,
                                  processor: PsiScopeProcessor): Boolean {
        for (spec in list) {
            if (!processor.execute(spec, state)) return false
        }
        return true
    }


}