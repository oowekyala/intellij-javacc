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

    {C_STYLE_COMMENT}           { return JCC_C_STYLE_COMMENT; }
    {END_OF_LINE_COMMENT}       { return JCC_END_OF_LINE_COMMENT; }
    {DOC_COMMENT}               { return JCC_C_STYLE_COMMENT; }

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

    "null"                      { return JCC_NULL_KEYWORD; }

    "assert"                    { return JCC_ASSERT_KEYWORD; } // since 1.4
    "enum"                      { return JCC_ENUM_KEYWORD; }     // since 1.5


    "abstract"                  { return JCC_ABSTRACT_KEYWORD; }
    "break"                     { return JCC_BREAK_KEYWORD; }
    "case"                      { return JCC_CASE_KEYWORD; }
    "class"                     { return JCC_CLASS_KEYWORD; }
    "const"                     { return JCC_CONST_KEYWORD; }
    "continue"                  { return JCC_CONTINUE_KEYWORD; }
    "default"                   { return JCC_DEFAULT_KEYWORD; }
    "do"                        { return JCC_DO_KEYWORD; }
    "else"                      { return JCC_ELSE_KEYWORD; }

    "final"                     { return JCC_FINAL_KEYWORD; }
    "for"                       { return JCC_FOR_KEYWORD; }
    "goto"                      { return JCC_GOTO_KEYWORD; }
    "if"                        { return JCC_IF_KEYWORD; }
    "implements"                { return JCC_IMPLEMENTS_KEYWORD; }
    "import"                    { return JCC_IMPORT_KEYWORD; }
    "instanceof"                { return JCC_INSTANCEOF_KEYWORD; }
    "interface"                 { return JCC_INTERFACE_KEYWORD; }
    "native"                    { return JCC_NATIVE_KEYWORD; }
    "new"                       { return JCC_NEW_KEYWORD; }
    "package"                   { return JCC_PACKAGE_KEYWORD; }
    "switch"                    { return JCC_SWITCH_KEYWORD; }
    "synchronized"              { return JCC_SYNCHRONIZED_KEYWORD; }
    "this"                      { return JCC_THIS_KEYWORD; }
    "throw"                     { return JCC_THROW_KEYWORD; }
    "transient"                 { return JCC_TRANSIENT_KEYWORD; }
    "return"                    { return JCC_RETURN_KEYWORD; }
    "static"                    { return JCC_STATIC_KEYWORD; }
    "strictfp"                  { return JCC_STRICTFP_KEYWORD; }
    "while"                     { return JCC_WHILE_KEYWORD; }
    "volatile"                  { return JCC_VOLATILE_KEYWORD; }


    {IDENTIFIER}                { return JCC_IDENT; }


    /* Java tokens not relevant to JavaCC */

    "=="                        { return JCC_EQEQ; }
    "!="                        { return JCC_NE; }
    "||"                        { return JCC_OROR; }
    "++"                        { return JCC_PLUSPLUS; }
    "--"                        { return JCC_MINUSMINUS; }
    "&&"                        { return JCC_ANDAND; }

    "&"                         { return JCC_AND; }
    "<="                        { return JCC_LE; }
    ">="                        { return JCC_GE; }


    "+="                        { return JCC_PLUSEQ; }
    "-="                        { return JCC_MINUSEQ; }
    "*="                        { return JCC_ASTERISKEQ; }
    "/="                        { return JCC_DIVEQ; }
    "&="                        { return JCC_ANDEQ; }
    "|="                        { return JCC_OREQ; }
    "^="                        { return JCC_XOREQ; }
    "%="                        { return JCC_PERCEQ; }

    "!"                         { return JCC_EXCL; }
    "/"                         { return JCC_DIV; }
    "%"                         { return JCC_PERC; }
    "@"                         { return JCC_AT; }

    "::"                        { return JCC_DOUBLE_COLON; }
    "->"                        { return JCC_ARROW; }


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
