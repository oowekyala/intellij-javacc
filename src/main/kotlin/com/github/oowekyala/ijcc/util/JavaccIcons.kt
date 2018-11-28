package com.github.oowekyala.ijcc.util

import com.intellij.openapi.util.IconLoader
import com.intellij.util.PlatformIcons
import javax.swing.Icon

/**
 * Provides icons for the plugin.
 */
enum class JavaccIcons(icon: Icon) : Icon by icon {
    // Structure view

    /** Terminal (for headers).  */
    TOKEN_HEADER("terminal.png"),
    /** Terminal regex.  */
    TOKEN(PlatformIcons.FIELD_ICON),
    /** BNF production.  */
    BNF_PRODUCTION(PlatformIcons.METHOD_ICON),
    /** Javacc option. */
    JAVACC_OPTION(PlatformIcons.ANNOTATION_TYPE_ICON),
    /** Javacode production.  */
    JAVACODE_PRODUCTION(PlatformIcons.ABSTRACT_METHOD_ICON),

    // other

    /** File type icon.  */
    JAVACC_FILE("JJmono.png");

    // This is a FP!
    constructor(fname: String) : this(IconLoader.getIcon("../icons/$fname"))
}