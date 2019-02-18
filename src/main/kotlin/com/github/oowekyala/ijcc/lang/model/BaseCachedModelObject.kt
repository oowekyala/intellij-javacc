package com.github.oowekyala.ijcc.lang.model

import com.github.oowekyala.ijcc.lang.psi.JccFile

/**
 * @author Clément Fournier
 * @since 1.2
 */
abstract class BaseCachedModelObject(val file: JccFile) {


    final override fun equals(other: Any?): Boolean =
        if (other?.javaClass != javaClass || other !is BaseCachedModelObject) false
        else file.virtualFile.path.hashCode() == other.file.virtualFile.path.hashCode()

    final override fun hashCode(): Int = file.virtualFile.path.hashCode()
}
