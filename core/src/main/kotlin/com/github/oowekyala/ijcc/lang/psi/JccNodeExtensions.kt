package com.github.oowekyala.ijcc.lang.psi

import com.intellij.util.io.java.AccessModifier

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