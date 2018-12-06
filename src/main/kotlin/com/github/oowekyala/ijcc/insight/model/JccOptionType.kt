package com.github.oowekyala.ijcc.insight.model

sealed class JccOptionType<T : Any> {

    /** Base type corresponding to syntactically allowed option values. */
    sealed class BaseOptionType<T : Any> : JccOptionType<T>() {

        @Suppress("UNCHECKED_CAST")
        fun parseStringValue(str: String): T? = when (this) {
            STRING  -> str.removeSurrounding("\"") as T
            BOOLEAN -> str.toBoolean() as T
            INTEGER -> str.toIntOrNull() as T?
        }

        override val projection = this

        object STRING : BaseOptionType<String>()
        object INTEGER : BaseOptionType<Int>()
        object BOOLEAN : BaseOptionType<Boolean>()
    }

    override fun toString(): String = javaClass.name.toLowerCase()

    abstract val projection: BaseOptionType<T>

    /** Refinement over another type to allow for e.g. reference injection. */
    sealed class RefinedOptionType<T : Any>(override val projection: BaseOptionType<T>) : JccOptionType<T>() {

        override fun toString(): String = super.toString() + " name"

        /** Expects a type FQCN as a string. */
        object TYPE : RefinedOptionType<String>(BaseOptionType.STRING)

        /** Expects a package FQCN as a string. */
        object PACKAGE : RefinedOptionType<String>(BaseOptionType.STRING)

        /** Expects a directory as a string. */
        object DIRECTORY : RefinedOptionType<String>(BaseOptionType.STRING)

        /** Expects a file as a string. */
        object FILE : RefinedOptionType<String>(BaseOptionType.STRING)
    }


}