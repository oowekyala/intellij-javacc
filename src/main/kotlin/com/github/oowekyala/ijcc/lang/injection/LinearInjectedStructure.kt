package com.github.oowekyala.ijcc.lang.injection

import com.github.oowekyala.ijcc.lang.psi.JccGrammarFileRoot
import com.github.oowekyala.ijcc.util.EnclosedLogger
import com.intellij.lang.injection.MultiHostRegistrar
import com.intellij.lang.java.JavaLanguage
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiLanguageInjectionHost
import com.intellij.psi.SmartPointerManager
import com.intellij.psi.SmartPsiElementPointer
import java.util.*

/**
 * Injection tree linearised by a [TreeLineariserVisitor].
 * Cached on the [JccGrammarFileRoot]. Mods in the javacc
 * file trigger a reparse so the cache is naturally invalidated.
 * Mods in injected fragments just update the mapping between
 * [HostSpec]s and their actual host. The structure is not rebuilt
 * but the whole structure is reinjected...
 *
 * @author Clément Fournier
 * @since 1.0
 */
class LinearInjectedStructure(val hostSpecs: List<HostSpec>) {
    private object LOG : EnclosedLogger()

    fun register(registrar: MultiHostRegistrar) {
        if (hostSpecs.isEmpty()) {
            LOG { debug("Nothing to inject") }
            return
        }

        registrar.startInjecting(JavaLanguage.INSTANCE, "java")

        for (spec in hostSpecs) {
            registrar.addPlace(spec.prefix, spec.suffix, spec.host, spec.rangeInsideHost)
        }

        registrar.doneInjecting()
    }

}

/**
 * One host with its prefix and suffix, resilient to local replacements
 * (ie mods in the injected file).
 */
class HostSpec(val prefix: String?, val suffix: String?,
               host: PsiLanguageInjectionHost,
               private val rangeGetter: (PsiLanguageInjectionHost) -> TextRange) {

    init {
        remapHost(host)
    }

    val rangeInsideHost: TextRange get() = rangeGetter(host)

    val host: PsiLanguageInjectionHost
        get() {
            val curHost = HostIndex[this]!!.element!!

            var replacedHost = curHost
            var replacementHost = ReplaceMap[curHost]
            while (replacementHost != null) {
                // follow replacement indirections and remove references
                ReplaceMap.remove(replacedHost)
                replacedHost = replacementHost
                replacementHost = ReplaceMap[replacedHost]
            }

            return if (replacedHost !== curHost) {
                remapHost(replacedHost)
                replacedHost
            } else {
                curHost
            }
        }


    fun appendSuffixAndDestroy(additionalSuffix: CharSequence): HostSpec =
            HostSpec(
                prefix = prefix,
                suffix = (suffix ?: "") + additionalSuffix,
                host = host,
                rangeGetter = rangeGetter
            ).also {
                // this makes this spec unusable
                HostIndex.remove(this)
            }

    private fun remapHost(newHost: PsiLanguageInjectionHost) {
        HostIndex[this] = SmartPointerManager.createPointer(newHost)
    }

    companion object {
        /** Global index of leaves to actual injection hosts. */
        private val HostIndex: MutableMap<HostSpec, SmartPsiElementPointer<PsiLanguageInjectionHost>> =
                HashMap()

        private val ReplaceMap: MutableMap<PsiLanguageInjectionHost, PsiLanguageInjectionHost> =
                HashMap()

        fun replaceHost(replaced: PsiLanguageInjectionHost, replacement: PsiLanguageInjectionHost) {
            ReplaceMap[replaced] = replacement
        }
    }
}
