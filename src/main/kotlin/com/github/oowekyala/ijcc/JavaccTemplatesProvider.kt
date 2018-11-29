package com.github.oowekyala.ijcc

import com.intellij.codeInsight.template.impl.DefaultLiveTemplatesProvider

/**
 * @author Clément Fournier
 * @since 1.0
 */
class JavaccTemplatesProvider : DefaultLiveTemplatesProvider {
    override fun getDefaultLiveTemplateFiles(): Array<String> =
            arrayOf(
                "/com/github/oowekyala/ijcc/liveTemplates/JavaCC"
            )

    override fun getHiddenLiveTemplateFiles(): Array<String>? = emptyArray()

}
