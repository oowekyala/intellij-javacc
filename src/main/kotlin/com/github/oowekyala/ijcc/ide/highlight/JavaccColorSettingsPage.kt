package com.github.oowekyala.ijcc.ide.highlight

import com.github.oowekyala.ijcc.icons.JavaccIcons
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
class JavaccColorSettingsPage : ColorSettingsPage {

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
        "jmethodcall" to JavaHighlightingColors.METHOD_CALL_ATTRIBUTES,
        "jtype" to JavaHighlightingColors.CLASS_NAME_ATTRIBUTES,
        "token" to JavaccHighlightingColors.TOKEN_DECLARATION.keys,
        "priv-token" to JavaccHighlightingColors.PRIVATE_REGEX_DECLARATION.keys,
        "token-lit-ref" to JavaccHighlightingColors.TOKEN_LITERAL_REFERENCE.keys,
        "token-ref" to JavaccHighlightingColors.TOKEN_REFERENCE.keys,
        "jjtree" to JavaccHighlightingColors.JJTREE_DECORATION.keys,
        "jjtree-scope" to JavaccHighlightingColors.JJTREE_NODE_SCOPE.keys,
        "unknown" to CodeInsightColors.WRONG_REFERENCES_ATTRIBUTES,
        "knownprod" to JavaccHighlightingColors.NONTERMINAL_REFERENCE.keys,
        "pdecl" to JavaccHighlightingColors.NONTERMINAL_DECLARATION.keys,
        "lexstate" to JavaccHighlightingColors.LEXICAL_STATE.keys
    )


    override fun getDemoText(): String = """
<jcckeyword>options</jcckeyword> {
    LOOKAHEAD = 1;
    CHOICE_AMBIGUITY_CHECK = 2;
    OTHER_AMBIGUITY_CHECK = 1;
    STATIC = true;
    FORCE_LA_CHECK = false;
}

<jcckeyword>PARSER_BEGIN</jcckeyword>(JJTreeParser)

    package org.javacc.jjtree;

    <jdoccomment>/**
     *  This is my parser declaration
     */</jdoccomment>
    public class <jtype>JJTreeParser</jtype> {

      void <jmethod>jjtreeOpenNodeScope</jmethod>(<jtype>Node</jtype> n) {
        ((<jtype>JJTreeNode</jtype>)n).<jmethodcall>setFirstToken</jmethodcall>(<jmethodcall>getToken</jmethodcall>(1));
      }

    }

<jcckeyword>PARSER_END</jcckeyword>(JJTreeParser)

// Some token declarations
<<lexstate>DEFAULT</lexstate>>
<jcckeyword>TOKEN</jcckeyword> :
{
| < <token>PLUS</token>: "+" >
| < <token>MINUS</token>: "-" >
| < <token>NULL</token>: "null" >
| < <token>INTEGER</token>: ["+" | "-"] <<token-ref>DIGITS</token-ref>> >
| < <priv-token>#DIGITS</priv-token>: (["0"-"9"])+ >
}


void <pdecl>Expression</pdecl>(): {}
{
      <knownprod>BinaryExpression</knownprod>()
    | <jjtree-scope><<token-ref>NULL</token-ref>></jjtree-scope>   <jjtree>#NullLiteral</jjtree>
}

void <pdecl>BinaryExpression</pdecl>() <jjtree>#BinaryExpression</jjtree>(>1): {}
{
    <knownprod>UnaryExpression</knownprod>() [ ( <token-lit-ref>"+"</token-lit-ref> | <token-lit-ref>"-"</token-lit-ref> ) <knownprod>UnaryExpression</knownprod>() ]
}

void <pdecl>UnaryExpression</pdecl>() <jjtree>#<jjtree>void</jjtree></jjtree>: {}
{
  "(" <unknown>Expression</unknown>() ")" | <knownprod>Integer</knownprod>()
}

void <pdecl>Integer</pdecl>() <jjtree>#IntegerLiteral</jjtree>: {}
{
  <<token-ref>INTEGER</token-ref>>
}
"""


}
