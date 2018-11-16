package com.github.oowekyala.gark87.idea.javacc

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

    override fun getIcon(): Icon? = null

    override fun getAttributeDescriptors(): Array<AttributesDescriptor> = ATTRS

    override fun getColorDescriptors(): Array<ColorDescriptor> = ColorDescriptor.EMPTY_ARRAY

    override fun getHighlighter(): SyntaxHighlighter = JavaCCHighlighter()

    override fun getDemoText(): String {
        return "options {\n" +
               "  LOOKAHEAD = 1;\n" +
               "  CHOICE_AMBIGUITY_CHECK = 2;\n" +
               "  OTHER_AMBIGUITY_CHECK = 1;\n" +
               "  STATIC = true;\n" +
               "  DEBUG_PARSER = false;\n" +
               "  DEBUG_LOOKAHEAD = false;\n" +
               "  DEBUG_TOKEN_MANAGER = false;\n" +
               "  ERROR_REPORTING = true;\n" +
               "  JAVA_UNICODE_ESCAPE = false;\n" +
               "  UNICODE_INPUT = false;\n" +
               "  IGNORE_CASE = false;\n" +
               "  USER_TOKEN_MANAGER = false;\n" +
               "  USER_CHAR_STREAM = false;\n" +
               "  BUILD_PARSER = true;\n" +
               "  BUILD_TOKEN_MANAGER = true;\n" +
               "  SANITY_CHECK = true;\n" +
               "  FORCE_LA_CHECK = false;\n" +
               "}\n" +
               "\n" +
               "PARSER_BEGIN(Simple1)\n" +
               "\n" +
               "public class Simple1 {\n" +
               "\n" +
               "  public static void main(String args[]) throws ParseException {\n" +
               "    Simple1 parser = new Simple1(System.in);\n" +
               "    parser.Input();\n" +
               "  }\n" +
               "\n" +
               "}\n" +
               "\n" +
               "PARSER_END(Simple1)\n" +
               "\n" +
               "void Input() :\n" +
               "{}\n" +
               "{\n" +
               "  MatchedBraces() (\"\\n\"|\"\\r\")* <EOF>\n" +
               "}\n" +
               "\n" +
               "void MatchedBraces() :\n" +
               "{}\n" +
               "{\n" +
               "  \"{\" [ MatchedBraces() ] \"}\"\n" +
               "}"
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
