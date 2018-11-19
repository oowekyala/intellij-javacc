package com.github.oowekyala.ijcc.highlight

import com.github.oowekyala.ijcc.util.JavaccIcons
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

    override fun getAttributeDescriptors(): Array<AttributesDescriptor> =
        Attributes

    override fun getColorDescriptors(): Array<ColorDescriptor> = ColorDescriptor.EMPTY_ARRAY

    override fun getHighlighter(): SyntaxHighlighter = JavaccSyntaxHighlighter()

    override fun getDemoText(): String {
        return """
options {
  LOOKAHEAD = 1;
  CHOICE_AMBIGUITY_CHECK = 2;
  OTHER_AMBIGUITY_CHECK = 1;
  STATIC = true;
  FORCE_LA_CHECK = false;
}

PARSER_BEGIN(Simple1)

public class Simple1 {

  public static void main(String args[]) throws ParseException {
    Simple1 parser = new Simple1(System.in);
    parser.Input();
  }

}

PARSER_END(Simple1)

TOKEN :
{
  < OPTIONS: "options" >
| < LOOKAHEAD: "LOOKAHEAD" >
| < IDENTIFIER: <LETTER> (<PART_LETTER>)* >
| < #LETTER:
    [
      "A"-"Z",
      "_",
      "a"-"z"
    ]
  >
}

/* WHITE SPACE */

SKIP :
{
 " "
 | "\t"
 | "\n"
 | "\r"
 | "\f"
}


/* COMMENTS */

// This is another comment

MORE :
{
  "//" : IN_SINGLE_LINE_COMMENT
|
  <"/**" ~["/"]> { input_stream.backup(1); } : IN_FORMAL_COMMENT
|
  "/*" : IN_MULTI_LINE_COMMENT
}

<IN_SINGLE_LINE_COMMENT>
SPECIAL_TOKEN :
{
  <SINGLE_LINE_COMMENT: "\n" | "\r" | "\r\n" > : DEFAULT
}

<IN_FORMAL_COMMENT>
SPECIAL_TOKEN :
{
  <FORMAL_COMMENT: "*/" > : DEFAULT
}


void Input() :
{}
{
  MatchedBraces() ("\n"|"\r")* <EOF>
}

void MatchedBraces() :
{}
{
  "{" [ MatchedBraces() ] "}"
}"""
    }

    override fun getAdditionalHighlightingTagToDescriptorMap(): Map<String, TextAttributesKey>? =
        null

    private val Attributes: Array<AttributesDescriptor> =
        JavaccHighlightingColors.values().map { AttributesDescriptor(it.displayName, it.keys) }.toTypedArray()


}
