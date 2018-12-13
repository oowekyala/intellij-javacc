package com.github.oowekyala.ijcc.lang.psi.impl

import com.github.oowekyala.ijcc.JavaccFileType
import com.github.oowekyala.ijcc.JavaccLanguage
import com.github.oowekyala.ijcc.insight.model.JavaccConfig
import com.github.oowekyala.ijcc.insight.model.LexicalGrammar
import com.github.oowekyala.ijcc.lang.psi.*
import com.github.oowekyala.ijcc.lang.refs.NonTerminalScopeProcessor
import com.github.oowekyala.ijcc.lang.refs.TerminalScopeProcessor
import com.github.oowekyala.ijcc.util.filterMapAs
import com.intellij.extapi.psi.PsiFileBase
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.ResolveState
import com.intellij.psi.scope.PsiScopeProcessor
import com.intellij.util.IncorrectOperationException


/**
 * File implementation.
 *
 * @author Clément Fournier
 * @since 1.0
 */
class JccFileImpl(fileViewProvider: FileViewProvider) : PsiFileBase(fileViewProvider, JavaccLanguage), JccFile {

    override fun getFileType(): FileType = JavaccFileType

    private val grammarFileRoot
        get() = findChildByClass(JccGrammarFileRoot::class.java)!!

    override val regexpProductions: Sequence<JccRegularExprProduction>
        get() = grammarFileRoot.childrenSequence().filterMapAs()

    override val parserDeclaration: JccParserDeclaration
        get() = grammarFileRoot.parserDeclaration

    override val nonTerminalProductions: Sequence<JccNonTerminalProduction>
        get() = grammarFileRoot.childrenSequence().filterMapAs()

    override val globalNamedTokens: Sequence<JccNamedRegularExpression>
        get() = globalTokenSpecs.map { it.regularExpression }.filterMapAs()


    override val globalTokenSpecs: Sequence<JccRegexprSpec>
        get() =
            grammarFileRoot.childrenSequence(reversed = false)
                .filterMapAs<JccRegularExprProduction>()
                .filter { it.regexprKind.text == "TOKEN" }
                .flatMap { it.childrenSequence().filterMapAs<JccRegexprSpec>() }

    override val options: JccOptionSection?
        get() = grammarFileRoot.optionSection

    override val javaccConfig: JavaccConfig by lazy { JavaccConfig(options, parserDeclaration) } // todo is lazy safe?

    override val lexicalGrammar: LexicalGrammar
        get() = LexicalGrammar(grammarFileRoot.childrenSequence().filterMapAs())

    override fun getContainingFile(): JccFile = this

    override fun getPackageName(): String = javaccConfig.parserPackage

    override fun setPackageName(packageName: String?) {
        throw IncorrectOperationException("Cannot set the package of the parser that way")
    }

    override fun getClasses(): Array<PsiClass> {

        val injected =
                InjectedLanguageManager.getInstance(project).getInjectedPsiFiles(grammarFileRoot)
                    ?.takeIf { it.isNotEmpty() }
                    ?: return emptyArray()

        return injected.mapNotNull {
            it.first.descendantSequence().map { it as? PsiClass }.firstOrNull { it != null }
        }.toTypedArray()
    }

    override fun processDeclarations(processor: PsiScopeProcessor,
                                     state: ResolveState,
                                     lastParent: PsiElement?,
                                     place: PsiElement): Boolean {
        return when (processor) {
            is NonTerminalScopeProcessor -> processor.executeUntilFound(nonTerminalProductions, state)
            is TerminalScopeProcessor    -> processor.executeUntilFound(globalTokenSpecs, state)
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