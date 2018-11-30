package com.github.oowekyala.ijcc.model

import kotlin.reflect.KClass

sealed class OptionType<T : Any> constructor(val klass: KClass<T>) {

    @Suppress("UNCHECKED_CAST")
    fun parseStringValue(str: String): T? = when (this) {
        STRING  -> str as T
        BOOLEAN -> str.toBoolean() as T
        INTEGER -> str.toIntOrNull() as T?
    }


    object STRING : OptionType<String>(String::class)
    object INTEGER : OptionType<Int>(Int::class)
    object BOOLEAN : OptionType<Boolean>(Boolean::class)

}