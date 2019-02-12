package com.github.oowekyala.ijcc.lang.psi.impl

import com.github.oowekyala.ijcc.JavaccFileType
import com.github.oowekyala.ijcc.JavaccLanguage
import com.github.oowekyala.ijcc.ide.highlight.JccHighlightVisitor
import com.github.oowekyala.ijcc.lang.model.GrammarOptions
import com.github.oowekyala.ijcc.lang.model.LexicalGrammar
import com.github.oowekyala.ijcc.lang.model.SyntaxGrammar
import com.github.oowekyala.ijcc.lang.psi.*
import com.intellij.extapi.psi.PsiFileBase
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiClass
import com.intellij.util.IncorrectOperationException


/**
 * File implementation.
 *
 * @author Clément Fournier
 * @since 1.0
 */
class JccFileImpl(fileViewProvider: FileViewProvider) : PsiFileBase(fileViewProvider, JavaccLanguage), JccFile {

    override fun getFileType(): FileType = JavaccFileType

    override val grammarFileRoot: JccGrammarFileRoot?
        get() = findChildByClass(JccGrammarFileRoot::class.java)

    override val tokenManagerDecls: Sequence<JccTokenManagerDecls>
        get() = grammarFileRoot?.childrenSequence()?.filterIsInstance<JccTokenManagerDecls>() ?: emptySequence()

    override val regexProductions: Sequence<JccRegexProduction>
        get() = grammarFileRoot?.childrenSequence()?.filterIsInstance<JccRegexProduction>() ?: emptySequence()

    override val lexicalStatesFirstMention: Sequence<JccIdentifier>
        get() = regexProductions.flatMap { it.lexicalStatesIdents.asSequence() }.distinctBy { it.name }


    override val parserDeclaration: JccParserDeclaration?
        get() = grammarFileRoot?.parserDeclaration

    override val nonTerminalProductions: Sequence<JccNonTerminalProduction>
        get() = grammarFileRoot?.childrenSequence()?.filterIsInstance<JccNonTerminalProduction>() ?: emptySequence()

    override val globalNamedTokens: Sequence<JccNamedRegularExpression>
        get() = globalTokenSpecs.map { it.regularExpression }.filterIsInstance<JccNamedRegularExpression>()


    override val globalTokenSpecs: Sequence<JccRegexSpec>
        get() =
            grammarFileRoot
                ?.childrenSequence(reversed = false)
                ?.filterIsInstance<JccRegexProduction>()
                ?.filter { it.regexKind.text == "TOKEN" }
                ?.flatMap { it.childrenSequence().filterIsInstance<JccRegexSpec>() }
                ?: emptySequence()

    override val options: JccOptionSection?
        get() = grammarFileRoot?.optionSection

    /**
     * Some structures are lazily cached to reduce the number of times
     * they must be constructed. These include the [LexicalGrammar], the
     * [GrammarOptions] and stuff. They're rebuilt when text changes, a
     * heuristic for that is the highlight passes (it's done in the init
     * routine of [JccHighlightVisitor]).
     */
    internal fun invalidateCachedStructures() {
        myLexGrammarImpl = LexicalGrammar(this)
        myGrammarOptionsImpl = GrammarOptions(options, parserDeclaration)
        mySyntaxGrammarImpl = SyntaxGrammar(this)
    }

    private var myLexGrammarImpl: LexicalGrammar? = null

    override val lexicalGrammar: LexicalGrammar
        get() = myLexGrammarImpl ?: let { invalidateCachedStructures(); myLexGrammarImpl!! }

    private var mySyntaxGrammarImpl: SyntaxGrammar? = null

    override val syntaxGrammar: SyntaxGrammar
        get() = mySyntaxGrammarImpl ?: let { invalidateCachedStructures(); mySyntaxGrammarImpl!! }

    private var myGrammarOptionsImpl: GrammarOptions? = null

    override val grammarOptions: GrammarOptions
        get() = myGrammarOptionsImpl ?: let { invalidateCachedStructures(); myGrammarOptionsImpl!! }


    override fun getContainingFile(): JccFile = this

    override fun getPackageName(): String = grammarOptions.parserPackage

    override fun setPackageName(packageName: String?) {
        throw IncorrectOperationException("Cannot set the package of the parser that way")
    }

    /**
     * This is important for access resolution to be done properly in injected
     * fragments. Otherwise package-private declarations are deemed inaccessible.
     */
    override fun getClasses(): Array<PsiClass> {

        val injected =
            grammarFileRoot?.let { InjectedLanguageManager.getInstance(project).getInjectedPsiFiles(it) }
                ?.takeIf { it.isNotEmpty() }
                ?: return emptyArray()

        return injected.mapNotNull {
            it.first.descendantSequence().filterIsInstance<PsiClass>().firstOrNull()
        }.toTypedArray()
    }

}
