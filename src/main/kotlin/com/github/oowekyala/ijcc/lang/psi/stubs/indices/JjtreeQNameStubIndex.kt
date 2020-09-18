package com.github.oowekyala.ijcc.lang.psi.stubs.indices

import com.github.oowekyala.ijcc.lang.psi.JjtNodeClassOwner
import com.intellij.psi.stubs.StringStubIndexExtension
import com.intellij.psi.stubs.StubIndexKey

/**
 * Indexes the node class owners of a grammar by qname, to provide
 * links from the node classes to the productions.
 *
 * @author Clément Fournier
 * @since 1.2
 */
object JjtreeQNameStubIndex : StringStubIndexExtension<JjtNodeClassOwner>() {


    private val Key = StubIndexKey.createIndexKey<String, JjtNodeClassOwner>("jjtree.qname.owner")

    override fun getKey(): StubIndexKey<String, JjtNodeClassOwner> = Key

    override fun getVersion(): Int = 1

}
