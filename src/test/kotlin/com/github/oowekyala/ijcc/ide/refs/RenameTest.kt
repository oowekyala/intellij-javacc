package com.github.oowekyala.ijcc.ide.refs

import com.github.oowekyala.ijcc.lang.util.JccTestBase
import org.intellij.lang.annotations.Language

/**
 * @author Clément Fournier
 * @since 1.0
 */
class RenameTest : JccTestBase() {

    @Language("JavaCC")
    private val jjtreeNodeAndProductionTests =
        """
            $DummyHeader

            void Four/*caret[def]*/():{}
            {
                "4"
            }

            void Foo():{}
            {
                  Hello() "4" #Four/*caret[scope]*/
                | MyFour()
            }

            void Hello():{}
            {
                "4" #Four
            }

            void MyFour/*caret[myfour]*/() #Four/*caret[defannot]*/:{}
            {
                "4" Four/*caret[ref]*/()
            }
        """

    private fun jjtreeNodeRenameTest(caretId: String) {


        @Language("JavaCC")
        val result = """
            $DummyHeader

            void Five():{}
            {
                "4"
            }

            void Foo():{}
            {
                  Hello() "4" #Five
                | MyFour()
            }

            void Hello():{}
            {
                "4" #Five
            }

            void MyFour() #Five:{}
            {
                "4" Five()
            }
        """

        configureByText(jjtreeNodeAndProductionTests.selectCaretMarker(caretId))

        myFixture.renameElementAtCaret("Five")

        myFixture.checkResult(result)
    }




    fun `test rename JJTree node from prod definition`() {
        jjtreeNodeRenameTest("def")
    }

    fun `test rename JJTree node from scoped expansion unit`() {
        jjtreeNodeRenameTest("scope")
    }

    fun `test rename JJTree node from node descriptor on production`() {
        jjtreeNodeRenameTest("defannot")
    }

    fun `test rename JJTree node from production reference`() {
        jjtreeNodeRenameTest("ref")
    }

    fun `test rename production with node descriptor`() {


        @Language("JavaCC")
        val result = """
            $DummyHeader

            void Four():{}
            {
                "4"
            }

            void Foo():{}
            {
                  Hello() "4" #Four
                | MyFive()
            }

            void Hello():{}
            {
                "4" #Four
            }

            void MyFive() #Four:{}
            {
                "4" Four()
            }
        """

        // nothing changes except the MyFour production and its usages
        configureByText(jjtreeNodeAndProductionTests.selectCaretMarker("myfour"))

        myFixture.renameElementAtCaret("MyFive")

        myFixture.checkResult(result)
    }


}