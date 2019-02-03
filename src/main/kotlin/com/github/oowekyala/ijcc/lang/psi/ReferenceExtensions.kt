package com.github.oowekyala.ijcc.lang.psi

import com.github.oowekyala.ijcc.lang.refs.JccNonTerminalReference
import com.github.oowekyala.ijcc.lang.refs.JccStringTokenReference
import com.github.oowekyala.ijcc.lang.refs.JccTerminalReference
import com.github.oowekyala.ijcc.lang.refs.JjtNodePolyReference

/**
 * @author Clément Fournier
 * @since 1.0
 */


val JccLiteralRegexpUnit.typedReference: JccStringTokenReference?
    get() = reference as JccStringTokenReference?


val JccLiteralRegularExpression.typedReference: JccStringTokenReference?
    get() = unit.typedReference

val JccTokenReferenceUnit.typedReference: JccTerminalReference
    get() = reference as JccTerminalReference

val JccRegularExpressionReference.typedReference: JccTerminalReference?
    get() = unit.typedReference




val JccNonTerminalExpansionUnit.typedReference: JccNonTerminalReference
    get() = reference as JccNonTerminalReference

/**
 * Null if [JccNodeClassOwner.isVoid], not null otherwise.
 * This is not yielded by PsiElement.getReference because it breaks
 * find usages.
 */
val JccNodeClassOwner.typedReference: JjtNodePolyReference?
    get() = if (isNotVoid) JjtNodePolyReference(this) else null