package com.github.oowekyala.jjtx.reporting

class ExceptionMerger {

    private val merged = mutableMapOf<ExMergeKey, MutableList<JjtricksExceptionWrapper>>()


    /**
     * Return true if no previous exceptions were merged with this one.
     */
    fun add(throwable: Throwable, messageKey: String?): Boolean {

        val k = ExMergeKey(throwable.javaClass, messageKey)

        var isMissing = true

        merged.computeIfAbsent(k) {
            isMissing = !isMissing
            mutableListOf()
        } += JjtricksExceptionWrapper.wrapIdem(throwable)

        return isMissing
    }


    fun getCollected(): Set<MergedException> =
        merged.mapTo(mutableSetOf()) { (k, vs) ->
            MergedException(k.message, vs)
        }


    private data class ExMergeKey(
        val klass: Class<*>,
        val message: String?
    )
}

data class MergedException(
    val message: String?,
    val instances: List<JjtricksExceptionWrapper>
)
