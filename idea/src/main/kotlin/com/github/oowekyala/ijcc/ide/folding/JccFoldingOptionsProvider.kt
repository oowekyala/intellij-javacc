package com.github.oowekyala.ijcc.ide.folding

import com.github.oowekyala.ijcc.settings.JccGlobalSettingsState
import com.github.oowekyala.ijcc.settings.globalPluginSettings
import com.intellij.application.options.editor.CodeFoldingOptionsProvider
import com.intellij.openapi.options.BeanConfigurable
import kotlin.reflect.KMutableProperty

/**
 * @author Clément Fournier
 */
class JccFoldingOptionsProvider :
    BeanConfigurable<JccGlobalSettingsState>(globalPluginSettings, "JavaCC"), CodeFoldingOptionsProvider {

    init {
        val settings = globalPluginSettings

        fun checkBox(title: String, prop: KMutableProperty<Boolean>) =
            checkBox(title, { prop.getter.call() }, { prop.setter.call(it) })

        checkBox("Java fragments in JavaCC code", settings::isFoldJavaFragments)
        checkBox("JavaCC local lookahead declarations", settings::isFoldLookaheads)
        checkBox("JavaCC token references that can be replaced by a string literal", settings::isFoldTokenRefs)
        checkBox("JavaCC options section", settings::isFoldOptions)
        checkBox("JavaCC parser declaration section", settings::isFoldParserDecl)
        checkBox("JavaCC token manager declaration section", settings::isFoldTokenMgrDecl)
        checkBox("JavaCC regular expression productions (token declarations)", settings::isFoldTokenProds)
        checkBox("Generated sections in .jj files (@bgen ... @egen)", settings::isFoldBgenSections)

    }

}
