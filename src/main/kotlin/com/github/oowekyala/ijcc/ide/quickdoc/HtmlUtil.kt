package com.github.oowekyala.ijcc.ide.quickdoc

import com.intellij.codeInsight.documentation.DocumentationManagerProtocol
import com.intellij.lang.documentation.DocumentationMarkup
import com.intellij.openapi.util.text.StringUtil
import org.intellij.lang.annotations.Language

object HtmlUtil {

    val br = "<br/>"


    @Language("HTML")
    fun grayed(it: String) = "${DocumentationMarkup.GRAYED_START}$it${DocumentationMarkup.GRAYED_END}"

    @Language("HTML")
    fun emph(it: String) = "<i>$it</i>"

    @Language("HTML")
    fun bold(it: String) = "<b>$it</b>"

    // plain text
    fun angles(it: String) = "<$it>"

    @Language("HTML")
    fun htmlAngles(it: String) = "&lt;$it&gt;"

    @Language("HTML")
    fun dquot(it: String) = "&quot;$it&quot;"

    @Language("HTML")
    fun code(it: String) = "<code>$it</code>"

    @Language("HTML")
    fun pre(it: String) = "<pre>$it</pre>"

    @Language("HTML")
    fun link(target: String, text: String) = "<a href=\"$target\">$text</a>"

    fun escapeHtml(text: String) = StringUtil.escapeXml(text)

    /**
     * Kotlin wrapper around [DocumentationManager.createHyperlink]
     * @param isCodeLink Whether the [linkTextUnescaped] should be wrapped into `<code>` tags
     */
    @Language("HTML")
    fun psiLink(
        builder: StringBuilder = StringBuilder(),
        linkTarget: String?,
        linkTextUnescaped: String, // will be escaped
        isCodeLink: Boolean = true
    ) {
        createHyperlinkImpl(
            buffer = builder,
            label = escapeHtml(linkTextUnescaped),
            refText = linkTarget,
            plainLink = !isCodeLink
        )
    }

    // copy pasted from DocumentationManagerUtil because the component service cannot be
    // created during tests..
    private fun createHyperlinkImpl(buffer: StringBuilder,
                                    refText: String?,
                                    label: String,
                                    plainLink: Boolean) {
        buffer.append("<a href=\"")
        buffer.append(DocumentationManagerProtocol.PSI_ELEMENT_PROTOCOL) // :-)
        buffer.append(refText)
        buffer.append("\">")
        if (!plainLink) {
            buffer.append("<code>")
        }
        buffer.append(label)
        if (!plainLink) {
            buffer.append("</code>")
        }
        buffer.append("</a>")
    }

    @Language("HTML")
    fun psiLink(
        linkTarget: String?,
        linkTextUnescaped: String, // will be escaped
        isCodeLink: Boolean = true
    ): String =
        StringBuilder().also { psiLink(it, linkTarget, linkTextUnescaped, isCodeLink) }.toString()
}
