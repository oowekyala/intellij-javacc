package com.github.oowekyala.ijcc.icons

import com.intellij.icons.AllIcons
import com.intellij.openapi.util.IconLoader
import com.intellij.util.PlatformIcons
import javax.swing.Icon

/**
 * Provides icons for the plugin.
 */
enum class JccIcons(icon: Icon) : Icon by icon {
    // Structure view

    TOKEN_HEADER("terminal.svg"),
    TOKEN(TOKEN_HEADER),
    LEXICAL_STATE(TOKEN),
    VOID_BNF_PRODUCTION(AllIcons.Nodes.Method),
    VOID_JAVACODE_PRODUCTION(VOID_BNF_PRODUCTION),
    JJT_BNF_PRODUCTION("bnfProdAndNode.svg"),
    JJT_JAVACODE_PRODUCTION(JJT_BNF_PRODUCTION),
    JAVACC_OPTION(PlatformIcons.ANNOTATION_TYPE_ICON),
    TOKEN_MGR_DECLS(PlatformIcons.CLASS_ICON),
    PARSER_DECLARATION(PlatformIcons.CLASS_ICON),


    JJTREE_NODE("jjtreeNode.svg"),

    // other
    /** For [com.github.oowekyala.ijcc.ide.gutter.JjtreeNodeClassLineMarkerProvider]. */
    GUTTER_NODE_CLASS(AllIcons.Gutter.OverridenMethod),
    /** For [com.github.oowekyala.ijcc.ide.gutter.JjtreePartialDeclarationLineMarkerProvider]. */
    GUTTER_PARTIAL_DECL("jjtreeNodeLocate.svg"),
    GUTTER_NAVIGATE_TO_GRAMMAR("jccNavigateToGrammar.svg"),
    GUTTER_NAVIGATE_TO_PRODUCTION(GUTTER_NAVIGATE_TO_GRAMMAR),
    GUTTER_NAVIGATE_TO_JJTREE_NODE("jccNavigateToNode.svg"),
    GUTTER_RECURSION(AllIcons.Gutter.RecursiveMethod),
    /** File type icon.  */
    JAVACC_FILE("jccFile.svg"),
    JJTREE_FILE("jjtreeFile.svg"),
    ;

    constructor(fname: String) : this(IconLoader.getIcon(fname))
}

