package com.github.oowekyala.ijcc.icons

import com.intellij.openapi.util.IconLoader
import javax.swing.Icon

/**
 * These icons should *not* depend on icons.jar, ie
 * not use AllIcons and only use icons shipped in this jar.
 */
enum class JccCoreIcons(icon: Icon) : Icon by icon {

    /** File type icon.  */
    JAVACC_FILE("jccFile.svg"),
    JJTREE_FILE("jjtreeFile.svg"),
    ;

    constructor(fname: String) : this(IconLoader.getIcon(fname))

    companion object {

        fun default(): Icon = IconLoader.getIcon("jjtreeNodeLocate.svg")
    }
}

