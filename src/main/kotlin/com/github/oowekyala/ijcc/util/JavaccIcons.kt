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
    LEXICAL_STATE(TOKEN),
    /** BNF production.  */
    BNF_PRODUCTION(AllIcons.Nodes.Method),
    /** Javacc option. */
    JAVACC_OPTION(PlatformIcons.ANNOTATION_TYPE_ICON),
    /** Javacode production.  */
    JAVACODE_PRODUCTION(AllIcons.Nodes.AbstractMethod),
    TOKEN_MGR_DECLS(PlatformIcons.CLASS_ICON),
    PARSER_DECLARATION(PlatformIcons.CLASS_ICON),

    JJTREE_NODE(PlatformIcons.INTERFACE_ICON),

    // other
    /** For [com.github.oowekyala.ijcc.ide.gutter.JjtreeNodeClassLineMarkerProvider]. */
    GUTTER_NODE_CLASS(AllIcons.Gutter.OverridenMethod),
    /** For [com.github.oowekyala.ijcc.ide.gutter.JjtreePartialDeclarationLineMarkerProvider]. */
    GUTTER_PARTIAL_DECL(AllIcons.General.Locate),

    GUTTER_RECURSION(AllIcons.Gutter.RecursiveMethod),
    /** File type icon.  */
    JAVACC_FILE("JJmono.png");

    constructor(fname: String) : this(IconLoader.getIcon("../icons/$fname"))
}