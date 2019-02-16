package com.github.oowekyala.ijcc.ide.completion

import com.intellij.codeInsight.TailType
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.codeInsight.lookup.TailTypeDecorator


fun LookupElementBuilder.withTail(tail: String): LookupElement =
    TailTypeDecorator.withTail(this, MultiCharTailType(tail))

fun LookupElementBuilder.withTail(tailType: TailType): LookupElement =
    TailTypeDecorator.withTail(this, tailType)