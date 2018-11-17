package com.github.oowekyala.ijcc.lang.lexer

import com.intellij.lexer.FlexAdapter

class JavaccLexerAdapter : FlexAdapter(JavaccLexer(null))