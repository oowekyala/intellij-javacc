package com.github.oowekyala.ijcc.lang.psi.impl

import com.github.oowekyala.ijcc.JavaccFileType
import com.github.oowekyala.ijcc.JjtreeFileType
import com.github.oowekyala.ijcc.lang.JccTypes
import com.github.oowekyala.ijcc.lang.model.RegexKind
import com.github.oowekyala.ijcc.lang.psi.*
import com.intellij.ide.highlighter.JavaFileType
import com.intellij.lang.ASTNode
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiParserFacade
import com.intellij.psi.util.PsiTreeUtil
import org.intellij.lang.annotations.Language


/**
 * @author Clément Fournier
 * @since 1.0
 */

object JccElementFactory {
    private fun <T : PsiElement> PsiElement.findChildOfType(clazz: Class<out T>): T? =
        PsiTreeUtil.findChildOfType(this, clazz)


    private val Project.psiFileFactory
        get() = PsiFileFactory.getInstance(this)


    fun insertEolCommentBefore(project: Project, anchor: PsiElement, name: String) {
        val parserFacade = PsiParserFacade.SERVICE.getInstance(project)

        val comment = parserFacade.createLineCommentFromText(JavaccFileType, name)
        anchor.parent.addBefore(comment, anchor)
        val eol = parserFacade.createWhiteSpaceFromText("\n")
        anchor.parent.addBefore(eol, anchor)
    }


    fun createOptionValue(project: Project, name: String): JccOptionValue {
        val fileText = """
            options {
             FOO = $name;
            }
            $DummyHeader

        """.trimIndent()
        val file = createFile(project, fileText)

        return file.options!!.optionBindingList[0].optionValue!!
    }


    fun createIdentifier(project: Project, name: String): JccIdentifier {
        val fileText = """
            $DummyHeader

            void $name() {} { "dummy" }
        """.trimIndent()
        val file = createFile(project, fileText)

        return file.nonTerminalProductions.first().nameIdentifier
    }

    inline fun <reified T : JccExpansion>
        createExpansion(project: Project, text: String): T {

        val fileText = """
            $DummyHeader

            void foo(): {} { $text }
        """.trimIndent()
        val file = createFile(project, fileText)

        return file.nonTerminalProductions.first()
            .let { it as JccBnfProduction }
            .expansion!! as T
    }


    inline fun <reified T : JccRegularExpression>
        createRegex(project: Project, text: String): T =
        createRegexSpec(project, RegexKind.TOKEN, text).regularExpression as T

    inline fun <reified T : JccRegexElement>
        createRegexElement(project: Project, text: String): T =
        createRegex<JccContainerRegularExpression>(project, "< $text >").regexElement as T


    fun createRegexSpec(project: Project, kind: RegexKind, text: String): JccRegexSpec {
        val fileText = """
            $DummyHeader

            $kind: {
                $text
            }

        """.trimIndent()
        val file = createFile(project, fileText)

        return file.globalTokenSpecs.first()
    }

    fun createJavaExpression(project: Project, text: String): JccJavaExpression {
        val fileText = """
            $DummyHeader

            void foo() {} { LOOKAHEAD({$text}) "dummy" }
        """.trimIndent()
        val file = createFile(project, fileText)

        return file.nonTerminalProductions.first().findChildOfType(JccLocalLookaheadUnit::class.java)!!.javaExpression!!
    }


    fun createJavaBlock(project: Project, text: String): JccJavaBlock {
        val fileText = """
            $DummyHeader

            JAVACODE void foo() $text
        """.trimIndent()
        val file = createFile(project, fileText)

        return file.nonTerminalProductions.first().javaBlock!!
    }


    fun createAssignmentLhs(project: Project, text: String): JccJavaAssignmentLhs {
        val fileText = """
            $DummyHeader

            void foo(): {} {
                $text = hello()
            }
        """.trimIndent()
        val file = createFile(project, fileText)

        return file.nonTerminalProductions.first()
            .let { it as JccBnfProduction }
            .expansion
            .let { it as JccAssignedExpansionUnit }
            .let { it.javaAssignmentLhs }
    }


    fun createJavaNonterminalHeader(project: Project, text: String): JccJavaNonTerminalProductionHeader {
        val fileText = """
            $DummyHeader

            JAVACODE $text {}
        """.trimIndent()
        val file = createFile(project, fileText)

        return file.nonTerminalProductions.first().header
    }

    fun createJcu(project: Project, text: String): JccJavaCompilationUnit {
        val fileText = """
            PARSER_BEGIN(dummy)
                $text
            PARSER_END(dummy)
        """.trimIndent()
        val file = createFile(project, fileText)

        return file.parserDeclaration!!.javaCompilationUnit!!
    }

    fun createJavaMethodForNonterminal(project: Project, header: JccJavaNonTerminalProductionHeader): PsiMethod {
        val text = """
            class Foo {
                ${header.toJavaMethodHeader()} {

                }
            }
        """.trimIndent()

        return project.psiFileFactory.createFileFromText("dummy.java", JavaFileType.INSTANCE, text)
            .findChildOfType(PsiMethod::class.java)!!
    }

    fun createFile(project: Project, text: String): JccFile =
        project.psiFileFactory.createFileFromText("dummy.jjt", JjtreeFileType, text) as JccFile

    /**
     * Create from an AST node, used by the parser.
     */
    fun createElement(node: ASTNode): PsiElement = JccTypes.Factory.createElement(node)


    @Language("JavaCC")
    const val DummyHeader = """
                PARSER_BEGIN(dummy)

                public class dummy {}

                PARSER_END(dummy)
                """

}


