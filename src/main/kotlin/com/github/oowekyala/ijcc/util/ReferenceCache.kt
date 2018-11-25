package com.github.oowekyala.ijcc.util

import java.lang.ref.Reference
import java.lang.ref.SoftReference

/**
 * @author Clément Fournier
 * @since 1.0
 */
class ReferenceCache<T>(private val supplier: () -> T) {

    private var ref: Reference<T>? = null


    val value: T
        get() {
            val cached = ref?.get()
            return if (cached == null) {
                val result = supplier()
                ref = SoftReference(result)
                return result
            } else cached
        }

}