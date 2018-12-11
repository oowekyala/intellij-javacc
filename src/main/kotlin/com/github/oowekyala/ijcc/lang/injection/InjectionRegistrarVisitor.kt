package com.github.oowekyala.ijcc.lang.injection

import com.github.oowekyala.ijcc.util.EnclosedLogger
import com.github.oowekyala.ijcc.util.foreachAndBetween
import com.intellij.lang.injection.MultiHostRegistrar
import com.intellij.lang.java.JavaLanguage
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement

class InjectionRegistrarVisitor(private val registrar: MultiHostRegistrar) : InjectionStructureTree.Companion.PrefixVisitor() {

    private val prefixBuilder = StringBuilder()
    private val lastPrefixBuilder = StringBuilder()
    private var lastVisitedHost: InjectionStructureTree.HostLeaf? = null

    private object LOG : EnclosedLogger()

    fun startOn(root: InjectionStructureTree) {
        lastVisitedHost = root.getLastHostInPrefixOrder() ?: run {
            LOG { debug("Nothing to inject") }
            return@startOn
        }
        registrar.startInjecting(JavaLanguage.INSTANCE, "java")
        root.accept(this)

        // add the last host
        registrar.addPlace(
            lastPrefixBuilder.toString(),
            prefixBuilder.toString(),
            lastVisitedHost!!.host,
            lastVisitedHost!!.rangeInsideHost
        )

        registrar.doneInjecting()
    }



    override fun visit(hostLeaf: InjectionStructureTree.HostLeaf) {
        if (hostLeaf === lastVisitedHost) {
            // taken care of in the root
            lastPrefixBuilder.append(prefixBuilder)
            prefixBuilder.clear()
            return
        }

        val prefix = prefixBuilder.toString()
        prefixBuilder.clear()
        registrar.addPlace(prefix, null, hostLeaf.host, hostLeaf.rangeInsideHost)
    }

    override fun visit(surroundNode: InjectionStructureTree.SurroundNode) {
        prefixBuilder.append(surroundNode.prefix)
        surroundNode.child.accept(this)
        prefixBuilder.append(surroundNode.suffix)
    }

    override fun visit(emptyLeaf: InjectionStructureTree.EmptyLeaf) {
        // do nothing
    }

    override fun visit(multiChildNode: InjectionStructureTree.MultiChildNode) {
        multiChildNode.children.foreachAndBetween({ prefixBuilder.append(multiChildNode.delimiter()) }) {
            it.accept(this)
        }
    }
}