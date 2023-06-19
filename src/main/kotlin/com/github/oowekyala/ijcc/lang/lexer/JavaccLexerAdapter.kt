package com.github.oowekyala.ijcc.lang.lexer

import com.intellij.lexer.FlexAdapter
class JavaccLexerAdapter @JvmOverloads constructor(isCCC: Boolean = false) : FlexAdapter(JavaccLexer(isCCC))
