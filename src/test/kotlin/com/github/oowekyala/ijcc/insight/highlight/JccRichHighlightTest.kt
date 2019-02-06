package com.github.oowekyala.ijcc.insight.highlight

import com.github.oowekyala.ijcc.util.JccAnnotationTestBase
import org.junit.Ignore

/**
 * FIXME info annots are not checked!
 *
 * @author Clément Fournier
 * @since 1.0
 */
@Ignore
class JccRichHighlightTest : JccAnnotationTestBase() {


    fun testNormalRegexLiteralReference() = checkByText(
        """
            TOKEN: {
             <FOO: "foo">
            }

            void Foo():{} {
              <info descr="Matched by \"foo\" has been defined as a private (#) regular expression">"foo"</info>
            }
        """.inGrammarCtx(),
        checkInfo = true
    )


}