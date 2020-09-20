package com.github.oowekyala.ijcc.ide.quickdoc

import com.github.oowekyala.ijcc.ide.quickdoc.HtmlUtil.angles
import com.github.oowekyala.ijcc.ide.quickdoc.HtmlUtil.bold
import com.github.oowekyala.ijcc.ide.quickdoc.HtmlUtil.psiLink
import com.github.oowekyala.ijcc.ide.quickdoc.JccDocUtil.buildQuickDoc
import com.github.oowekyala.ijcc.ide.quickdoc.JccDocUtil.linkRefToLexicalState
import com.github.oowekyala.ijcc.ide.quickdoc.JccNonTerminalDocMaker.BnfSectionName
import com.github.oowekyala.ijcc.ide.quickdoc.JccNonTerminalDocMaker.JJTreeSectionName
import com.github.oowekyala.ijcc.ide.quickdoc.JccNonTerminalDocMaker.StartSectionName
import com.github.oowekyala.ijcc.ide.quickdoc.JccTerminalDocMaker.makeDocImpl
import com.github.oowekyala.ijcc.lang.model.LexicalState
import com.github.oowekyala.ijcc.lang.model.RegexKind
import com.github.oowekyala.ijcc.lang.util.JccTestBase
import com.intellij.codeInsight.documentation.DocumentationManager
import com.intellij.psi.PsiElement
import org.intellij.lang.annotations.Language

/**
 * @author Clément Fournier
 * @since 1.0
 */
class JccQuickdocTest : JccTestBase() {

    private val myDummyPackage = "dummy.grammar.doctest"

    private val myDummyHeader = """
        PARSER_BEGIN(Dummy)

        package $myDummyPackage;

        PARSER_END(Dummy)
    """


    private fun simpleFooDoc(noJjtreeSection: Boolean = false) = buildQuickDoc {
        definition { "void Foo()" }
        sections {
            section(BnfSectionName, sectionDelim = " ::=") {

                val foo = psiLink("null", angles("foo"))

                val hey = "\"hey\"".unnamedTokenLink(0)
                val i = "\"i\"".unnamedTokenLink(1)

                "$hey ( $i | $foo )"
            }
            section(JJTreeSectionName) {
                if (noJjtreeSection) "none"
                else psiLink("$myDummyPackage.ASTFoo", "ASTFoo")
            }
        }

        sections {
            section(StartSectionName) {
                "&nbsp;&nbsp;" + "\"hey\"".unnamedTokenLink(0)
            }
        }
    }

    private val simpleFooTokenDoc = buildQuickDoc {
        definition { "TOKEN\t${bold(angles("FOO"))}" }
        sections {
            section("Expansion") { "&quot;foo&quot;" }
            section("Case-sensitive") { "true" }
            section(header = "Lexical states") {
                psiLink(
                    linkTarget = linkRefToLexicalState("DEFAULT"),
                    linkTextUnescaped = "DEFAULT"
                )
            }
        }
    }

    fun `test prod declaration doc`() = doTest(
        """
        $myDummyHeader

        void Foo():
            //^
        {}
        {
            "hey" ( "i" | <foo> )
        }

    """, simpleFooDoc()
    )

    fun `test nonterminal reference`() = doTest(
        """
        $myDummyHeader

        void Foo():
        {}
        {
            "hey" ( "i" | <foo> )
        }

        void bar():
        {}
        {
            "hohoho" | "helo" Foo()
                             //^
        }

    """,
        simpleFooDoc()
    )


    fun `test token reference`() = doTest(
        """
        $myDummyHeader

        TOKEN: {
          <FOO : "foo" >
        | <BAR : ("bar") >
        }

        void Foo():
        {}
        {
            "hey" ( "i" | <FOO> )
                         //^
        }
    """,
        simpleFooTokenDoc
    )

    fun `test token reference inside other token definition`() = doTest(
        """
        $myDummyHeader

        TOKEN: {
          <FOO : "foo" >
        | <BAR : ("bar") | <FOO> >
                           //^

        }
    """,
        simpleFooTokenDoc
    )

    fun `test token link inside token definition`() = doTest(
        """
        $myDummyHeader

        TOKEN: {
          <FOO : "foo" >
        | <BAR : ("bar") | <FOO> >
         //^

        }
    """,
        buildQuickDoc {
            definition { "TOKEN\t" + bold(angles("BAR")) }
            sections {
                section(header = "Expansion") { "( &quot;bar&quot; ) | " + psiLink("token/FOO", "\"foo\"") }
                section("Case-sensitive") { "true" }
                section(header = "Lexical states") { psiLink(linkRefToLexicalState("DEFAULT"), "DEFAULT") }
            }
        }
    )


