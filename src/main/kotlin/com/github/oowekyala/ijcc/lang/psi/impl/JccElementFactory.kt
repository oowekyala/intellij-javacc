package com.github.oowekyala.ijcc.lang.psi.impl

import com.github.oowekyala.ijcc.JavaccFileType
import com.github.oowekyala.ijcc.insight.inspections.isJccComment
import com.github.oowekyala.ijcc.insight.model.RegexKind
import com.github.oowekyala.ijcc.lang.JccTypes
import com.github.oowekyala.ijcc.lang.psi.*
import com.intellij.ide.highlighter.JavaFileType
import com.intellij.lang.ASTNode
import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil
import org.intellij.lang.annotations.Language


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

    fun createEolComment(project: Project, name: String): PsiElement {
        val fileText = """
            // $name
            options {
             FOO = $name;
            }
            $DummyHeader

        """.trimIndent()

        val file = createFile(project, fileText)

        return file.firstChild.also { assert(it.isJccComment) }
    }


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

    fun createRegexReferenceUnit(project: Project, name: String): JccTokenReferenceUnit =
            createRegularExpressionReference(project, name).unit

    fun createRegularExpressionReference(project: Project, name: String): JccRegularExpressionReference {
        val fileText = """
            $DummyHeader

            void foo(): {} { $name }
        """.trimIndent()
        val file = createFile(project, fileText)

        return file.nonTerminalProductions.first()
            .let { it as JccBnfProduction }
            .expansion
            .let { it as JccRegexpExpansionUnit }
            .let { it.regularExpression as JccRegularExpressionReference }
    }


    fun createBracedExpansionUnit(project: Project, name: String): JccOptionalExpansionUnit =
            createBnfExpansion(project, name).let { it as JccOptionalExpansionUnit }

    fun createParenthesizedExpansionUnit(project: Project, name: String): JccParenthesizedExpansionUnit =
            createBnfExpansion(project, name).let { it as JccParenthesizedExpansionUnit }


    fun createLiteralRegexUnit(project: Project, name: String): JccLiteralRegexpUnit {
        return createBnfExpansion(project, name)
            .let { it as JccRegexpExpansionUnit }
            .let { it.regularExpression as JccLiteralRegularExpression }
            .let { it.unit }
    }

    fun createBnfExpansion(project: Project, name: String): JccExpansion {
        val fileText = """
            $DummyHeader

            void foo(): {} { $name }
        """.trimIndent()
        val file = createFile(project, fileText)

        return file.nonTerminalProductions.first()
            .let { it as JccBnfProduction }
            .expansion!!
    }

    fun createIdentifier(project: Project, name: String): JccIdentifier {
        val fileText = """
            $DummyHeader

            void $name() {} { "dummy" }
        """.trimIndent()
        val file = createFile(project, fileText)

        return file.nonTerminalProductions.first().nameIdentifier
    }

    inline fun <reified T : JccRegularExpression>
            createRegex(project: Project, text: String): T =
            createRegexSpec(project, RegexKind.TOKEN, text).regularExpression as T

    fun createRegexSpec(project: Project, kind: RegexKind, text: String): JccRegexprSpec {
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

        return file.nonTerminalProductions.first().findChildOfType(JccLocalLookahead::class.java)!!.javaExpression!!
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
            project.psiFileFactory.createFileFromText("dummy.jjt", JavaccFileType, text) as JccFile

    /**
     * Create from an AST node, used by the parser.
     */
    fun createElement(node: ASTNode): PsiElement = JccTypes.Factory.createElement(node)
    
    
    @Language("JavaCC")
    private const val DummyHeader = 
"""
PARSER_BEGIN(dummy)

public class dummy {}

PARSER_END(dummy)
"""
    
}


