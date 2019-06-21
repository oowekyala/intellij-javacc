package com.github.oowekyala.jjtx.postprocessor

import com.github.oowekyala.jjtx.templates.vbeans.ClassVBean
import spoon.processing.AbstractProcessor
import spoon.reflect.factory.TypeFactory
import spoon.reflect.reference.CtTypeReference

/**
 * Renames references to a type.
 *
 * @param targetQname The target of the renaming
 */
class TypeReferenceRenamer(
    qnameMap: Map<ClassVBean, ClassVBean>
) : AbstractProcessor<CtTypeReference<*>>() {


    private val matchList: List<Pair<ClassVBean, CtTypeReference<Any>>> =
        qnameMap
            .filterNot { (a, b) -> a == b }
            .map { (from, to) ->
                Pair(
                    from,
                    TypeFactory().createReference<Any>(to.qualifiedName)!!
                )
            }

    override fun process(element: CtTypeReference<*>) {

        for ((qn, ref) in matchList) {
            if (element.isReferenceTo(qn.`package`, qn.simpleName, qn.qualifiedName)) {
                element.replace(ref.clone())
                break
            }
        }
    }


    /**
     * Optimise for most frequent cases.
     */
    private fun CtTypeReference<*>.isReferenceTo(packName: String, simpleName: String, qname: String): Boolean {
        if (this.simpleName != simpleName) return false
        val declaringType = this.declaringType
        val pack = this.`package`
        if (declaringType == null && pack != null) {
            return pack.qualifiedName == packName
        }
        // this.qualifiedName is super expensive
        return this.qualifiedName == qname
    }


}
