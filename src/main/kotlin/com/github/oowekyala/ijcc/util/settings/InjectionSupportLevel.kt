package com.github.oowekyala.ijcc.util.settings

import com.github.oowekyala.ijcc.lang.injection.InjectedJavaHighlightVisitor
import com.github.oowekyala.ijcc.lang.injection.JavaccLanguageInjector

/**
 * Level of injection to carry out.
 *
 * @author Clément Fournier
 * @since 1.0
 */
enum class InjectionSupportLevel(val displayName: String, val description: String) {

    /** No injection. */
    DISABLED("Disabled", "No injection"),

    // TODO maybe have a level with a basic injection like in the old days before the InjectionStructureTree
    // if performance is still too bad for some users

    /** Enables [JavaccLanguageInjector]*/
    CONSERVATIVE(
        "Partial",
        """Basic highlighting, code completion, quick documentation, usage resolution, control-flow analysis, inspections, etc.
            |This level is already extremely potent, but compilation errors are not flagged and there's no semantic highlighting.""".trimMargin()
    ),

    /** Enables [InjectedJavaHighlightVisitor]. */
    FULL(
        "Full",
        """Adds compilation error detection, including type checking... everything like a regular Java file.
          |Be aware that those highlighting updates are most of the time quite slow.""".trimMargin()
    )
}