package com.github.oowekyala.ijcc.lang.injection

import com.github.oowekyala.ijcc.lang.psi.JccGrammarFileRoot
import com.github.oowekyala.ijcc.util.EnclosedLogger
import com.github.oowekyala.ijcc.util.plusAssign
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
class LinearInjectedStructure(private var hostSpecs: List<HostSpec>) {
    private object LOG : EnclosedLogger()

    fun register(registrar: MultiHostRegistrar) {
        hostSpecs = removeStaleSpecs(hostSpecs)

        if (hostSpecs.isEmpty()) {
            LOG { debug("Nothing to inject") }
            return
        }

        registrar.startInjecting(JavaLanguage.INSTANCE, "java")

        for (spec in hostSpecs) {
            val host = spec.host!!
            registrar.addPlace(spec.prefix, spec.suffix, host, spec.getRangeInsideHost(host))
        }

        registrar.doneInjecting()
    }


    private companion object {
        fun removeStaleSpecs(specs: List<HostSpec>): List<HostSpec> {

            if (specs.isEmpty()) return emptyList()

            var lastValidSpec: HostSpec? = null
            val nextPrefixBuilder = StringBuilder()

            // only yields valid specs, the last suffix needs to be adjusted though
            val preFilterSeq = sequence {
                val specSeq = specs.asSequence()

                for (spec in specSeq) {
                    val host = spec.host

                    if (host != null) {
                        val myRes = if (nextPrefixBuilder.isNotEmpty()) {
                            // merge previous empty specs in the prefix
                            val myPrefix = nextPrefixBuilder.append(spec.prefix).toString()
                            nextPrefixBuilder.clear()
                            HostSpec(myPrefix, spec.suffix, host, spec.rangeGetter)
                        } else spec

                        lastValidSpec = myRes

                        yield(myRes)
                    } else {
                        // host == null
                        nextPrefixBuilder += spec.prefix.orEmpty()
                        nextPrefixBuilder += spec.suffix.orEmpty()
                        // continue until the next valid spec
                    }
                }
            }

            if (lastValidSpec == null) return emptyList()

            return if (lastValidSpec != null && nextPrefixBuilder.isNotEmpty()) {
                preFilterSeq.map {
                    if (it == lastValidSpec) {
                        HostSpec(it.prefix, it.suffix + nextPrefixBuilder.toString(), it.host!!, it.rangeGetter)
                    } else it
                }.toList()
            } else preFilterSeq.toList()
        }
    }
}

/**
 * One host with its prefix and suffix, resilient to local replacements
 * (ie mods in the injected file).
 */
class HostSpec(val prefix: String?, val suffix: String?,
               host: PsiLanguageInjectionHost,
               val rangeGetter: (PsiLanguageInjectionHost) -> TextRange) {

    init {
        remapHost(host)
    }

    fun getRangeInsideHost(theHost: PsiLanguageInjectionHost) = rangeGetter(theHost)

    val host: PsiLanguageInjectionHost?
        get() {
            val curHost = HostIndex[this]!!.element ?: return null // stale pointer

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
