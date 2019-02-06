package com.github.oowekyala.ijcc.insight.highlight

import com.github.oowekyala.ijcc.lang.psi.match
import com.github.oowekyala.ijcc.util.JccAnnotationTestBase
import groovyjarjarantlr.build.ANTLR.root

/**
 * @author Clément Fournier
 * @since 1.0
 */
class JccErrorHighlightTest : JccAnnotationTestBase() {



    fun testPrivateRegexReference()= checkByText(
        """
            TOKEN: {
             <#FOO: "foo">
            }

            void Foo():{} {
              <<error descr="Token name \"FOO\" refers to a private (with a #) regular expression">FOO</error>>
            }
        """.inGrammarCtx()
    )

    fun testPrivateRegexLiteralReference()= checkByText(
        """
            TOKEN: {
             <#FOO: "foo">
            }

            void Foo():{} {
              <error descr="String token \"foo\" has been defined as a private (#) regular expression">"foo"</error>
            }
        """.inGrammarCtx()
    )










}