package com.github.oowekyala.ijcc.lang.psi.stubs

import com.intellij.openapi.application.ApplicationManager
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
        fun getInstance(): StubIndexService =
            ApplicationManager.getApplication().getService(StubIndexService::class.java) ?: NO_INDEX

        private val NO_INDEX = StubIndexService()
    }

}
