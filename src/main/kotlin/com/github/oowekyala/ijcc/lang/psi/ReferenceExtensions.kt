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

val JccTokenReferenceUnit.typedReference: JccTerminalReference
    get() = reference as JccTerminalReference

val JccNonTerminalExpansionUnit.typedReference: JccNonTerminalReference
    get() = reference as JccNonTerminalReference

val JccNodeClassOwner.typedReference: JjtNodePolyReference?
    get() = reference as JjtNodePolyReference?