package com.github.oowekyala.ijcc.highlight

import com.github.oowekyala.ijcc.util.JavaccIcons
import com.intellij.ide.highlighter.JavaHighlightingColors
import com.intellij.openapi.editor.colors.CodeInsightColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.ColorSettingsPage
import javax.swing.Icon

/**
 * Color settings page (extension point).
 */
object JavaccColorSettingsPage : ColorSettingsPage {

    override fun getDisplayName(): String = "JavaCC"

    override fun getIcon(): Icon = JavaccIcons.JAVACC_FILE

    override fun getAttributeDescriptors(): Array<AttributesDescriptor> = attributes

    override fun getColorDescriptors(): Array<ColorDescriptor> = ColorDescriptor.EMPTY_ARRAY

    override fun getHighlighter(): SyntaxHighlighter = JavaccSyntaxHighlighter()

    override fun getAdditionalHighlightingTagToDescriptorMap(): Map<String, TextAttributesKey>? = additionalAttributes

    private val attributes: Array<AttributesDescriptor> =
            JavaccHighlightingColors.values().map { AttributesDescriptor(it.displayName, it.keys) }.toTypedArray()

    private val additionalAttributes: Map<String, TextAttributesKey> = mapOf(
        "jcckeyword" to JavaccHighlightingColors.JAVACC_KEYWORD.keys,
        "jdoccomment" to JavaccHighlightingColors.C_COMMENT.keys,
        "jmethod" to JavaHighlightingColors.METHOD_DECLARATION_ATTRIBUTES,
        "jtype" to JavaHighlightingColors.CLASS_NAME_ATTRIBUTES,
        "token" to JavaccHighlightingColors.TOKEN.keys,
        "jjtree" to JavaccHighlightingColors.JJTREE_DECORATION.keys,
        "unknown" to CodeInsightColors.WRONG_REFERENCES_ATTRIBUTES,
        "knownprod" to JavaccHighlightingColors.NONTERMINAL_REFERENCE.keys,
        "pdecl" to JavaccHighlightingColors.NONTERMINAL_DECLARATION.keys
    )


    override fun getDemoText(): String = """
<jcckeyword>options</jcckeyword> {
    LOOKAHEAD = 1;
    CHOICE_AMBIGUITY_CHECK = 2;
    OTHER_AMBIGUITY_CHECK = 1;
    STATIC = true;
    FORCE_LA_CHECK = false;
}

PARSER_BEGIN(JJTreeParser)

    package org.javacc.jjtree;

    <jdoccomment>/**
     *  This is my parser declaration
     */</jdoccomment>
    public class <jtype>JJTreeParser</jtype> {

      void <jmethod>jjtreeOpenNodeScope</jmethod>(<jtype>Node</jtype> n) {
        ((<jtype>JJTreeNode</jtype>)n).setFirstToken(getToken(1));
      }

    }

PARSER_END(JJTreeParser)

// Some token declarations
TOKEN :
{
| < <token>PLUS</token>: "+" >
| < <token>MINUS</token>: "-" >
| < <token>VOID</token>: "void" >
| < <token>INTEGER</token>: "1" | "2" >
}

void <pdecl>BinaryExpression</pdecl>(): {}
{
    <knownprod>UnaryExpression</knownprod>() ( <token>"+"</token> | <token>"-"</token> ) <knownprod>UnaryExpression</knownprod>()
}

void <pdecl>UnaryExpression</pdecl>() <jjtree>#void</jjtree>: {}
{
  "(" <unknown>Expression</unknown>() ")" | <knownprod>Integer</knownprod>()
}

void <pdecl>Integer</pdecl>() <jjtree>#IntegerLiteral</jjtree>: {}
{
  <<token>INTEGER</token>>
}
"""


}
