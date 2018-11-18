package com.github.oowekyala.ijcc.lang.psi.impl

import com.github.oowekyala.ijcc.JavaccFileType
import com.github.oowekyala.ijcc.lang.JavaccTypes.*
import com.github.oowekyala.ijcc.lang.psi.JccIdentifier
import com.github.oowekyala.ijcc.lang.psi.JccJavaCompilationUnit
import com.github.oowekyala.ijcc.lang.psi.JccParserDeclaration
import com.github.oowekyala.ijcc.lang.psi.light.JccLightIdentifier
import com.intellij.lang.ASTNode
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiManager
import com.intellij.psi.util.PsiTreeUtil


/**
 * @author Clément Fournier
 * @since 1.0
 */

object JccElementFactory {
    fun <T : PsiElement> PsiElement.findChildOfType(clazz: Class<out T>): T? = PsiTreeUtil.findChildOfType(this, clazz)

    private val Project.psiManager
        get() = PsiManager.getInstance(this)


    private val Project.psiFileFactory
        get() = PsiFileFactory.getInstance(this)


    fun createIdentifier(project: Project, name: String): JccIdentifier {
        return JccLightIdentifier(project.psiManager, name)
    }

    fun createAcu(project: Project, text: String): JccJavaCompilationUnit {
        val fileText = """
            PARSER_BEGIN(dummy)
                $text
            PARSER_END(dummy)
        """.trimIndent()
        val file = createFile(project, fileText)

        return file.findChildOfType(JccParserDeclaration::class.java)!!.findChildOfType(JccJavaCompilationUnit::class.java)!!
    }

    private fun createFile(project: Project, text: String): JccFileImpl =
        project.psiFileFactory.createFileFromText("dummy.javacc", JavaccFileType, text) as JccFileImpl

    /**
     * Create from an AST node, used by the parser.
     */
    fun createElement(node: ASTNode): PsiElement {
        return when (node.elementType) {
            JCC_BNF_PRODUCTION -> JccBnfProductionImpl(node)
            JCC_CHARACTER_DESCRIPTOR -> JccCharacterDescriptorImpl(node)
            JCC_CHARACTER_LIST -> JccCharacterListImpl(node)
            JCC_COMPLEX_REGULAR_EXPRESSION -> JccComplexRegularExpressionImpl(node)
            JCC_COMPLEX_REGULAR_EXPRESSION_CHOICES -> JccComplexRegularExpressionChoicesImpl(node)
            JCC_COMPLEX_REGULAR_EXPRESSION_UNIT -> JccComplexRegularExpressionUnitImpl(node)
            JCC_EXPANSION -> JccExpansionImpl(node)
            JCC_EXPANSION_CHOICES -> JccExpansionChoicesImpl(node)
            JCC_EXPANSION_UNIT -> JccExpansionUnitImpl(node)
            JCC_IDENTIFIER -> JccIdentifierImpl(node)
            JCC_JAVACC_OPTIONS -> JccJavaccOptionsImpl(node)
            JCC_JAVACODE_PRODUCTION -> JccJavacodeProductionImpl(node)
            JCC_JAVA_BLOCK -> JccJavaBlockImpl(node)
            JCC_JAVA_COMPILATION_UNIT -> JccJavaCompilationUnitImpl(node)
            JCC_JAVA_EXPRESSION_LIST -> JccJavaExpressionListImpl(node)
            JCC_JAVA_FORMAL_PARAMETER -> JccJavaFormalParameterImpl(node)
            JCC_JAVA_NAME -> JccJavaNameImpl(node)
            JCC_JAVA_PARAMETER_LIST -> JccJavaParameterListImpl(node)
            JCC_JAVA_THROWS_LIST -> JccJavaThrowsListImpl(node)
            JCC_LEXICAL_STATE_LIST -> JccLexicalStateListImpl(node)
            JCC_LOCAL_LOOKAHEAD -> JccLocalLookaheadImpl(node)
            JCC_NON_TERMINAL_PRODUCTION_HEADER -> JccNonTerminalProductionHeaderImpl(node)
            JCC_ONE_OR_MORE -> JccOneOrMoreImpl(node)
            JCC_OPTION_BINDING -> JccOptionBindingImpl(node)
            JCC_PARSER_DECLARATION -> JccParserDeclarationImpl(node)
            JCC_REGEXPR_KIND -> JccRegexprKindImpl(node)
            JCC_REGEXPR_SPEC -> JccRegexprSpecImpl(node)
            JCC_REGULAR_EXPRESSION -> JccRegularExpressionImpl(node)
            JCC_REGULAR_EXPR_PRODUCTION -> JccRegularExprProductionImpl(node)
            JCC_REPETITION_RANGE -> JccRepetitionRangeImpl(node)
            JCC_TOKEN_MANAGER_DECLS -> JccTokenManagerDeclsImpl(node)
            JCC_TRY_CATCH_EXPANSION_UNIT -> JccTryCatchExpansionUnitImpl(node)
            JCC_ZERO_OR_MORE -> JccZeroOrMoreImpl(node)
            JCC_ZERO_OR_ONE -> JccZeroOrOneImpl(node)
            JCC_JJTREE_NODE_DESCRIPTOR -> JjtNodeDescriptorImpl(node)
            JCC_JJTREE_NODE_DESCRIPTOR_EXPR -> JjtNodeDescriptorExprImpl(node)

            else -> throw AssertionError("Unknown element type: ${node.elementType}")
        }
    }
}


