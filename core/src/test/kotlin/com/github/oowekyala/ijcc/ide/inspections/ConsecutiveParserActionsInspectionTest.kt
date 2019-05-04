package com.github.oowekyala.ijcc.ide.inspections

/**
 * @author Clément Fournier
 * @since 1.1
 */
class ConsecutiveParserActionsInspectionTest : JccInspectionTestBase(ConsecutiveParserActionsInspection()) {

    private fun String.warn() = warningAnnot(this, ConsecutiveParserActionsInspection.ProblemDescription)


    fun `test warning range`() = checkByText("""
       $DummyHeader


        void Foo():
        {}
        {
            "foo" "bar" ${"{foo();} {bar;} {lol.fo();}".warn()} "flab" {weLiveInASociety();}
        }


    """)


}