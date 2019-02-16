package com.github.oowekyala.ijcc.ide.completion

import com.intellij.codeInsight.TailType
import com.intellij.codeInsight.completion.PrioritizedLookupElement
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.TailTypeDecorator


fun LookupElement.withTail(tail: String): LookupElement =
    TailTypeDecorator.withTail(this, MultiCharTailType(tail))

fun LookupElement.withTail(tailType: TailType): LookupElement =
    TailTypeDecorator.withTail(this, tailType)

fun LookupElement.withPriority(priority: Double): LookupElement =
    PrioritizedLookupElement.withPriority(this, priority)