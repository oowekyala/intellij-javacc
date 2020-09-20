package com.github.oowekyala.ijcc.lang.psi.impl

import com.github.oowekyala.ijcc.*
import com.github.oowekyala.ijcc.lang.model.*
import com.github.oowekyala.ijcc.lang.psi.*
import com.github.oowekyala.ijcc.lang.psi.stubs.JccFileStub
import com.intellij.extapi.psi.PsiFileBase
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.fileTypes.FileTypeRegistry
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElementVisitor
import com.intellij.util.IncorrectOperationException


/**
 * File implementation.
 *
 * @author Clément Fournier
 * @since 1.0
 */
class JccFileImpl(fileViewProvider: FileViewProvider) : PsiFileBase(fileViewProvider, JavaccLanguage), JccFile {

    private val myType: FileType by lazy {
        originalFile.virtualFile?.fileType ?: FileTypeRegistry.getInstance().getFileTypeByFileName(name)
    }

    override fun getFileType(): FileType = myType

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

    override var grammarNature: GrammarNature = when (fileType) {
        // isInInjection -> GrammarNature.UNKNOWN
        JjtreeFileType   -> GrammarNature.JJTREE
        JavaccFileType   -> GrammarNature.JAVACC
        JjtricksFileType -> GrammarNature.JJTRICKS
        Javacc21FileType -> GrammarNature.J21
        else             -> GrammarNature.UNKNOWN
    }
        set(value) {
            field = value
            clearCaches()
        }


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

    override fun getStub(): JccFileStub? = super.getStub() as? JccFileStub?

    /**
     * Some structures are lazily cached to reduce the number of times
     * they must be constructed. These include the [LexicalGrammar], the
     * [InlineGrammarOptions] and stuff. They're rebuilt when text changes, a
     * heuristic for that is the highlight passes (it's done in the init
     * routine of [JccHighlightVisitor]).
     */
    override fun clearCaches() {
        super<PsiFileBase>.clearCaches()
        myLexGrammarImpl = null
        myGrammarOptionsImpl = null
        mySyntaxGrammarImpl = null
    }

    private var myLexGrammarImpl: LexicalGrammar? = null

    override val lexicalGrammar: LexicalGrammar
        get() = myLexGrammarImpl ?: let { myLexGrammarImpl = LexicalGrammar(this);myLexGrammarImpl!! }

    private var mySyntaxGrammarImpl: SyntaxGrammar? = null

    val syntaxGrammar: SyntaxGrammar
        get() = mySyntaxGrammarImpl ?: let { mySyntaxGrammarImpl = SyntaxGrammar(this); mySyntaxGrammarImpl!! }

    private var myGrammarOptionsImpl: IGrammarOptions? = null

    override val grammarOptions: IGrammarOptions
        get() = myGrammarOptionsImpl ?: let { myGrammarOptionsImpl = buildOptions(); myGrammarOptionsImpl!! }

    private fun buildOptions(): IGrammarOptions =
        project.grammarOptionsService.buildOptions(this)

    override fun getContainingFile(): JccFile = this

    override fun getPackageName(): String = grammarOptions.parserPackage

    override fun setPackageName(packageName: String?) {
        throw IncorrectOperationException("Cannot set the package of the parser that way")
    }

    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is JccVisitor)
            visitor.visitPsiElement(this)
        else
            visitor.visitFile(this)
    }


    /**
     * This is important for access resolution to be done properly in injected
     * fragments. Otherwise package-private declarations are deemed inaccessible.
     */
    override fun getClasses(): Array<PsiClass> {

        val injected =
            parserDeclaration
                ?.javaCompilationUnit
                ?.let { InjectedLanguageManager.getInstance(project).getInjectedPsiFiles(it) }
                ?.takeIf { it.isNotEmpty() }
                ?: return emptyArray()

        return injected.mapNotNull {
            it.first.descendantSequence().filterIsInstance<PsiClass>().firstOrNull()
        }.toTypedArray()
    }
}
