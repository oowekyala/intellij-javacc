/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright (C) 1998-2009  Gerwin Klein <lsf@jflex.de>                    *
 * All rights reserved.                                                    *
 *                                                                         *
 * License: BSD                                                            *
 *                                                                         *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

/* Java 1.2 language lexer specification */

/* Use together with unicode.flex for Unicode preprocesssing */
/* and java12.cup for a Java 1.2 parser                      */

/* Note that this lexer specification is not tuned for speed.
   It is in fact quite slow on integer and floating point literals, 
   because the input is read twice and the methods used to parse
   the numbers are not very fast. 
   For a production quality application (e.g. a Java compiler) 
   this could be optimized */

package com.github.oowekyala.ijcc.lang.lexer;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.JavaTokenType;
import com.intellij.psi.tree.IElementType;

%%


%public
%class JavaccLexer
%implements FlexLexer
%function advance
%type IElementType

%unicode

/* main character classes */
LineTerminator = \r|\n|\r\n
InputCharacter = [^\r\n]

WhiteSpace = {LineTerminator} | [ \t\f]

/* comments */
Comment = {TraditionalComment} | {EndOfLineComment} | 
          {DocumentationComment}

TraditionalComment = "/*" [^*] ~"*/" | "/*" "*"+ "/"
EndOfLineComment = "//" {InputCharacter}* {LineTerminator}?
DocumentationComment = "/*" "*"+ [^/*] ~"*/"

/* identifiers */
Identifier = [:jletter:][:jletterdigit:]*

/* integer literals */
DecIntegerLiteral = 0 | [1-9][0-9]*
DecLongLiteral    = {DecIntegerLiteral} [lL]

HexIntegerLiteral = 0 [xX] 0* {HexDigit} {1,8}
HexLongLiteral    = 0 [xX] 0* {HexDigit} {1,16} [lL]
HexDigit          = [0-9a-fA-F]

OctIntegerLiteral = 0+ [1-3]? {OctDigit} {1,15}
OctLongLiteral    = 0+ 1? {OctDigit} {1,21} [lL]
OctDigit          = [0-7]
    
/* floating point literals */        
FloatLiteral  = ({FLit1}|{FLit2}|{FLit3}) {Exponent}? [fF]
DoubleLiteral = ({FLit1}|{FLit2}|{FLit3}) {Exponent}?

FLit1    = [0-9]+ \. [0-9]* 
FLit2    = \. [0-9]+ 
FLit3    = [0-9]+ 
Exponent = [eE] [+-]? [0-9]+

/* string and character literals */
StringCharacter = [^\r\n\"\\]
SingleCharacter = [^\r\n\'\\]

%state STRING, CHARLITERAL

%%

<YYINITIAL> {

  {EndOfLineComment}             { return JavaTokenType.END_OF_LINE_COMMENT; }
  {TraditionalComment}           { return JavaTokenType.C_STYLE_COMMENT; }

  /* keywords */
  "abstract"                     { return JavaTokenType.ABSTRACT_KEYWORD; }
  "boolean"                      { return JavaTokenType.BOOLEAN_KEYWORD; }
  "break"                        { return JavaTokenType.BREAK_KEYWORD; }
  "byte"                         { return JavaTokenType.BYTE_KEYWORD; }
  "case"                         { return JavaTokenType.CASE_KEYWORD; }
  "catch"                        { return JavaTokenType.CATCH_KEYWORD; }
  "char"                         { return JavaTokenType.CHAR_KEYWORD; }
  "class"                        { return JavaTokenType.CLASS_KEYWORD; }
  "const"                        { return JavaTokenType.CONST_KEYWORD; }
  "continue"                     { return JavaTokenType.CONTINUE_KEYWORD; }
  "do"                           { return JavaTokenType.DO_KEYWORD; }
  "double"                       { return JavaTokenType.DOUBLE_KEYWORD; }
  "else"                         { return JavaTokenType.ELSE_KEYWORD; }
  "extends"                      { return JavaTokenType.EXTENDS_KEYWORD; }
  "final"                        { return JavaTokenType.FINAL_KEYWORD; }
  "finally"                      { return JavaTokenType.FINALLY_KEYWORD; }
  "float"                        { return JavaTokenType.FLOAT_KEYWORD; }
  "for"                          { return JavaTokenType.FOR_KEYWORD; }
  "default"                      { return JavaTokenType.DEFAULT_KEYWORD; }
  "implements"                   { return JavaTokenType.IMPLEMENTS_KEYWORD; }
  "import"                       { return JavaTokenType.IMPORT_KEYWORD; }
  "instanceof"                   { return JavaTokenType.INSTANCEOF_KEYWORD; }
  "int"                          { return JavaTokenType.INT_KEYWORD; }
  "interface"                    { return JavaTokenType.INTERFACE_KEYWORD; }
  "long"                         { return JavaTokenType.LONG_KEYWORD; }
  "native"                       { return JavaTokenType.NATIVE_KEYWORD; }
  "new"                          { return JavaTokenType.NEW_KEYWORD; }
  "goto"                         { return JavaTokenType.GOTO_KEYWORD; }
  "if"                           { return JavaTokenType.IF_KEYWORD; }
  "public"                       { return JavaTokenType.PUBLIC_KEYWORD; }
  "short"                        { return JavaTokenType.SHORT_KEYWORD; }
  "super"                        { return JavaTokenType.SUPER_KEYWORD; }
  "switch"                       { return JavaTokenType.SWITCH_KEYWORD; }
  "synchronized"                 { return JavaTokenType.SYNCHRONIZED_KEYWORD; }
  "package"                      { return JavaTokenType.PACKAGE_KEYWORD; }
  "private"                      { return JavaTokenType.PRIVATE_KEYWORD; }
  "protected"                    { return JavaTokenType.PROTECTED_KEYWORD; }
  "transient"                    { return JavaTokenType.TRANSIENT_KEYWORD; }
  "return"                       { return JavaTokenType.RETURN_KEYWORD; }
  "void"                         { return JavaTokenType.VOID_KEYWORD; }
  "static"                       { return JavaTokenType.STATIC_KEYWORD; }
  "while"                        { return JavaTokenType.WHILE_KEYWORD; }
  "this"                         { return JavaTokenType.THIS_KEYWORD; }
  "throw"                        { return JavaTokenType.THROW_KEYWORD; }
  "throws"                       { return JavaTokenType.THROWS_KEYWORD; }
  "try"                          { return JavaTokenType.TRY_KEYWORD; }
  "volatile"                     { return JavaTokenType.VOLATILE_KEYWORD; }
  "strictfp"                     { return JavaTokenType.STRICTFP_KEYWORD; }
  
  /* boolean literals */
  "true"                         { return JavaTokenType.TRUE_KEYWORD; }
  "false"                        { return JavaTokenType.FALSE_KEYWORD; }
  
  /* null literal */
  "null"                         { return JavaTokenType.NULL_KEYWORD; }
  
  
  /* separators */
  "("                            { return JavaTokenType.LPARENTH; }
  ")"                            { return JavaTokenType.RPARENTH; }
  "{"                            { return JavaTokenType.LBRACE; }
  "}"                            { return JavaTokenType.RBRACE; }
  "["                            { return JavaTokenType.LBRACKET; }
  "]"                            { return JavaTokenType.RBRACKET; }
  ";"                            { return JavaTokenType.SEMICOLON; }
  ","                            { return JavaTokenType.COMMA; }
  "."                            { return JavaTokenType.DOT; }

  /* operators */
  "="                            { return JavaTokenType.EQ; }
  ">"                            { return JavaTokenType.GT; }
  "<"                            { return JavaTokenType.LT; }
  "!"                            { return JavaTokenType.EXCL; }
  "~"                            { return JavaTokenType.TILDE; }
  "?"                            { return JavaTokenType.QUEST; }
  ":"                            { return JavaTokenType.COLON; }
  "=="                           { return JavaTokenType.EQEQ; }
  "<="                           { return JavaTokenType.LE; }
  ">="                           { return JavaTokenType.GE; }
  "!="                           { return JavaTokenType.NE; }
  "&&"                           { return JavaTokenType.ANDAND; }
  "||"                           { return JavaTokenType.OROR; }
  "++"                           { return JavaTokenType.PLUSPLUS; }
  "--"                           { return JavaTokenType.MINUSMINUS; }
  "+"                            { return JavaTokenType.PLUS; }
  "-"                            { return JavaTokenType.MINUS; }
  "*"                            { return JavaTokenType.ASTERISK; }
  "/"                            { return JavaTokenType.DIV; }
  "&"                            { return JavaTokenType.AND; }
  "|"                            { return JavaTokenType.OR; }
  "^"                            { return JavaTokenType.XOR; }
  "%"                            { return JavaTokenType.PERC; }
  "<<"                           { return JavaTokenType.LTLT; }
  ">>"                           { return JavaTokenType.GTGT; }
  ">>>"                          { return JavaTokenType.GTGTGT; }
  "+="                           { return JavaTokenType.PLUSEQ; }
  "-="                           { return JavaTokenType.MINUSEQ; }
  "*="                           { return JavaTokenType.ASTERISKEQ; }
  "/="                           { return JavaTokenType.DIVEQ; }
  "&="                           { return JavaTokenType.ANDEQ; }
  "|="                           { return JavaTokenType.OREQ; }
  "^="                           { return JavaTokenType.XOREQ; }
  "%="                           { return JavaTokenType.PERCEQ; }
  "<<="                          { return JavaTokenType.LTLTEQ; }
  ">>="                          { return JavaTokenType.GTGTEQ; }
  ">>>="                         { return JavaTokenType.GTGTGTEQ; }
  
  /* string literal */
  \"                             { yybegin(STRING); }

  /* character literal */
  \'                             { yybegin(CHARLITERAL); }

  /* numeric literals */

  /* This is matched together with the minus, because the number is too big to 
     be represented by a positive integer. */
  "-2147483648"                  { return JavaTokenType.INTEGER_LITERAL; }
  
  {DecIntegerLiteral}            { return JavaTokenType.INTEGER_LITERAL; }
  {DecLongLiteral}               { return JavaTokenType.INTEGER_LITERAL; }
  
  {HexIntegerLiteral}            { return JavaTokenType.INTEGER_LITERAL; }
  {HexLongLiteral}               { return JavaTokenType.INTEGER_LITERAL; }
 
  {OctIntegerLiteral}            { return JavaTokenType.INTEGER_LITERAL; }
  {OctLongLiteral}               { return JavaTokenType.INTEGER_LITERAL; }
  
  {FloatLiteral}                 { return JavaTokenType.FLOAT_LITERAL; }
  {DoubleLiteral}                { return JavaTokenType.FLOAT_LITERAL; }
  {DoubleLiteral}[dD]            { return JavaTokenType.FLOAT_LITERAL; }
  
  /* comments */
  {Comment}                      { /* ignore */ }

  /* whitespace */
  {WhiteSpace}                   { /* ignore */ }

  /* identifiers */ 
  {Identifier}                   { return JavaTokenType.IDENTIFIER; }
}

<STRING> {
  \"                             { yybegin(YYINITIAL); return JavaTokenType.STRING_LITERAL; }
  
  {StringCharacter}+             { /* ignore */ }
  
  /* escape sequences */
  "\\b"                          { /* ignore */ }
  "\\t"                          { /* ignore */ }
  "\\n"                          { /* ignore */ }
  "\\f"                          { /* ignore */ }
  "\\r"                          { /* ignore */ }
  "\\\""                         { /* ignore */ }
  "\\'"                          { /* ignore */ }
  "\\\\"                         { /* ignore */ }
  \\[0-3]?{OctDigit}?{OctDigit}  { /* ignore */ }

  
  /* error cases */
  \\.                            { return JavaTokenType.BAD_CHARACTER; }
  {LineTerminator}               { return JavaTokenType.BAD_CHARACTER; }
}

<CHARLITERAL> {
  {SingleCharacter}\'            { yybegin(YYINITIAL); return JavaTokenType.CHARACTER_LITERAL; }

  /* escape sequences */
  "\\b"\'                        { yybegin(YYINITIAL); return JavaTokenType.CHARACTER_LITERAL;}
  "\\t"\'                        { yybegin(YYINITIAL); return JavaTokenType.CHARACTER_LITERAL;}
  "\\n"\'                        { yybegin(YYINITIAL); return JavaTokenType.CHARACTER_LITERAL;}
  "\\f"\'                        { yybegin(YYINITIAL); return JavaTokenType.CHARACTER_LITERAL;}
  "\\r"\'                        { yybegin(YYINITIAL); return JavaTokenType.CHARACTER_LITERAL;}
  "\\\""\'                       { yybegin(YYINITIAL); return JavaTokenType.CHARACTER_LITERAL;}
  "\\'"\'                        { yybegin(YYINITIAL); return JavaTokenType.CHARACTER_LITERAL;}
  "\\\\"\'                       { yybegin(YYINITIAL); return JavaTokenType.CHARACTER_LITERAL;}
  \\[0-3]?{OctDigit}?{OctDigit}\' { yybegin(YYINITIAL);return JavaTokenType.CHARACTER_LITERAL;}
  
  /* error cases */
  \\.                            { return JavaTokenType.BAD_CHARACTER; }
  {LineTerminator}               { return JavaTokenType.BAD_CHARACTER; }
}

/* error fallback */
[^]                              { return JavaTokenType.BAD_CHARACTER; }