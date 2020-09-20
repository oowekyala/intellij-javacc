package com.github.oowekyala.ijcc.lang.psi.stubs

import com.intellij.openapi.components.ServiceManager
import com.intellij.psi.stubs.IndexSink

/**
 * Overridable stub service, implemented by the plugin.
 *
 * @author Clément Fournier
 */
open class StubIndexService protected constructor() {

    open fun indexJjtreeNodeClassOwner(stub: JjtNodeClassOwnerStub<*>, sink: IndexSink) {
    }

    companion object {
        @JvmStatic
        fun getInstance(): StubIndexService = ServiceManager.getService(StubIndexService::class.java) ?: NO_INDEX

        private val NO_INDEX = StubIndexService()
    }

}
