@file:JvmName("JjtricksUtil")

package com.github.oowekyala.jjtx.util

import com.github.oowekyala.ijcc.lang.psi.JccFile
import com.intellij.openapi.util.text.StringUtil
import org.apache.commons.lang3.text.WordUtils
import org.apache.velocity.VelocityContext
import org.apache.velocity.app.VelocityEngine
import java.io.FileInputStream
import java.io.InputStream
import java.io.Reader
import java.io.StringWriter
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

fun String.splitAroundFirst(delimiter: Char): Pair<String, String> =
    Pair(substringBefore(delimiter, missingDelimiterValue = ""), substringAfter(delimiter))

/**
 * Returns a pair of the substrings before and after the last occurrence of the [delimiter].
 * If the delimiter is not found in the string, then
 * - if [firstBias], returns a Pair([this], ""),
 * - else returns Pair("", [this])
 */
fun String.splitAroundLast(delimiter: Char, firstBias: Boolean = false): Pair<String, String> =
    Pair(
        substringBeforeLast(delimiter, missingDelimiterValue = if (firstBias) this else ""),
        substringAfterLast(delimiter, missingDelimiterValue = if (firstBias) "" else this)
    )

fun String.removeTrailingSpaces() = StringUtil.trimTrailing(this)

val Path.extension: String?
    get() = toFile().extension.takeIf { it.isNotEmpty() }

val Path.baseName: String
    get() = fileName.toString().substringBefore('.')

fun Path.bufferedReader(): Reader = toFile().bufferedReader()
fun Path.inputStream(): InputStream = FileInputStream(toFile())
fun Path.isDirectory(): Boolean = Files.isDirectory(this)
fun Path.exists(): Boolean = Files.exists(this)
fun Path.isFile(): Boolean = Files.isRegularFile(this)
fun Path.createFile() {
    Files.createDirectories(parent)
    Files.createFile(this)
}

fun <K, V, R> Map<K, V>.mapValuesNotNull(f: (Map.Entry<K, V>) -> R?): Map<K, R> =
    mapValuesTo(mutableMapOf(), f).filterValues { it != null } as Map<K, R>

fun String.wrap(lineLength: Int, indent: Int = 0): String =
    WordUtils.wrap(this, lineLength, "\n".padEnd(indent + 1), false)

fun Path.overwrite(contents: () -> String) = toFile().apply {
    parentFile.mkdirs()
    createNewFile()
    writeText(contents())
}

fun String.toPath() = Paths.get(this)


operator fun VelocityContext.plus(map: Map<String, Any?>) = VelocityContext(map.toMutableMap(), this)

fun VelocityEngine.evaluate(ctx: VelocityContext, template: String, logId: String = "jjtx-velocity"): String =
    if (template.indexOfAny(charArrayOf('$', '#')) < 0)
    // shortcut when the string is a constant template (has no meta characters)
        template
    else StringWriter().also {
        this.evaluate(ctx, it, logId, template)
    }.toString()

inline fun VelocityEngine.evaluate(ctx: VelocityContext,
                                   template: String,
                                   logId: String = "jjtx-velocity",
                                   onException: (Throwable) -> Nothing): String =
        try {
            this.evaluate(ctx = ctx, logId = logId, template = template)
        } catch (e: Throwable) {
            onException(e)
        }

internal fun <R : Any, T> ReadOnlyProperty<R, T>.lazily(): ReadOnlyProperty<R, T> =
    object : ReadOnlyProperty<R, T> {

        private lateinit var myThisRef: R
        private lateinit var myProperty: KProperty<*>

        val v: T by lazy {
            this@lazily.getValue(myThisRef, myProperty)
        }

        override fun getValue(thisRef: R, property: KProperty<*>): T {
            myThisRef = thisRef
            myProperty = property
            return v
        }
    }


internal fun <T, R> ReadOnlyProperty<Any, T>.map(f: (T) -> R): ReadOnlyProperty<Any, R> =
    object : ReadOnlyProperty<Any, R> {
        override fun getValue(thisRef: Any, property: KProperty<*>): R =
            f(this@map.getValue(thisRef, property))
    }


fun String.matches(regex: String): Boolean = matches(Regex(regex))
val JccFile.path: Path
    get() = Paths.get(virtualFile.path).normalize()
const val baseIndent = "    "
operator fun VelocityContext.set(key: String, value: Any) {
    put(key, value)
}
