package com.github.oowekyala.ijcc.ide.refs

import com.github.oowekyala.ijcc.lang.psi.JjtNodeClassOwner
import com.github.oowekyala.ijcc.lang.psi.descendantSequence
import com.github.oowekyala.ijcc.lang.psi.typedReference
import com.github.oowekyala.ijcc.lang.util.JccTestBase
import io.kotest.matchers.collections.haveSize
import io.kotest.matchers.should

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


        val owner = file.descendantSequence().filterIsInstance<JjtNodeClassOwner>().first()

        owner.typedReference!!.multiResolve(false).toList() should haveSize(4)
    }


}
