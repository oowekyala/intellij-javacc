package com.github.oowekyala.ijcc.lang.psi

import com.github.oowekyala.ijcc.lang.model.AccessModifier

/*
    Miscellaneous semantic extensions for jcc nodes.
 */



val JccJavaAccessModifier.modelConstant: AccessModifier
    get() = when (text) {
        ""          -> AccessModifier.PACKAGE_LOCAL
        "public"    -> AccessModifier.PUBLIC
        "protected" -> AccessModifier.PROTECTED
        "private"   -> AccessModifier.PRIVATE
        else        -> throw IllegalStateException()
    }
