package com.github.oowekyala.ijcc.util

import com.intellij.icons.AllIcons
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
    TOKEN(AllIcons.Nodes.Variable),
    /** BNF production.  */
    BNF_PRODUCTION(AllIcons.Nodes.Pointcut),
    /** Javacc option. */
    JAVACC_OPTION(PlatformIcons.ANNOTATION_TYPE_ICON),
    /** Javacode production.  */
    JAVACODE_PRODUCTION(AllIcons.Nodes.DisabledPointcut),
    TOKEN_MGR_DECLS(PlatformIcons.CLASS_ICON),
    PARSER_DECLARATION(PlatformIcons.CLASS_ICON),

    // other
    /** For [com.github.oowekyala.ijcc.ide.gutter.JjtreeNodeClassLineMarkerProvider]. */
    GUTTER_NODE_CLASS(AllIcons.Gutter.OverridenMethod),
    /** For [com.github.oowekyala.ijcc.ide.gutter.JjtreePartialDeclarationLineMarkerProvider]. */
    GUTTER_PARTIAL_DECL(AllIcons.General.Locate),
    /** File type icon.  */
    JAVACC_FILE("JJmono.png");

    constructor(fname: String) : this(IconLoader.getIcon("../icons/$fname"))
}