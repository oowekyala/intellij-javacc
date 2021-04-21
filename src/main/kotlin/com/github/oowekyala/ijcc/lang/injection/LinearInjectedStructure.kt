package com.github.oowekyala.ijcc.lang.injection

import com.github.oowekyala.ijcc.lang.psi.JccGrammarFileRoot
import com.github.oowekyala.ijcc.util.plusAssign
import com.github.oowekyala.ijcc.util.removeLast
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
 * but the whole file has to be reinjected though.
 *
 * @author Clément Fournier
 * @since 1.0
 */
class LinearInjectedStructure(hostSpecs: List<HostSpec>) {


    var hostSpecs = hostSpecs
        private set


    fun prepareRegister(): List<HostSpec> {
        hostSpecs = removeStaleSpecs(hostSpecs)

        return hostSpecs
    }

    private companion object {
        fun removeStaleSpecs(specs: List<HostSpec>): List<HostSpec> {

            if (specs.isEmpty()) return emptyList()

            var lastValidSpec: HostSpec? = null
            val nextPrefixBuilder = StringBuilder()

            val preFilterList = mutableListOf<HostSpec>()

            // only yields valid specs, the last suffix needs to be adjusted though
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

                    preFilterList += myRes
                } else {
                    // host == null
                    // LOG { debug("Removed null host") }
                    nextPrefixBuilder += spec.prefix.orEmpty()
                    nextPrefixBuilder += spec.suffix.orEmpty()
                    // continue until the next valid spec
                }
            }


            if (lastValidSpec == null) return emptyList()

            if (nextPrefixBuilder.isNotEmpty()) {
                preFilterList += preFilterList.removeLast().let {
                    HostSpec(it.prefix, it.suffix + nextPrefixBuilder.toString(), it.host!!, it.rangeGetter)
                }
            }

            return preFilterList
        }
    }
}

/**
 * One host with its prefix and suffix, resilient to local replacements
 * (ie mods in the injected file).
 * Part of the linear injected structure.
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
            val curHost = HostIndex[this]?.element ?: return null // stale pointer

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

    /**
     * Creates a new [HostSpec] based on this one but with the [additionalSuffix] appended to this suffix.
     * This spec is thereafter unusable.
     */
    fun appendSuffixAndDestroy(additionalSuffix: CharSequence): HostSpec {
        val host = host
        return if (host != null) {
            // this makes this spec unusable
            HostIndex.remove(this)
            HostSpec(
                prefix = prefix,
                suffix = (suffix ?: "") + additionalSuffix,
                host = host,
                rangeGetter = rangeGetter
            )
        } else {
            this
        }
    }

    private fun remapHost(newHost: PsiLanguageInjectionHost) {
        HostIndex[this] = SmartPointerManager.createPointer(newHost)
    }

    companion object {
        /** Global indices of leaves to actual injection hosts. */
        private val HostIndex: MutableMap<HostSpec, SmartPsiElementPointer<PsiLanguageInjectionHost>> =
            HashMap()

        private val ReplaceMap: MutableMap<PsiLanguageInjectionHost, PsiLanguageInjectionHost> =
            HashMap()

        fun replaceHost(replaced: PsiLanguageInjectionHost, replacement: PsiLanguageInjectionHost) {
            ReplaceMap[replaced] = replacement
        }
    }
}

