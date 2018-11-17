package com.github.oowekyala.ijcc

import com.github.oowekyala.gark87.idea.javacc.generated.JavaCCElementTypes
import com.github.oowekyala.gark87.idea.javacc.generated.JavaCCTreeConstants
import com.github.oowekyala.gark87.idea.javacc.psi.*
import com.github.oowekyala.ijcc.lang.JavaccTypes
import com.github.oowekyala.ijcc.lang.lexer.JavaccLexerAdapter
import com.github.oowekyala.ijcc.lang.parser.JavaccParser
import com.intellij.extapi.psi.ASTWrapperPsiElement
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

    override fun getFileNodeType(): IFileElementType = JavaCCElementTypes.FILE
    /**/
    override fun getWhitespaceTokens(): TokenSet =
        TokenSet.create(TokenType.WHITE_SPACE)

//    init {
//        JavaParser.INSTANCE.statementParser.parseCodeBlock()
//    }

    override fun getCommentTokens(): TokenSet =
        TokenSet.create(
            JavaccTypes.JCC_C_STYLE_COMMENT,
            JavaccTypes.JCC_END_OF_LINE_COMMENT,
            JavaccTypes.JCC_DOC_COMMENT
        )

    override fun getStringLiteralElements(): TokenSet =
        TokenSet.create(JavaccTypes.JCC_STRING_LITERAL)

    override fun createElement(node: ASTNode): PsiElement {
        val type = node.elementType
        if (type === JavaCCTreeConstants.JJTBNF_PRODUCTION) {
            return BNFProduction(node)
        }
        if (type === JavaCCTreeConstants.JJTJAVACODE_PRODUCTION) {
            return JavacodeProduction(node)
        }
        if (type === JavaCCTreeConstants.JJTFORMALPARAMETER) {
            return FormalParameter(node)
        }
        if (type === JavaCCTreeConstants.JJTFORMALPARAMETERS) {
            return FormalParameters(node)
        }
        if (type === JavaCCTreeConstants.JJTVARIABLEDECLARATORID) {
            return VariableDeclaratorId(node)
        }
        if (type === JavaCCTreeConstants.JJTPRODUCTION) {
            return Production(node)
        }
        if (type === JavaCCTreeConstants.JJTBLOCK) {
            return Block(node)
        }
        if (type === JavaCCTreeConstants.JJTLOCALVARIABLEDECLARATION || type === JavaCCTreeConstants.JJTFIELDDECLARATION) {
            return VariableDeclaration(node)
        }
        if (type === JavaCCTreeConstants.JJTVARIABLEDECLARATOR) {
            return VariableDeclarator(node)
        }
        if (type === JavaCCTreeConstants.JJTJAVACC_INPUT) {
            return JavaccInput(node)
        }
        if (type === JavaCCTreeConstants.JJTREGULAR_EXPR_PRODUCTION) {
            return RegexpProduction(node)
        }
        return if (type === JavaCCTreeConstants.JJTREGEXPR_SPEC) {
            RegexpSpec(node)
        } else ASTWrapperPsiElement(node)
    }

    override fun createFile(viewProvider: FileViewProvider): PsiFile = JavaccFileImpl(viewProvider)

    override fun spaceExistanceTypeBetweenTokens(
        astNode: ASTNode?,
        astNode1: ASTNode?
    ): ParserDefinition.SpaceRequirements =
        ParserDefinition.SpaceRequirements.MAY
}
