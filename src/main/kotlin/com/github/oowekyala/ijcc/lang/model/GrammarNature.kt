package com.github.oowekyala.ijcc.lang.model

import com.github.oowekyala.ijcc.icons.JccIcons
import com.intellij.icons.AllIcons
import javax.swing.Icon

/**
 * Type of preprocessor used by a grammar. JJTree is a superset
 * of JavaCC so we can implement both with the exact same PSI.
 * The [nature][com.github.oowekyala.ijcc.lang.psi.grammarNature] of
 * a file is determined by file extension.
 *
 * Grammar natures are comparable, their ordering is determined
 * by inclusion relation of the languages.
 *
 * @property displayName The display name, in lower case. JavaCC and JJTree
 *                       should always be capitalized.
 *
 * @author Clément Fournier
 * @since 1.2
 */
@Suppress("MemberVisibilityCanBePrivate")
enum class GrammarNature(val displayName: String,
                         val conventionalExtension: String,
                         val icon: Icon) {
    JAVACC("JavaCC", "jj", JccIcons.JAVACC_FILE),
    JJTREE("JJTree", "jjt", JccIcons.JJTREE_FILE),
    /**
     * Special nature in which all features are enabled,
     * used in injection. Higher than all features.
     */
    UNKNOWN("unknown", "", AllIcons.FileTypes.Unknown);

    val dotAndExtension = ".$conventionalExtension"
}
