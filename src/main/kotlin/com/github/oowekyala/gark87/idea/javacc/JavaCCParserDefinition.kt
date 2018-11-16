package com.github.oowekyala.gark87.idea.javacc

import com.github.oowekyala.gark87.idea.javacc.generated.JavaCC
import com.github.oowekyala.gark87.idea.javacc.generated.JavaCCElementTypes
import com.github.oowekyala.gark87.idea.javacc.generated.JavaCCLexer
import com.github.oowekyala.gark87.idea.javacc.generated.JavaCCTreeConstants
import com.github.oowekyala.gark87.idea.javacc.psi.*
import com.intellij.extapi.psi.ASTWrapperPsiElement
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
 * @author gark87
 */
class JavaCCParserDefinition : ParserDefinition {

    override fun createLexer(project: Project): Lexer = JavaCCLexer()

    override fun createParser(project: Project): PsiParser = JavaCCParser()

    override fun getFileNodeType(): IFileElementType = JavaCCElementTypes.FILE

    override fun getWhitespaceTokens(): TokenSet = WHITESPACES

    override fun getCommentTokens(): TokenSet = COMMENTS

    override fun getStringLiteralElements(): TokenSet = STRINGS

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

    override fun spaceExistanceTypeBetweenTokens(astNode: ASTNode?, astNode1: ASTNode?): ParserDefinition.SpaceRequirements =
            ParserDefinition.SpaceRequirements.MAY

    companion object {
        private val WHITESPACES = TokenSet.create(JavaCC.SKIP)
        private val COMMENTS = TokenSet.create(JavaCC.MULTI_LINE_COMMENT, JavaCC.SINGLE_LINE_COMMENT, JavaCC.FORMAL_COMMENT)
        private val STRINGS = TokenSet.create(JavaCC.STRING_LITERAL)
    }
}
