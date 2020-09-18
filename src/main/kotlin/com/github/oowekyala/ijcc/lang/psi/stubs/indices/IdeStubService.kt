package com.github.oowekyala.ijcc.lang.psi.stubs.indices

import com.github.oowekyala.ijcc.lang.psi.stubs.JjtNodeClassOwnerStub
import com.github.oowekyala.ijcc.lang.psi.stubs.StubIndexService
import com.github.oowekyala.ijcc.util.runIt
import com.intellij.psi.stubs.IndexSink

/**
 * @author Clément Fournier
 */
object IdeStubService : StubIndexService() {

    override fun indexJjtreeNodeClassOwner(stub: JjtNodeClassOwnerStub<*>, sink: IndexSink) {
        stub.jjtNodeQualifiedName?.runIt {
            sink.occurrence(JjtreeQNameStubIndex.key, it)
        }
    }

}
