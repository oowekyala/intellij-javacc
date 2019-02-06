package com.github.oowekyala.ijcc.ide.inspections

import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.psi.PsiElement
import com.intellij.psi.SmartPointerManager
import com.intellij.psi.SmartPsiElementPointer
import java.util.*

/**
 * Remembers some data from the time the problem was detected.
 *
 * @author Clément Fournier
 * @since 1.0
 */
class MindfulProblemDescriptor(val descriptor: ProblemDescriptor) {

    fun <T : PsiElement> putData(dataKey: ProblemDataKey<T>, value: T) {
        putData(descriptor, manglePointer(dataKey), SmartPointerManager.createPointer(value))
    }

    fun <T : PsiElement> getData(dataKey: ProblemDataKey<T>): T? = getData(descriptor, manglePointer(dataKey)).element


    fun <T> putData(dataKey: ProblemDataKey<T>, value: T) {
        putData(descriptor, dataKey, value)
    }

    fun <T> getData(dataKey: ProblemDataKey<T>): T = getData(descriptor, dataKey)

    companion object {

        private fun <T : PsiElement> manglePointer(dataKey: ProblemDataKey<T>): ProblemDataKey<SmartPsiElementPointer<T>> =
                ProblemDataKey(dataKey.name + "~pointer")

        private val myData: MutableMap<ProblemDescriptor, MutableMap<String, Any?>> = WeakHashMap()

        @PublishedApi
        internal fun <T> putData(descriptor: ProblemDescriptor, dataKey: ProblemDataKey<T>, value: T) {
            myData.computeIfAbsent(descriptor) { mutableMapOf() }[dataKey.name] = value
        }

        @Suppress("UNCHECKED_CAST")
        @PublishedApi
        internal fun <T> getData(descriptor: ProblemDescriptor, dataKey: ProblemDataKey<T>): T =
                myData[descriptor]?.get(dataKey.name) as T
                    ?: throw IllegalArgumentException("Unregistered data key $dataKey")

    }
}

// could be an inline class
data class ProblemDataKey<T>(val name: String)