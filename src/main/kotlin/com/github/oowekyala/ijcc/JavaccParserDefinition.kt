package com.github.oowekyala.ijcc

import com.github.oowekyala.ijcc.lang.JavaccTypes
import com.github.oowekyala.ijcc.lang.lexer.JavaccLexerAdapter
import com.github.oowekyala.ijcc.lang.parser.JavaccParser
import com.github.oowekyala.ijcc.lang.psi.impl.JccFileImpl
import com.intellij.lang.ASTNode
import com.intellij.lang.ParserDefinition
import com.intellij.lang.PsiParser
import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet

/**
 * Parser definition for the JavaCC language.
 *
 * @author Clément Fournier
 */
class JavaccParserDefinition : ParserDefinition {

    override fun createLexer(project: Project): Lexer = JavaccLexerAdapter()

    override fun createParser(project: Project): PsiParser = JavaccParser()

    override fun getFileNodeType(): IFileElementType = JccFileImpl.TYPE

    override fun getWhitespaceTokens(): TokenSet = TokenSet.create(TokenType.WHITE_SPACE)


    override fun getCommentTokens(): TokenSet =
        TokenSet.create(
            JavaccTypes.JCC_C_STYLE_COMMENT,
            JavaccTypes.JCC_END_OF_LINE_COMMENT,
            JavaccTypes.JCC_DOC_COMMENT
        )

    override fun getStringLiteralElements(): TokenSet =
        TokenSet.create(JavaccTypes.JCC_STRING_LITERAL)

    override fun createElement(node: ASTNode): PsiElement = JavaccTypes.Factory.createElement(node)

    override fun createFile(viewProvider: FileViewProvider): PsiFile = JccFileImpl(viewProvider)

    override fun spaceExistenceTypeBetweenTokens(
        astNode: ASTNode?,
        astNode1: ASTNode?
    ): ParserDefinition.SpaceRequirements =
        ParserDefinition.SpaceRequirements.MAY
}
