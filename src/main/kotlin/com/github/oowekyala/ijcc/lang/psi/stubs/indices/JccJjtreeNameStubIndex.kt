package com.github.oowekyala.ijcc.lang.psi.stubs.indices

import com.github.oowekyala.ijcc.lang.psi.JccNonTerminalProduction
import com.intellij.psi.stubs.StringStubIndexExtension
import com.intellij.psi.stubs.StubIndexKey

/**
 * @author Clément Fournier
 * @since 1.2
 */
object JccJjtreeNameStubIndex : StringStubIndexExtension<JccNonTerminalProduction>() {


    private val Key = StubIndexKey.createIndexKey<String, JccNonTerminalProduction>("jcc.prods.by.jjtree.qname")

    override fun getKey(): StubIndexKey<String, JccNonTerminalProduction> = Key

    override fun getVersion(): Int = 1

}