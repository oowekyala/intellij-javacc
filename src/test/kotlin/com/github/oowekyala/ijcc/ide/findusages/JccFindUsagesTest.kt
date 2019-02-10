package com.github.oowekyala.ijcc.ide.findusages

import com.github.oowekyala.ijcc.lang.psi.JccPsiElement
import com.github.oowekyala.ijcc.lang.util.JccTestBase
import com.intellij.psi.PsiElement
import org.intellij.lang.annotations.Language

/**
 * @author Clément Fournier
 * @since 1.1
 */
class JccFindUsagesTest : JccTestBase() {


    fun `test nonterminal usages`() = doTestByText(
        """
                $DummyHeader

                void Foo():
                    //^
                {}
                {
                    bar() "foo" "zlatan" Foo() $Nonterminal
                }

                void bar(): {}
                {
                    "f" Foo() $Nonterminal
                }

            """
    )


    fun `test token ref`() = doTestByText(
        """

            $DummyHeader

            TOKEN: {
                <foo: "fo" | "b">
                //^
            }


            TOKEN: {
                <bar: <foo>> $Token
            }


            void MyFour() #Four:{}
            {
                <foo> $Token
            }

        """
    )

    fun `test implicit token`() = doTestByText(
        """

            $DummyHeader

            void Four():{}
            {
                "4" $Token
               //^
            }

            void Foo():{}
            {
                Hello() "4" #Four $Token
            }

            void Hello():{}
            {
                "4" #Four $Token
            }



            void MyFour() #Four:{}
            {
                "4" $Token
            }

        """
    )

    fun `test implicit usage`() = doTestByText(
        """

            $DummyHeader

            void Four():{}
            {
                "4" $Token
            }

            void Foo():{}
            {
                Hello() "4" #Four $Token
                       //^
            }

            void Hello():{}
            {
                "4" #Four $Token
            }



            void MyFour() #Four:{}
            {
                "4" $Token
            }

        """
    )

    fun `test string usage of spec from string`() = doTestByText(
        """

            $DummyHeader

            TOKEN: {
                <four: "4"> $Token
            }

            void Four():{}
            {
                "4" $Token
            }

            void Foo():{}
            {
                Hello() "4" #Four $Token
                       //^
            }

            void Hello():{}
            {
                "4" #Four $Token
            }



            void MyFour() #Four:{}
            {
                "4" $Token
            }

        """
    )


    fun `test string usage of spec from name`() = doTestByText(
        """

            $DummyHeader

            TOKEN: {
                <four: "4"> $Token
                //^
            }

            void Four():{}
            {
                "4" $Token
            }

            void Foo():{}
            {
                Hello() "4" #Four $Token
            }

            void Hello():{}
            {
                "4" #Four $Token
            }



            void MyFour() #Four:{}
            {
                <four> $Token
            }

        """
    )


    fun `test string usage of spec from literal in spec`() = doTestByText(
        """

            $DummyHeader

            TOKEN: {
                <four: "4"> $Token
                      //^

            }

            void Four():{}
            {
                "4" $Token
            }

            void Foo():{}
            {
                Hello() "4" #Four $Token
            }

            void Hello():{}
            {
                "4" #Four $Token
            }



            void MyFour() #Four:{}
            {
                <four> $Token
            }

        """
    )

    // this is taken from the kotlin plugin i think
    private fun doTestByText(@Language("JavaCC") code: String) {
        configureByText(code)

        val source = findElementInEditor<JccPsiElement>()

        val actual = markersActual(source)
        val expected = markersFrom(code)
        assertEquals(expected.joinToString(COMPARE_SEPARATOR), actual.joinToString(COMPARE_SEPARATOR))
    }

    private fun markersActual(source: JccPsiElement) =
        myFixture.findUsages(source)
            .filter { it.element != null }
            .map { Pair(it.element?.line ?: -1, JccFindUsagesProvider().getType(it.element!!).split(" ")[0]) }

    private fun markersFrom(text: String) =
        text.split('\n')
            .withIndex()
            .filter { it.value.contains(MARKER) }
            .map { Pair(it.index, it.value.substring(it.value.indexOf(MARKER) + MARKER.length).trim()) }

    private companion object {
        const val MARKER = "// - "
        const val COMPARE_SEPARATOR = " | "
        const val Nonterminal = MARKER + "non-terminal"
        const val Token = MARKER + "token"
    }

    private val PsiElement.line: Int? get() = containingFile.viewProvider.document?.getLineNumber(textRange.startOffset)

}