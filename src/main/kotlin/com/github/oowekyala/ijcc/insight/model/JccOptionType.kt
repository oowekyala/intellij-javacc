package com.github.oowekyala.ijcc.insight.model

import kotlin.reflect.KClass

sealed class JccOptionType<T : Any> constructor(val klass: KClass<T>) {

    @Suppress("UNCHECKED_CAST")
    fun parseStringValue(str: String): T? = when (this) {
        STRING  -> str as T
        BOOLEAN -> str.toBoolean() as T
        INTEGER -> str.toIntOrNull() as T?
    }


    object STRING : JccOptionType<String>(String::class)
    object INTEGER : JccOptionType<Int>(Int::class)
    object BOOLEAN : JccOptionType<Boolean>(Boolean::class)

}