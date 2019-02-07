// This is a generated file. Not intended for manual editing.
package com.github.oowekyala.ijcc.lang.psi

/**
 * Represents a regular expression. Distinct from the [JccRegexElement]
 * tree, which is why [safeReplace] should be used to ensure the replaced
 * element is in the correct tree.
 */
interface JccRegularExpression : JccRegexLike {

    val pattern: Regex?
    val prefixPattern: Regex?

}
