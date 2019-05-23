package com.github.oowekyala.ijcc.ide.inspections

/**
 * @author Clément Fournier
 * @since 1.1
 */
class EmptyParserActionsInspectionTest : JccInspectionTestBase(EmptyParserActionsInspection()) {

    private fun String.warn() = warningAnnot(this, EmptyParserActionsInspection.ProblemDescription)


    fun `test warning range`() = checkByText("""
       $DummyHeader


        void Foo():
        {}
        {
            "foo" "bar" ${"{  }".warn()} "flab" {weLiveInASociety();}
        }


    """)


    fun `test warning not suppressed`() = checkByText("""
       $DummyHeader


        void Foo():
        {}
        {
            "foo" "bar" ${"{  }".warn()}
        }


    """)


    fun `test warning void`() = checkByText("""
       $DummyHeader


        void Foo() #void:
        {}
        {
            "foo" "bar" {goo();} ${"{  }".warn()}
        }


    """)

    fun `test warning suppressed 2`() = checkByText("""
       $DummyHeader


        void Foo():
        {}
        {
            ("foo" "bar" { jj(); } {  }) #A
        }


    """)


}
