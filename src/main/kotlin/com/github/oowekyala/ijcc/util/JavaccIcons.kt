package com.github.oowekyala.ijcc.util

import com.github.oowekyala.ijcc.util.IconLoader.getIcon
import com.intellij.openapi.util.IconLoader
import javax.swing.Icon

/**
 * Provides icons for the plugin.
 */
enum class JavaccIcons(fname: String) : Icon by getIcon(fname) {
    /** Terminal icon for the structure view.  */
    TERMINAL("terminal.png"),
    /** Non-terminal icon for the structure view.  */
    NONTERMINAL("nonterminal.png"),
    /** File type icon.  */
    JAVACC_FILE("javaccFile.png");
}

private object IconLoader {
    fun getIcon(fname: String) = IconLoader.getIcon("../icons/$fname")
}
