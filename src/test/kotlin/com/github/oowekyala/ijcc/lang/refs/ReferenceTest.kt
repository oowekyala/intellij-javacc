package com.github.oowekyala.ijcc.lang.refs

import com.github.oowekyala.ijcc.lang.psi.JccNodeClassOwner
import com.github.oowekyala.ijcc.lang.psi.descendantSequence
import com.github.oowekyala.ijcc.lang.psi.typedReference
import com.github.oowekyala.ijcc.lang.util.JccTestBase
import io.kotlintest.matchers.haveSize
import io.kotlintest.should

/**
 * @author Clément Fournier
 * @since 1.0
 */
class ReferenceTest : JccTestBase() {

    fun testJjtreePolyRef() {

        val file = """

            $DummyHeader

            void Four():{}
            {
                "4"
            }

            void Foo():{}
            {
                Hello() "4" #Four
            }

            void Hello():{}
            {
                "4" #Four
            }



            void MyFour() #Four:{}
            {
                "4"
            }

        """.trimIndent().asJccFile()


        val owner = file.descendantSequence().filterIsInstance<JccNodeClassOwner>().first()

        owner.typedReference!!.multiResolve(false).toList() should haveSize(4)
    }


}