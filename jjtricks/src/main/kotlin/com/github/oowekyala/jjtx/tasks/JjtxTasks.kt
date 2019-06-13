package com.github.oowekyala.jjtx.tasks

import com.github.oowekyala.ijcc.lang.model.parserPackage
import com.github.oowekyala.jjtx.Jjtricks
import com.github.oowekyala.jjtx.JjtxContext
import com.github.oowekyala.jjtx.JjtxOptsModel
import com.github.oowekyala.jjtx.util.io.Io
import com.github.oowekyala.jjtx.util.path
import com.github.oowekyala.jjtx.util.resolveQname
import com.github.oowekyala.jjtx.util.splitAroundFirst
import java.nio.file.Path
import java.util.concurrent.CompletableFuture
import kotlin.collections.set
import org.javacc.parser.Main as JavaccMain


enum class JjtxTaskKey(
    val ref: String,
    private val taskBuilder: (TaskCtx) -> JjtxTask,
    /** Tasks that must be entirely completed before this task may be run. */
    vararg val serialDependencies: JjtxTaskKey
) {
    DUMP_CONFIG("help:dump-config", ::DumpConfigTask),
    GEN_COMMON("gen:common", ::CommonGenTask),
    GEN_NODES("gen:nodes", ::GenerateNodesTask),
    GEN_SUPPORT("gen:javacc-support", ::GenerateJavaccSupportFilesTask),
    GEN_JAVACC("gen:javacc", ::GenerateJavaccTask),
    GEN_PARSER("gen:parser", ::JavaccExecTask, GEN_JAVACC)
    ;



    val namespace = ref.substringBefore(':')
    val localName = ref.substringAfter(':')

    override fun toString(): String = ref


    fun execute(taskCtx: TaskCtx): CompletableFuture<Void?> {
        val task = taskBuilder(taskCtx)
        return when {
            !Jjtricks.TEST_MODE -> CompletableFuture.runAsync(task::execute)
            else                -> CompletableFuture.completedFuture(task.execute()).thenApply { null }
        }
    }


    companion object {
        private val nss = mutableMapOf<String, MutableList<String>>()
        private val refs = mutableMapOf<String, JjtxTaskKey>()

        init {
            for (k in values()) {
                nss.computeIfAbsent(k.namespace) { mutableListOf() } += k.localName
                refs[k.ref] = k
            }
        }

        fun parse(string: String, io: Io): List<JjtxTaskKey> {
            if (string in refs) return listOf(refs[string]!!)

            val (ns, local) = string.splitAroundFirst(':')

            return when {
                // foo:*
                local == "*" && ns.isNotEmpty() -> {
                    val nskeys = nss.getOrDefault(ns, emptyList<String>())
                    if (nskeys.isEmpty()) {
                        io.bail("Task group $ns not known, available tasks ${values().toSet()}")
                    }

                    nskeys.mapNotNull { refs["$ns:$it"] }
                }
                // foo is local
                ns.isEmpty()                    ->
                    refs.values.firstOrNull { it.localName == local }?.let { listOf(it) }
                        ?: io.bail("Task $local not known, available tasks ${values().toSet()}")
                else                            -> emptyList()
            }

        }

    }
}


abstract class JjtxTask {

    abstract fun execute()

}


val JjtxContext.chainDump
    get() =
        configChain
            .map { io.wd.relativize(it).normalize() }
            .plus(JjtxOptsModel.RootJjtOpts.filename)
            // the "element =" here is not optional, since Path <: Iterable<Path>,
            // it could append all segments if not disambiguated
            .plus(element = io.wd.relativize(grammarFile.path))
            .joinToString(separator = " -> ")


internal fun JjtxContext.genJjPath(outputDir: Path) =
    outputDir.resolveQname(jjtxOptsModel.parserPackage).resolve("$grammarName.jj")


