package com.github.oowekyala.ijcc.ide.inspections

import com.github.oowekyala.ijcc.ide.inspections.BnfStringCanNeverBeMatchedInspection.Companion.problemDescription

/**
 * @author Clément Fournier
 * @since 1.1
 */
class BnfStringCanNeverBeMatchedInspectionTest : JccInspectionTestBase(BnfStringCanNeverBeMatchedInspection()) {


    private fun String.warning(missedMatchName: String?, realMatchName: String?, literalText: String = this) =
        warningAnnot(this, problemDescription(literalText, missedMatchName, realMatchName))

    fun `test neg`() = checkByText(
        """
            $DummyHeader

           TOKEN: {
               <FOO: "foo">
             | <BAR: "bar" | "foo">
           }

           void Foo():
           {}
           {
               "foo"
           }


        """
    )

    fun `test synthetic neg`() = checkByText(
        """
            $DummyHeader

           void Foo():
           {}
           {
               "foo" "foo"
           }


        """
    )

    fun `test pos`() = checkByText(
        """
            $DummyHeader

           TOKEN: {
               <BAR: "bar" | "foo">
             | <FOO: "foo">
           }

           void Foo():
           {}
           {
               ${"\"foo\"".warning("FOO", "BAR")}
           }
        """
    )


    fun `test non-literal override neg`() = checkByText(
        """
            $DummyHeader

           TOKEN: {
               < "foo" > // has higher precedence
             | < NCNAME: (["a"-"z"])+ >
           }

           void Foo():
           {}
           {
               "foo"
           }
        """
    )

    fun `test non-literal override pos`() = checkByText(
        """
            $DummyHeader

           TOKEN: {
                < NCNAME: (["a"-"z"])+ >
           }

           void Foo():
           {}
           {
               ${"\"foo\"".warning(null, "NCNAME")}
           }
        """
    )


    fun `test in different state neg`() = checkByText(
        """
           $DummyHeader

           <A> TOKEN: {
               <NCNAME: (["a"-"z"])+ >
           }

           void Foo():
           {}
           {
              "foo"
           }
        """
    )


    fun `test in explicit default state pos`() = checkByText(
        """
           $DummyHeader

           <DEFAULT> TOKEN: {
               <NCNAME: (["a"-"z"])+ >
           }

           void Foo():
           {}
           {
               ${"\"foo\"".warning(null, "NCNAME")}
           }
        """
    )

    fun `test synthetic has precedence neg`() = checkByText(
        """
            $DummyHeader


           void Foo():
           {}
           {
               "foo"
           }
           
           TOKEN: {
             < NCNAME: (["a"-"z"])+ >
           }
        """
    )

    fun `test reference count as string tokens pos`() = checkByText(
        """
            $DummyHeader

             TOKEN: {
                <foo:<boo>>
             }

             void Foo():
             {}
             {
                 ${"<boo : \"foo\">".warning(literalText = "\"foo\"", missedMatchName = "boo", realMatchName = "foo")}
                 <foo> "foo"
             }
        """
    )

    /*
TODO
    PARSER_END(Foo)
      TOKEN: {
        <foo:<boo>>
      | <boo:"str">
      // JavaCC says "str" cannot be matched as <boo>,
      // will always be matched as <foo>, this is a warning though
   }

     */

}