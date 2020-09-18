package com.github.oowekyala.ijcc.util

import java.lang.ref.Reference
import java.lang.ref.SoftReference
import kotlin.reflect.KProperty

/**
 * Delegated property type.
 *
 * @author Clément Fournier
 * @since 1.0
 */
class SoftReferenceCache<T>(private val supplier: () -> T) {

    private var ref: Reference<T>? = null

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        val cached = ref?.get()
        return if (cached == null) {
            val result = supplier()
            ref = SoftReference(result)
            return result
        } else cached
    }

}


fun <T> softCache(supplier: () -> T) = SoftReferenceCache(supplier)