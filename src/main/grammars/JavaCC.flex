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
import com.intellij.psi.tree.IElementType;
import static com.github.oowekyala.ijcc.lang.JavaccTypes.*;
import com.intellij.psi.JavaTokenType;

%%


%public
%class JavaccLexer
%implements FlexLexer
%function advance
%type IElementType

%unicode

WHITE_SPACE_CHAR    =   [\ \n\r\t\f]

IDENTIFIER          =   [:jletter:] [:jletterdigit:]*

C_STYLE_COMMENT     =   ("/*"[^"*"]{COMMENT_TAIL})|"/*"
DOC_COMMENT         =   "/*""*"+("/"|([^"/""*"]{COMMENT_TAIL}))?
COMMENT_TAIL        =   ([^"*"]*("*"+[^"*""/"])?)*("*"+"/")?
END_OF_LINE_COMMENT =   "/""/"[^\r\n]*

DIGIT               =   [0-9]
DIGIT_OR_UNDERSCORE =   [_0-9]
DIGITS              =   {DIGIT} | {DIGIT} {DIGIT_OR_UNDERSCORE}*
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

%%

<YYINITIAL> {

  {WHITE_SPACE_CHAR}+         { return TokenType.WHITE_SPACE; }

  {C_STYLE_COMMENT}           { return JavaTokenType.C_STYLE_COMMENT; }
  {END_OF_LINE_COMMENT}       { return JavaTokenType.END_OF_LINE_COMMENT; }

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

   /* Java keywords */
  "true"                      { return JCC_TRUE_KEYWORD; }
  "false"                     { return JCC_FALSE_KEYWORD; }
  "null"                      { return JCC_NULL_KEYWORD; }

  "boolean"                   { return JavaTokenType.BOOLEAN_KEYWORD; }
  "break"                     { return JavaTokenType.BREAK_KEYWORD; }
  "byte"                      { return JavaTokenType.BYTE_KEYWORD; }
  "case"                      { return JavaTokenType.CASE_KEYWORD; }
  "catch"                     { return JavaTokenType.CATCH_KEYWORD; }
  "char"                      { return JavaTokenType.CHAR_KEYWORD; }
  "class"                     { return JavaTokenType.CLASS_KEYWORD; }
  "const"                     { return JavaTokenType.CONST_KEYWORD; }
  "continue"                  { return JavaTokenType.CONTINUE_KEYWORD; }
  "default"                   { return JavaTokenType.DEFAULT_KEYWORD; }
  "do"                        { return JavaTokenType.DO_KEYWORD; }
  "double"                    { return JavaTokenType.DOUBLE_KEYWORD; }
  "else"                      { return JavaTokenType.ELSE_KEYWORD; }
  "enum"                      { return myEnumKeyword ? JavaTokenType.ENUM_KEYWORD : JavaTokenType.IDENTIFIER; }
  "extends"                   { return JavaTokenType.EXTENDS_KEYWORD; }
  "final"                     { return JavaTokenType.FINAL_KEYWORD; }
  "finally"                   { return JavaTokenType.FINALLY_KEYWORD; }
  "float"                     { return JavaTokenType.FLOAT_KEYWORD; }
  "for"                       { return JavaTokenType.FOR_KEYWORD; }
  "goto"                      { return JavaTokenType.GOTO_KEYWORD; }
  "if"                        { return JavaTokenType.IF_KEYWORD; }
  "implements"                { return JavaTokenType.IMPLEMENTS_KEYWORD; }
  "import"                    { return JavaTokenType.IMPORT_KEYWORD; }
  "instanceof"                { return JavaTokenType.INSTANCEOF_KEYWORD; }
  "int"                       { return JavaTokenType.INT_KEYWORD; }
  "interface"                 { return JavaTokenType.INTERFACE_KEYWORD; }
  "long"                      { return JavaTokenType.LONG_KEYWORD; }
  "native"                    { return JavaTokenType.NATIVE_KEYWORD; }
  "new"                       { return JavaTokenType.NEW_KEYWORD; }
  "package"                   { return JavaTokenType.PACKAGE_KEYWORD; }
  "private"                   { return JavaTokenType.PRIVATE_KEYWORD; }
  "public"                    { return JavaTokenType.PUBLIC_KEYWORD; }
  "short"                     { return JavaTokenType.SHORT_KEYWORD; }
  "super"                     { return JavaTokenType.SUPER_KEYWORD; }
  "switch"                    { return JavaTokenType.SWITCH_KEYWORD; }
  "synchronized"              { return JavaTokenType.SYNCHRONIZED_KEYWORD; }
  "this"                      { return JavaTokenType.THIS_KEYWORD; }
  "throw"                     { return JavaTokenType.THROW_KEYWORD; }
  "protected"                 { return JavaTokenType.PROTECTED_KEYWORD; }
  "transient"                 { return JavaTokenType.TRANSIENT_KEYWORD; }
  "return"                    { return JavaTokenType.RETURN_KEYWORD; }
  "void"                      { return JavaTokenType.VOID_KEYWORD; }
  "static"                    { return JavaTokenType.STATIC_KEYWORD; }
  "strictfp"                  { return JavaTokenType.STRICTFP_KEYWORD; }
  "while"                     { return JavaTokenType.WHILE_KEYWORD; }
  "try"                       { return JavaTokenType.TRY_KEYWORD; }
  "volatile"                  { return JavaTokenType.VOLATILE_KEYWORD; }
  "throws"                    { return JavaTokenType.THROWS_KEYWORD; }

  {IDENTIFIER}                { return JavaTokenType.IDENTIFIER; }

  "=="                        { return JavaTokenType.EQEQ; }
  "!="                        { return JavaTokenType.NE; }
  "||"                        { return JavaTokenType.OROR; }
  "++"                        { return JavaTokenType.PLUSPLUS; }
  "--"                        { return JavaTokenType.MINUSMINUS; }

  "<"                         { return JavaTokenType.LT; }
  "<="                        { return JavaTokenType.LE; }
  "<<="                       { return JavaTokenType.LTLTEQ; }
  "<<"                        { return JavaTokenType.LTLT; }
  ">"                         { return JavaTokenType.GT; }
  "&"                         { return JavaTokenType.AND; }
  "&&"                        { return JavaTokenType.ANDAND; }

  "+="                        { return JavaTokenType.PLUSEQ; }
  "-="                        { return JavaTokenType.MINUSEQ; }
  "*="                        { return JavaTokenType.ASTERISKEQ; }
  "/="                        { return JavaTokenType.DIVEQ; }
  "&="                        { return JavaTokenType.ANDEQ; }
  "|="                        { return JavaTokenType.OREQ; }
  "^="                        { return JavaTokenType.XOREQ; }
  "%="                        { return JavaTokenType.PERCEQ; }

  "("                         { return JavaTokenType.LPARENTH; }
  ")"                         { return JavaTokenType.RPARENTH; }
  "{"                         { return JavaTokenType.LBRACE; }
  "}"                         { return JavaTokenType.RBRACE; }
  "["                         { return JavaTokenType.LBRACKET; }
  "]"                         { return JavaTokenType.RBRACKET; }
  ";"                         { return JavaTokenType.SEMICOLON; }
  ","                         { return JavaTokenType.COMMA; }
  "..."                       { return JavaTokenType.ELLIPSIS; }
  "."                         { return JavaTokenType.DOT; }

  "="                         { return JavaTokenType.EQ; }
  "!"                         { return JavaTokenType.EXCL; }
  "~"                         { return JavaTokenType.TILDE; }
  "?"                         { return JavaTokenType.QUEST; }
  ":"                         { return JavaTokenType.COLON; }
  "+"                         { return JavaTokenType.PLUS; }
  "-"                         { return JavaTokenType.MINUS; }
  "*"                         { return JavaTokenType.ASTERISK; }
  "/"                         { return JavaTokenType.DIV; }
  "|"                         { return JavaTokenType.OR; }
  "^"                         { return JavaTokenType.XOR; }
  "%"                         { return JavaTokenType.PERC; }
  "@"                         { return JavaTokenType.AT; }

  "::"                        { return JavaTokenType.DOUBLE_COLON; }
  "->"                        { return JavaTokenType.ARROW; }
}

[^]                           { return JavaTokenType.BAD_CHARACTER; }
