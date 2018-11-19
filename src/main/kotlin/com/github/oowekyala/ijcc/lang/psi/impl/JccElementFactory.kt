package com.github.oowekyala.ijcc.lang.psi.impl

import com.github.oowekyala.ijcc.JavaccFileType
import com.github.oowekyala.ijcc.lang.JavaccTypes
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
    private fun <T : PsiElement> PsiElement.findChildOfType(clazz: Class<out T>): T? = PsiTreeUtil.findChildOfType(this, clazz)

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
    fun createElement(node: ASTNode): PsiElement = JavaccTypes.Factory.createElement(node)
}


