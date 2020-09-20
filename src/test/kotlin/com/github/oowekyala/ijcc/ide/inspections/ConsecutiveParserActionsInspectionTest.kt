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

    fun `test warning range in node scope`() = checkByText("""
       $DummyHeader


        void Foo():
        {}
        {
            ("foo" "bar" ${"{foo();} {bar;}".warn()} {lol.fo();}) #F "flab" {weLiveInASociety();}
        }


    """)

    fun `test warning range in node scope 2`() = checkByText("""
       $DummyHeader


        void Foo():
        {}
        {
            "foo" "bar" ${"{foo();} {bar;}".warn()} {lol.fo();}
        }


    """)

    fun `test warning range in node scope 3`() = checkByText("""
       $DummyHeader


        void Foo():
        {}
        {
            "foo" "bar" {foo();} {bar;}
        }


    """)


}