    fun `test doc for jjtree scoped expansion`() = doTest(
        """
        $myDummyHeader


        void Foo():
        {}
        {
           "foo" "hey" #Hey
                      //^
        }

        void Hey(): // here to test it's the doc of the annot and not the production
        {}
        {
            "kk"
        }
    """,
        buildQuickDoc {
            definition { "#Hey" }
            sections {
                section(header = "BNF", sectionDelim = " ::=") {
                    "\"hey\"".unnamedTokenLink(1) // test that the doc doesn't include the "foo"
                }
                section(JJTreeSectionName) {
                    psiLink("$myDummyPackage.ASTHey", "ASTHey")
                }
            }
        }
    )


    fun `test doc for jjtree scoped expansion with partial decls`() = doTest(
        """
        $myDummyHeader


        void Foo():
        {}
        {
           "foo" #Hey "hey" #Hey
                           //^
        }

    """,
        buildQuickDoc {
            definition { "#Hey" }
            sections {
                section(header = "BNF", sectionDelim = " ::=") {
                    "\"hey\"".unnamedTokenLink(1)
                }
                section(JJTreeSectionName) {
                    psiLink("$myDummyPackage.ASTHey", "ASTHey")
                }
            }
        }
    )


    fun `test void prod declaration doc`() = doTest(
        """
        $myDummyHeader

        void Foo() #void:
            //^
        {}
        {
            "hey" #Foo(true) ( "i" | <foo> )
        }

    """, simpleFooDoc(noJjtreeSection = true)
    )


    fun `test prod declaration in default void grammar`() = doTest(
        """
        options {
          NODE_DEFAULT_VOID = true;
        }

        $myDummyHeader

        void Foo(): // no #void
            //^
        {}
        {
            "hey" ( "i" | <foo> )
        }

    """, simpleFooDoc(noJjtreeSection = true)
    )


    fun `test implicit string token decl`() = doTest(
        """
        options {
          NODE_DEFAULT_VOID = true;
        }

        $myDummyHeader

        void Foo(): // no #void
        {}
        {
            "hey" ( "i" | <foo> )
            //^
        }

    """,
        makeSyntheticDoc(null, "\"hey\"".unnamedTokenLink(0))
    )


    fun `test implicit named token decl`() = doTest(
        """
        options {
          NODE_DEFAULT_VOID = true;
        }

        $myDummyHeader

        void Foo(): // no #void
        {}
        {
            <ab: "a"|"b"> ( "i" | <foo> )
            //^
        }

    """,
        makeSyntheticDoc("ab", "&quot;a&quot; | &quot;b&quot;")
    )

    fun `test link from synthetic`() = doTest(
        """
        options {
          NODE_DEFAULT_VOID = true;
        }

        $myDummyHeader


        TOKEN: {
          <FOO : "foo" >
        | <BAR : ("bar") | <FOO> >
        }

        void Foo(): // no #void
        {}
        {
            <ab: "a"|<BAR>> ( "i" | <foo> )
            //^
        }

    """,
        makeSyntheticDoc("ab", "&quot;a&quot; | " + psiLink("token/BAR", "<BAR>"))
    )


    fun `test caret in uninteresting place 1`() = expectNothing(
        """
        options {
          NODE_DEFAULT_VOID = true;
        }

        $myDummyHeader

        void Foo(): // no #void
        {}
        {
            "hey" ( "i" | <foo> )
                //^
        }
    """
    )

    fun `test caret in uninteresting place 2`() = expectNothing(
        """
        options {
          NODE_DEFAULT_VOID = true;
        }
        $myDummyHeader

        void Foo(int foo): // no #void
                //^
        {}
        {
            "hey" ( "i" | <foo> )
        }
    """
    )

    fun `test caret on unresolved`() = expectNothing(
        """
        options {
          NODE_DEFAULT_VOID = true;
        }
        $myDummyHeader

        void Foo(int foo): // no #void
        {}
        {
            "hey" ( "i" | <foo> )
                          //^
        }
    """
    )

    private fun expectNothing(@Language("JavaCC") code: String) = doTest(code, null)


    private fun doTest(@Language("JavaCC") code: String, @Language("Html") expected: String?) {
        configureByText(code)

        val (originalElement, _, offset) = findElementWithDataAndOffsetInEditor<PsiElement>()
        val element = DocumentationManager.getInstance(project)
            .findTargetElement(myFixture.editor, offset, myFixture.file, originalElement)

        val actual = JccDocumentationProvider.generateDoc(element, originalElement)?.trim()

        when {
            actual == null && expected == null -> return
            expected == null                   -> error("Expected null result")
            actual == null                     -> error("Expected non-null result")
            else                               -> assertSameLines(expected.trimIndent(), actual)
        }
    }

    private fun makeSyntheticDoc(name: String?, expansion: String) =
        makeDocImpl(
            name = name,
            kind = RegexKind.TOKEN,
            isExplicit = false,
            isIgnoreCase = false,
            states = LexicalState.JustDefaultState
        ) { it.append(expansion) }


    private fun String.unnamedTokenLink(i: Int) =
        psiLink(
            linkTarget = JccDocUtil.linkRefToStringToken(i),
            linkTextUnescaped = this
        )

}
