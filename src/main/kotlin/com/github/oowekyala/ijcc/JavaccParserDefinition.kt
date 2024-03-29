package com.github.oowekyala.ijcc

import com.github.oowekyala.ijcc.lang.lexer.JavaccLexerAdapter
import com.github.oowekyala.ijcc.lang.parser.JavaccParser
import com.github.oowekyala.ijcc.lang.psi.JccTypesExt
import com.github.oowekyala.ijcc.lang.psi.impl.JccElementFactory
import com.github.oowekyala.ijcc.lang.psi.impl.JccFileImpl
import com.github.oowekyala.ijcc.lang.psi.stubs.JccFileStub
import com.intellij.lang.ASTNode
import com.intellij.lang.ParserDefinition
import com.intellij.lang.PsiParser
import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet

/**
 * Parser definition for the JavaCC language.
 *
 * @author Clément Fournier
 */
open class JavaccParserDefinition : ParserDefinition {

    override fun createLexer(project: Project): Lexer = JavaccLexerAdapter()

    override fun createParser(project: Project): PsiParser = JavaccParser()

    override fun getFileNodeType(): IFileElementType = JccFileStub.TYPE

    override fun getWhitespaceTokens(): TokenSet = JccTypesExt.WhitespaceTypeSet

    override fun getCommentTokens(): TokenSet = JccTypesExt.CommentTypeSet

    override fun getStringLiteralElements(): TokenSet = JccTypesExt.StringLiteralTypeSet

    override fun createElement(node: ASTNode): PsiElement = JccElementFactory.createElement(node)

    override fun createFile(viewProvider: FileViewProvider): PsiFile = JccFileImpl(viewProvider, JavaccLanguage.INSTANCE)

    override fun spaceExistenceTypeBetweenTokens(
        astNode: ASTNode?,
        astNode1: ASTNode?
    ): ParserDefinition.SpaceRequirements =
        ParserDefinition.SpaceRequirements.MAY
}

open class CongoccParserDefinition : JavaccParserDefinition() {
    override fun createLexer(project: Project): Lexer = JavaccLexerAdapter(isCCC = true)
    override fun createFile(viewProvider: FileViewProvider): PsiFile =
        JccFileImpl(viewProvider, CongoccLanguage.INSTANCE)

    override fun getFileNodeType(): IFileElementType = JccFileStub.CCC_TYPE

}
