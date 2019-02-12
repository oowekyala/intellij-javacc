package com.github.oowekyala.ijcc.icons

import com.intellij.icons.AllIcons
import com.intellij.openapi.util.IconLoader
import com.intellij.ui.LayeredIcon
import com.intellij.ui.SizedIcon
import com.intellij.util.IconUtil
import com.intellij.util.PlatformIcons
import javax.swing.Icon
import javax.swing.SwingConstants

/**
 * Provides icons for the plugin.
 */
enum class JavaccIcons(icon: Icon) : Icon by icon {
    // Structure view

    /** Terminal (for headers).  */
    TOKEN_HEADER("terminal.svg"),
    /** Terminal regex.  */
    TOKEN(TOKEN_HEADER),
    LEXICAL_STATE(TOKEN),
    /** BNF production.  */
    BNF_PRODUCTION(AllIcons.Nodes.Method),
    /** Javacc option. */
    JAVACC_OPTION(PlatformIcons.ANNOTATION_TYPE_ICON),
    /** Javacode production.  */
    JAVACODE_PRODUCTION(AllIcons.Nodes.AbstractMethod),
    TOKEN_MGR_DECLS(PlatformIcons.CLASS_ICON),
    PARSER_DECLARATION(PlatformIcons.CLASS_ICON),

    JJTREE_NODE("trait_scala.svg"),

    // other
    /** For [com.github.oowekyala.ijcc.ide.gutter.JjtreeNodeClassLineMarkerProvider]. */
    GUTTER_NODE_CLASS(AllIcons.Gutter.OverridenMethod),
    /** For [com.github.oowekyala.ijcc.ide.gutter.JjtreePartialDeclarationLineMarkerProvider]. */
    GUTTER_PARTIAL_DECL(AllIcons.General.Locate),

    GUTTER_RECURSION(AllIcons.Gutter.RecursiveMethod),
    /** File type icon.  */
    JAVACC_FILE("jccFile.svg"),
    JJTREE_FILE(IjccIconUtil.makeJjtreeFileIcon()),
    ;

    constructor(fname: String) : this(IjccIconUtil.getIjccIcon(fname))
}

private object IjccIconUtil {
    fun getIjccIcon(fname: String): Icon = IconLoader.getIcon(fname)

    fun makeJjtreeFileIcon():Icon {

//        base.setIcon(JavaccIcons.JAVACC_FILE, 0)
        val fore = SizedIcon(JavaccIcons.JJTREE_NODE, 12, 16)
//        base.setIcon(JavaccIcons.JJTREE_NODE, 1, SwingConstants.SOUTH_EAST)

        return SizedIcon(LayeredIcon(JavaccIcons.JAVACC_FILE, fore), 16,16)

    }
}