package com.github.oowekyala.ijcc.ide.completion

import com.intellij.codeInsight.TailType
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.codeInsight.lookup.TailTypeDecorator


fun LookupElementBuilder.withTail(tail: String) =
    TailTypeDecorator.withTail(this, MultiCharTailType(tail))

fun LookupElementBuilder.withTail(tailType: TailType) =
    TailTypeDecorator.withTail(this, tailType)