package com.github.oowekyala.ijcc.lang.psi.impl

import com.github.oowekyala.ijcc.JavaccFileType
import com.github.oowekyala.ijcc.JjtreeFileType
import com.github.oowekyala.ijcc.lang.JccTypes
import com.github.oowekyala.ijcc.lang.model.RegexKind
import com.github.oowekyala.ijcc.lang.psi.*
import com.intellij.lang.ASTNode
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiParserFacade
import com.intellij.psi.util.PsiTreeUtil
import org.intellij.lang.annotations.Language


/**
 * @author Clément Fournier
 * @since 1.0
 */

open class JccElementFactory(val project: Project) {


    fun <T : PsiElement> PsiElement.findChildOfType(clazz: Class<out T>): T? =
        PsiTreeUtil.findChildOfType(this, clazz)


    private val Project.psiFileFactory
        get() = PsiFileFactory.getInstance(this)


    fun insertEolCommentBefore(anchor: PsiElement, name: String) {
        val parserFacade = PsiParserFacade.SERVICE.getInstance(project)

        val comment = parserFacade.createLineCommentFromText(JavaccFileType(), name)
        anchor.parent.addBefore(comment, anchor)
        val eol = parserFacade.createWhiteSpaceFromText("\n")
        anchor.parent.addBefore(eol, anchor)
    }

    fun createOptionValue(name: String): JccOptionValue {
        val fileText = """
            options {
             FOO = $name;
            }
            $DummyHeader

        """.trimIndent()
        val file = createFile(fileText)

        return file.options!!.optionBindingList[0].optionValue!!
    }


    fun createIdentifier(name: String): JccIdentifier {
        val fileText = """
            $DummyHeader

            void $name() {} { "dummy" }
        """.trimIndent()
        val file = createFile(fileText)

        return file.nonTerminalProductions.first().nameIdentifier
    }

    inline fun <reified T : JccExpansion>
        createExpansion(text: String): T {

        val fileText = """
            $DummyHeader

            void foo(): {} { $text }
        """.trimIndent()
        val file = createFile(fileText)

        return file.nonTerminalProductions.first()
            .let { it as JccBnfProduction }
            .expansion!! as T
    }


    inline fun <reified T : JccRegularExpression>
        createRegex(text: String): T =
        createRegexSpec(RegexKind.TOKEN, text).regularExpression as T

    inline fun <reified T : JccRegexElement>
        createRegexElement(text: String): T =
        createRegex<JccContainerRegularExpression>("< $text >").regexElement as T


    fun createRegexSpec(kind: RegexKind, text: String): JccRegexSpec {
        val fileText = """
            $DummyHeader

            $kind: {
                $text
            }

        """.trimIndent()
        val file = createFile(fileText)

        return file.globalTokenSpecs.first()
    }

    fun createJavaExpression(text: String): JccJavaExpression {
        val fileText = """
            $DummyHeader

            void foo() {} { LOOKAHEAD({$text}) "dummy" }
        """.trimIndent()
        val file = createFile(fileText)

        return file.nonTerminalProductions.first().findChildOfType(JccLocalLookaheadUnit::class.java)!!.javaExpression!!
    }


    fun createJavaBlock(text: String): JccJavaBlock {
        val fileText = """
            $DummyHeader

            JAVACODE void foo() $text
        """.trimIndent()
        val file = createFile(fileText)

        return file.nonTerminalProductions.first().javaBlock!!
    }


    fun createAssignmentLhs(text: String): JccJavaAssignmentLhs {
        val fileText = """
            $DummyHeader

            void foo(): {} {
                $text = hello()
            }
        """.trimIndent()
        val file = createFile(fileText)

        return file.nonTerminalProductions.first()
            .let { it as JccBnfProduction }
            .expansion
            .let { it as JccAssignedExpansionUnit }
            .let { it.javaAssignmentLhs }
    }


    fun createJavaNonterminalHeader(text: String): JccJavaNonTerminalProductionHeader {
        val fileText = """
            $DummyHeader

            JAVACODE $text {}
        """.trimIndent()
        val file = createFile(fileText)

        return file.nonTerminalProductions.first().header
    }

    fun createJcu(text: String): JccJavaCompilationUnit {
        val fileText = """
            PARSER_BEGIN(dummy)
            $text
            PARSER_END(dummy)
        """.trimIndent()
        val file = createFile(fileText)

        return file.parserDeclaration!!.javaCompilationUnit!!
    }

    fun createFile(text: String): JccFile =
        project.psiFileFactory.createFileFromText("dummy.jjt", JjtreeFileType(), text) as JccFile

    /**
     * Create from an AST node, used by the parser.
     */
    fun createElement(node: ASTNode): PsiElement = JccTypes.Factory.createElement(node)


    companion object {
        @Language("JavaCC")
        const val DummyHeader = """
                PARSER_BEGIN(dummy)

                public class dummy {}

                PARSER_END(dummy)
                """


        /**
         * Create from an AST node, used by the parser.
         */
        fun createElement(node: ASTNode): PsiElement = JccTypes.Factory.createElement(node)

    }

}


val Project.jccEltFactory: JccElementFactory
    get() = JccElementFactory(this)
