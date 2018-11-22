package com.github.oowekyala.ijcc.lang.psi.impl

import com.github.oowekyala.ijcc.JavaccFileType
import com.github.oowekyala.ijcc.lang.JavaccTypes
import com.github.oowekyala.ijcc.lang.psi.*
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
    private fun <T : PsiElement> PsiElement.findChildOfType(clazz: Class<out T>): T? =
        PsiTreeUtil.findChildOfType(this, clazz)

    private val Project.psiManager
        get() = PsiManager.getInstance(this)


    private val Project.psiFileFactory
        get() = PsiFileFactory.getInstance(this)


    fun createLiteralRegex(project: Project, name: String): JccLiteralRegularExpression {
        val fileText = """
            PARSER_BEGIN(dummy)
            PARSER_END(dummy)

            void foo() {} { $name }
        """.trimIndent()
        val file = createFile(project, fileText)

        return file.nonTerminalProductions[0]
            .let { it as JccBnfProduction }
            .expansion
            .let { it as JccLiteralRegularExpression }
    }

    fun createIdentifier(project: Project, name: String): JccIdentifier {
        val fileText = """
            PARSER_BEGIN(dummy)
            PARSER_END(dummy)

            void $name() {} { "dummy" }
        """.trimIndent()
        val file = createFile(project, fileText)

        return file.nonTerminalProductions[0].nameIdentifier
    }


    fun createJavaExpression(project: Project, text: String): JccJavaExpression {
        val fileText = """
            PARSER_BEGIN(dummy)
            PARSER_END(dummy)

            void foo() {} { LOOKAHEAD({$text}) "dummy" }
        """.trimIndent()
        val file = createFile(project, fileText)

        return file.nonTerminalProductions[0].findChildOfType(JccLocalLookahead::class.java)!!.javaExpression!!
    }


    fun createJavaBlock(project: Project, text: String): JccJavaBlock {
        val fileText = """
            PARSER_BEGIN(dummy)
            PARSER_END(dummy)

            JAVACODE void foo() $text
        """.trimIndent()
        val file = createFile(project, fileText)

        return file.nonTerminalProductions[0].javaBlock
    }


    fun createAssignmentLhs(project: Project, text: String): JccJavaAssignmentLhs {
        val fileText = """
            PARSER_BEGIN(dummy)
            PARSER_END(dummy)

            void foo(): {} {
                $text = hello()
            }
        """.trimIndent()
        val file = createFile(project, fileText)

        return file.nonTerminalProductions[0]
            .let { it as? JccBnfProduction }
            ?.expansion
            .let { it as? JccAssignedExpansionUnit }
            .let { it as JccJavaAssignmentLhs }
    }


    fun createJavaNonterminalHeader(project: Project, text: String): JccJavaNonTerminalProductionHeader {
        val fileText = """
            PARSER_BEGIN(dummy)
            PARSER_END(dummy)

            JAVACODE $text {}
        """.trimIndent()
        val file = createFile(project, fileText)

        return file.nonTerminalProductions[0].header
    }

    fun createAcu(project: Project, text: String): JccJavaCompilationUnit {
        val fileText = """
            PARSER_BEGIN(dummy)
                $text
            PARSER_END(dummy)
        """.trimIndent()
        val file = createFile(project, fileText)

        return file.parserDeclaration.javaCompilationUnit!!
    }

    private fun createFile(project: Project, text: String): JccFileImpl =
        project.psiFileFactory.createFileFromText("dummy.javacc", JavaccFileType, text) as JccFileImpl

    /**
     * Create from an AST node, used by the parser.
     */
    fun createElement(node: ASTNode): PsiElement = JavaccTypes.Factory.createElement(node)
}


