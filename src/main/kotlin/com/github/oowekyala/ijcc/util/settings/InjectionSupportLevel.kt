package com.github.oowekyala.ijcc.util.settings

import com.github.oowekyala.ijcc.lang.injection.InjectedJavaHighlightVisitor
import com.github.oowekyala.ijcc.lang.injection.JavaccLanguageInjector
import org.intellij.lang.annotations.Language

/**
 * Level of Java injection the plugin performs.
 *
 * @author Clément Fournier
 * @since 1.0
 */
enum class InjectionSupportLevel(val displayName: String, val description: String) {

    /** No injection. */
    DISABLED("Disabled", "No injection"),

    // TODO maybe have a level with a basic, fast injection scheme with no control flow reconstruction
    // if performance is still too bad for some users

    /** Enables [JavaccLanguageInjector]*/
    @Language("HTML")
    CONSERVATIVE(
        "Partial",
        """<html>
            Basic highlighting, code completion, quick documentation,
            usage resolution, control-flow analysis, inspections, etc.
            This level is already extremely potent, but <b>compilation errors
            are not flagged</b>.
           </html>""".trimIndent()
    ),

    /** Enables [InjectedJavaHighlightVisitor]. */
    @Language("HTML")
    FULL(
        "Full",
        """<html>
            Adds compilation error checking, including type checking &mdash;
            <b>everything is like a regular Java file</b>. Be aware that highlighting
            updates are most of the time quite slow.
           </html>""".trimIndent()
    )
}