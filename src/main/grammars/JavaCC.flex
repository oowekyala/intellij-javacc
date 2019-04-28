/*
 * Copyright 2000-2013 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.oowekyala.ijcc.lang.lexer;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.JavaTokenType;
import com.intellij.psi.impl.source.tree.JavaDocElementType;

import static com.github.oowekyala.ijcc.lang.JccTypes.*;

%%

%unicode

%public
%class JavaccLexer
%implements FlexLexer
%function advance
%type IElementType


%{
    private boolean myAssertKeyword;
    private boolean myEnumKeyword;

    public JavaccLexer() {
      this(null);
      myAssertKeyword = false; // level.isAtLeast(LanguageLevel.JDK_1_4);
      myEnumKeyword = false; // level.isAtLeast(LanguageLevel.JDK_1_5);
    }

    public void goTo(int offset) {
      zzCurrentPos = zzMarkedPos = zzStartRead = offset;
      zzAtEOF = false;
    }
%}


WHITE_SPACE_CHAR    =   [\ \n\r\t\f]

IDENTIFIER          =   [:jletter:] [:jletterdigit:]*


C_STYLE_COMMENT     =   ("/*"[^"*"]{COMMENT_TAIL})|"/*"
DOC_COMMENT         =   "/*""*"+("/"|([^"/""*"]{COMMENT_TAIL}))?
COMMENT_TAIL        =   ([^"*"]*("*"+[^"*""/"])?)*("*"+"/")?
END_OF_LINE_COMMENT =   "//" [^\r\n]*

DIGIT                   =   [0-9]
DIGIT_OR_UNDERSCORE     =   [_0-9]
DIGITS                  =   {DIGIT} | {DIGIT} {DIGIT_OR_UNDERSCORE}*
HEX_DIGIT_OR_UNDERSCORE =   [_0-9A-Fa-f]

INTEGER_LITERAL     =   {DIGITS} | {HEX_INTEGER_LITERAL} | {BIN_INTEGER_LITERAL}
LONG_LITERAL        =   {INTEGER_LITERAL} [Ll]
HEX_INTEGER_LITERAL =   0 [Xx] {HEX_DIGIT_OR_UNDERSCORE}*
BIN_INTEGER_LITERAL =   0 [Bb] {DIGIT_OR_UNDERSCORE}*

FLOAT_LITERAL       =   ({DEC_FP_LITERAL} | {HEX_FP_LITERAL}) [Ff] | {DIGITS} [Ff]
DOUBLE_LITERAL      =   ({DEC_FP_LITERAL} | {HEX_FP_LITERAL}) [Dd]? | {DIGITS} [Dd]
DEC_FP_LITERAL      =   {DIGITS} {DEC_EXPONENT} | {DEC_SIGNIFICAND} {DEC_EXPONENT}?
DEC_SIGNIFICAND     =   "." {DIGITS} | {DIGITS} "." {DIGIT_OR_UNDERSCORE}*
DEC_EXPONENT        =   [Ee] [+-]? {DIGIT_OR_UNDERSCORE}*
HEX_FP_LITERAL      =   {HEX_SIGNIFICAND} {HEX_EXPONENT}
HEX_SIGNIFICAND     =   0 [Xx] ({HEX_DIGIT_OR_UNDERSCORE}+ "."? | {HEX_DIGIT_OR_UNDERSCORE}* "." {HEX_DIGIT_OR_UNDERSCORE}+)
HEX_EXPONENT        =   [Pp] [+-]? {DIGIT_OR_UNDERSCORE}*

ESCAPE_SEQUENCE     =   \\[^\r\n]
CHARACTER_LITERAL   =   "'" ([^\\\'\r\n] | {ESCAPE_SEQUENCE})* ("'"|\\)?
STRING_LITERAL      =   \" ([^\\\"\r\n] | {ESCAPE_SEQUENCE})* (\"|\\)?


%state IN_JAVACODE, IN_PARSER_HEADER_1, IN_PARSER_HEADER_2, IN_PARSER_HEADER_3

%%

<YYINITIAL> {

    {WHITE_SPACE_CHAR}+         { return TokenType.WHITE_SPACE; }

    {C_STYLE_COMMENT}           { return JavaTokenType.C_STYLE_COMMENT; }
    {END_OF_LINE_COMMENT}       { return JavaTokenType.END_OF_LINE_COMMENT; }
    {DOC_COMMENT}               { return JavaDocElementType.DOC_COMMENT; }

    {LONG_LITERAL}              { return JCC_LONG_LITERAL; }
    {INTEGER_LITERAL}           { return JCC_INTEGER_LITERAL; }
    {FLOAT_LITERAL}             { return JCC_FLOAT_LITERAL; }
    {DOUBLE_LITERAL}            { return JCC_DOUBLE_LITERAL; }
    {CHARACTER_LITERAL}         { return JCC_CHARACTER_LITERAL; }
    {STRING_LITERAL}            { return JCC_STRING_LITERAL; }


    /* JavaCC keywords */
    "LOOKAHEAD"                 { return JCC_LOOKAHEAD_KEYWORD; }
    "IGNORE_CASE"               { return JCC_IGNORE_CASE_OPTION; }
    "PARSER_BEGIN"              { return JCC_PARSER_BEGIN_KEYWORD; }
    "PARSER_END"                { return JCC_PARSER_END_KEYWORD; }
    "JAVACODE"                  { return JCC_JAVACODE_KEYWORD; }
    "TOKEN"                     { return JCC_TOKEN_KEYWORD; }
    "SPECIAL_TOKEN"             { return JCC_SPECIAL_TOKEN_KEYWORD; }
    "MORE"                      { return JCC_MORE_KEYWORD; }
    "SKIP"                      { return JCC_SKIP_KEYWORD; }
    "TOKEN_MGR_DECLS"           { return JCC_TOKEN_MGR_DECLS_KEYWORD; }
    "EOF"                       { return JCC_EOF_KEYWORD; }

    /* Java keywords used by JavaCC */
    "true"                      { return JCC_TRUE_KEYWORD; }
    "false"                     { return JCC_FALSE_KEYWORD; }

    "boolean"                   { return JCC_PRIMITIVE_TYPE; }
    "byte"                      { return JCC_PRIMITIVE_TYPE; }
    "char"                      { return JCC_PRIMITIVE_TYPE; }
    "double"                    { return JCC_PRIMITIVE_TYPE; }
    "float"                     { return JCC_PRIMITIVE_TYPE; }
    "int"                       { return JCC_PRIMITIVE_TYPE; }
    "long"                      { return JCC_PRIMITIVE_TYPE; }
    "short"                     { return JCC_PRIMITIVE_TYPE; }
    "void"                      { return JCC_VOID_KEYWORD; }

    "private"                   { return JCC_PRIVATE_KEYWORD; }
    "public"                    { return JCC_PUBLIC_KEYWORD; }
    "protected"                 { return JCC_PROTECTED_KEYWORD; }

    "throws"                    { return JCC_THROWS_KEYWORD; }
    "try"                       { return JCC_TRY_KEYWORD; }
    "catch"                     { return JCC_CATCH_KEYWORD; }
    "finally"                   { return JCC_FINALLY_KEYWORD; }

    "extends"                   { return JCC_EXTENDS_KEYWORD; }
    "super"                     { return JCC_SUPER_KEYWORD; }


    /* Java keywords not relevant to JavaCC */

    "null"                      { return JavaTokenType.NULL_KEYWORD; }

    "assert"                    { return JavaTokenType.ASSERT_KEYWORD; } // since 1.4
    "enum"                      { return JavaTokenType.ENUM_KEYWORD; }     // since 1.5


    "abstract"                  { return JavaTokenType.ABSTRACT_KEYWORD; }
    "break"                     { return JavaTokenType.BREAK_KEYWORD; }
    "case"                      { return JavaTokenType.CASE_KEYWORD; }
    "class"                     { return JavaTokenType.CLASS_KEYWORD; }
    "const"                     { return JavaTokenType.CONST_KEYWORD; }
    "continue"                  { return JavaTokenType.CONTINUE_KEYWORD; }
    "default"                   { return JavaTokenType.DEFAULT_KEYWORD; }
    "do"                        { return JavaTokenType.DO_KEYWORD; }
    "else"                      { return JavaTokenType.ELSE_KEYWORD; }

    "final"                     { return JavaTokenType.FINAL_KEYWORD; }
    "for"                       { return JavaTokenType.FOR_KEYWORD; }
    "goto"                      { return JavaTokenType.GOTO_KEYWORD; }
    "if"                        { return JavaTokenType.IF_KEYWORD; }
    "implements"                { return JavaTokenType.IMPLEMENTS_KEYWORD; }
    "import"                    { return JavaTokenType.IMPORT_KEYWORD; }
    "instanceof"                { return JavaTokenType.INSTANCEOF_KEYWORD; }
    "interface"                 { return JavaTokenType.INTERFACE_KEYWORD; }
    "native"                    { return JavaTokenType.NATIVE_KEYWORD; }
    "new"                       { return JavaTokenType.NEW_KEYWORD; }
    "package"                   { return JavaTokenType.PACKAGE_KEYWORD; }
    "switch"                    { return JavaTokenType.SWITCH_KEYWORD; }
    "synchronized"              { return JavaTokenType.SYNCHRONIZED_KEYWORD; }
    "this"                      { return JavaTokenType.THIS_KEYWORD; }
    "throw"                     { return JavaTokenType.THROW_KEYWORD; }
    "transient"                 { return JavaTokenType.TRANSIENT_KEYWORD; }
    "return"                    { return JavaTokenType.RETURN_KEYWORD; }
    "static"                    { return JavaTokenType.STATIC_KEYWORD; }
    "strictfp"                  { return JavaTokenType.STRICTFP_KEYWORD; }
    "while"                     { return JavaTokenType.WHILE_KEYWORD; }
    "volatile"                  { return JavaTokenType.VOLATILE_KEYWORD; }


    {IDENTIFIER}                { return JCC_IDENT; }


    /* Java tokens not relevant to JavaCC */

    "=="                        { return JavaTokenType.EQEQ; }
    "!="                        { return JavaTokenType.NE; }
    "||"                        { return JavaTokenType.OROR; }
    "++"                        { return JavaTokenType.PLUSPLUS; }
    "--"                        { return JavaTokenType.MINUSMINUS; }
    "&&"                        { return JavaTokenType.ANDAND; }

    "&"                         { return JavaTokenType.AND; }
    "<="                        { return JavaTokenType.LE; }
    ">="                        { return JavaTokenType.GE; }


    "+="                        { return JavaTokenType.PLUSEQ; }
    "-="                        { return JavaTokenType.MINUSEQ; }
    "*="                        { return JavaTokenType.ASTERISKEQ; }
    "/="                        { return JavaTokenType.DIVEQ; }
    "&="                        { return JavaTokenType.ANDEQ; }
    "|="                        { return JavaTokenType.OREQ; }
    "^="                        { return JavaTokenType.XOREQ; }
    "%="                        { return JavaTokenType.PERCEQ; }

    "!"                         { return JavaTokenType.EXCL; }
    "/"                         { return JavaTokenType.DIV; }
    "%"                         { return JavaTokenType.PERC; }
    "@"                         { return JavaTokenType.AT; }

    "::"                        { return JavaTokenType.DOUBLE_COLON; }
    "->"                        { return JavaTokenType.ARROW; }


    /* Tokens used by JavaCC */

    "#"                         { return JCC_POUND; }
    ":"                         { return JCC_COLON; }


    "<"                         { return JCC_LT; }
    ">"                         { return JCC_GT; }

    "("                         { return JCC_LPARENTH; }
    ")"                         { return JCC_RPARENTH; }
    "{"                         { return JCC_LBRACE; }
    "}"                         { return JCC_RBRACE; }
    "["                         { return JCC_LBRACKET; }
    "]"                         { return JCC_RBRACKET; }

    ";"                         { return JCC_SEMICOLON; }
    ","                         { return JCC_COMMA; }
    "..."                       { return JCC_ELLIPSIS; }
    "."                         { return JCC_DOT; }

    "="                         { return JCC_EQ; }

    "|"                         { return JCC_UNION; }
    "~"                         { return JCC_TILDE; }
    "*"                         { return JCC_ASTERISK; }
    "+"                         { return JCC_PLUS; }
    "-"                         { return JCC_MINUS; }
    "?"                         { return JCC_QUESTION; }
}


[^]                             { return JCC_BAD_CHARACTER; }
