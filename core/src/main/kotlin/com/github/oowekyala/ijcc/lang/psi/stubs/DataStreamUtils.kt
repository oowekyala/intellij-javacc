package com.github.oowekyala.ijcc.lang.psi.stubs

import com.intellij.util.io.DataInputOutputUtil
import java.io.DataInputStream
import java.io.DataOutputStream

inline fun <reified T : Enum<T>>
    DataInputStream.readEnum(): T = T::class.java.enumConstants[readInt()]

fun <T : Enum<T>> DataOutputStream.writeEnum(t: T) = writeInt(t.ordinal)

fun <T> DataOutputStream.writeNullable(t: T?, writer: DataOutputStream.(T) -> Unit): Unit =
    DataInputOutputUtil.writeNullable(this, t) { writer(this, it) }

fun <T> DataInputStream.readNullable(reader: DataInputStream.() -> T): T? =
    DataInputOutputUtil.readNullable(this) { reader(this) }
