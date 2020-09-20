package com.github.oowekyala.ijcc.ide.structureview

import com.github.oowekyala.ijcc.lang.util.JccTestBase
import com.intellij.testFramework.PlatformTestUtil.assertTreeEqual
import com.intellij.testFramework.PlatformTestUtil.expandAll
import org.intellij.lang.annotations.Language

/**
 * @author Clément Fournier
 * @since 1.1
 */
class JccStructureViewTest : JccTestBase() {
    fun `test tokens`() = doTest(
        """
        $DummyHeader

        <Astate> TOKEN: {
              <FOO: "hello">
            | <BAR: "hye"> { foo(); }
        }


        <Bstate> SPECIAL_TOKEN: {
              <SPECIAL: "SPECIAL"> : Astate
        }



    """, """
        -dummy.jjt
         parser class Dummy
         -TOKEN
          <FOO : "hello">
          <BAR : "hye">
         -SPECIAL_TOKEN
          <SPECIAL : "SPECIAL">
    """
    )

    fun `test synthetic tokens`() = doTest(
        """
        $DummyHeader

        <Astate> TOKEN: {
              <FOO: "hello">
            | <BAR: "hye"> { foo(); }
        }

        void Foo():
        {}
        {
            "flabberGasted" // synthetic
            "flabberGasted" // no duplicate
        }
        """,
        """
            -dummy.jjt
             parser class Dummy
             -TOKEN
              <FOO : "hello">
              <BAR : "hye">
             -Foo()
              <"flabberGasted">
            """
    )


    fun `test synthetic token duplicates`() = doTest(
        """
        $DummyHeader

        TOKEN: {
              <FOO: "hello">
            | <BAR: "hye"> { foo(); }
        }

        void Foo():
        {}
        {
            "hello" // refs the explicit
            "flabberGasted" // no duplicate
        }
    """,
        """
        -dummy.jjt
         parser class Dummy
         -TOKEN
          <FOO : "hello">
          <BAR : "hye">
         -Foo()
          <"flabberGasted">
         """
    )


    fun `test EOF regex is not added`() = doTest(
        """
        $DummyHeader

        void Foo():
        {}
        {
            <EOF>
        }
    """,
        """
        -dummy.jjt
         parser class Dummy
         Foo()
         """
    )

    fun `test java members are visible`() = doTest(
        """
                
        PARSER_BEGIN(Dummy)
        
        package dummy.grammar;
        
        public class Dummy {
        
            void zzcheckVersion(int v) {}
        
        }
        
        PARSER_END(Dummy)


        void Foo():
        {}
        {
            <EOF>
        }
    """,
        """
        -dummy.jjt
         -parser class Dummy
${generatedFieldsJjtree("          ")}
          zzcheckVersion(int): void
${generatedMethodsJjtree("          ")}
         Foo()
         """
    )

    private fun doTest(@Language("JavaCC") code: String, expected: String) {
        val normExpected = expected.trimIndent() + "\n"
        myFixture.configureByText("dummy.jjt", code)
        myFixture.testStructureView {
            expandAll(it.tree)
            assertTreeEqual(it.tree, normExpected)
        }
    }

    companion object {
        fun generatedFieldsJjtree(indent:String) =
            """
jjtree: JJTDummyState = new JJTDummyState()
token: Token
token_source: DummyTokenManager
            """.trim().replaceIndent(indent)

        fun generatedMethodsJjtree(indent: String) =
            """
getToken(int): Token
getNextToken(): Token
generateParseException(): ParseException
ReInit(DummyTokenManager): void""".trim().replaceIndent(indent)
    }
}
