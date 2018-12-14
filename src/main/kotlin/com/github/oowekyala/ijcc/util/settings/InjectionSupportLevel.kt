package com.github.oowekyala.ijcc.util.settings

import com.github.oowekyala.ijcc.lang.injection.InjectedJavaHighlightVisitor

/**
 * Level of injection to carry out.
 *
 * @author Clément Fournier
 * @since 1.0
 */
enum class InjectionSupportLevel(val displayName: String, val description: String) {
    DISABLED("Disabled", "No injection will be performed"),
    CONSERVATIVE("Partial", "Basic highlighting, code completion, quick documentation"),
    /** Enables [InjectedJavaHighlightVisitor]. */
    FULL("Full", "Type checking, richest highlighting (these are quite slow to update)")
}