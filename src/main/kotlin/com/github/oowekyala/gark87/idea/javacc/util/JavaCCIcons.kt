package com.github.oowekyala.gark87.idea.javacc.util

import com.intellij.openapi.util.IconLoader
import javax.swing.Icon

/**
 * Provides icons for the plugin.
 */
enum class JavaCCIcons(fname: String) {
    /** Terminal icon for the structure view.  */
    TERMINAL("terminal.png"),
    /** Non-terminal icon for the structure view.  */
    NONTERMINAL("nonterminal.png"),
    /** File type icon.  */
    JAVACC_FILE("javacc.png");

    /** Returns the AWT icon.  */
    val icon: Icon by lazy { IconLoader.getIcon("../icons/$fname") }
}
