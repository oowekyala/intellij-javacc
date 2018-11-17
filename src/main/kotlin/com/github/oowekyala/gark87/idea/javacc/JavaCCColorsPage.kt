package com.github.oowekyala.gark87.idea.javacc

import com.github.oowekyala.gark87.idea.javacc.util.JavaCCIcons
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.ColorSettingsPage
import javax.swing.Icon

/**
 * @author gark87
 */
class JavaCCColorsPage : ColorSettingsPage {

    override fun getDisplayName(): String = "JavaCC"

    override fun getIcon(): Icon = JavaCCIcons.JAVACC_FILE.icon

    override fun getAttributeDescriptors(): Array<AttributesDescriptor> = ATTRS

    override fun getColorDescriptors(): Array<ColorDescriptor> = ColorDescriptor.EMPTY_ARRAY

    override fun getHighlighter(): SyntaxHighlighter = JavaCCHighlighter()

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

    companion object {
        private val ATTRS: Array<AttributesDescriptor>

        init {

            val lst = mutableListOf<AttributesDescriptor>()
            JavaCCHighlighter.DISPLAY_NAMES.forEach {
                lst.add(AttributesDescriptor(it.value.first, it.key))
            }

            ATTRS = lst.toTypedArray()
        }
    }
}
