package com.github.oowekyala.ijcc.ide.folding

import com.github.oowekyala.ijcc.settings.JccGlobalSettingsState
import com.github.oowekyala.ijcc.settings.globalPluginSettings
import com.intellij.application.options.editor.CodeFoldingOptionsProvider
import com.intellij.openapi.options.BeanConfigurable
import com.intellij.openapi.util.Getter
import com.intellij.openapi.util.Setter
import kotlin.reflect.KMutableProperty

/**
 * @author Clément Fournier
 */
class JccFoldingOptionsProvider :
    BeanConfigurable<JccGlobalSettingsState>(globalPluginSettings), CodeFoldingOptionsProvider {

    init {

        checkBox("foldJavaFragments", "Java fragments in JavaCC code")
        checkBox("foldLookaheads", "JavaCC local lookahead declarations")
        checkBox("foldTokenRefs", "JavaCC token references that can be replaced by a string literal")
        checkBox("foldOptions", "JavaCC options section")
        checkBox("foldParserDecl", "JavaCC parser declaration section")
        checkBox("foldTokenMgrDecl", "JavaCC token manager declaration section")
        checkBox("foldTokenProds", "JavaCC regular expression productions (token declarations)")
        checkBox("foldBgenSections", "Generated sections in .jj files (@bgen ... @egen)")

    }

}
