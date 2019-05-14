@file:JvmName("PositionUtils")

package com.github.oowekyala.jjtx.util

import com.github.oowekyala.jjtx.util.dataAst.DataAstNode
import com.github.oowekyala.jjtx.util.dataAst.findPointer
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.util.text.StringUtil.isLineBreak
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import org.yaml.snakeyaml.error.Mark
import java.nio.file.Path
import java.nio.file.Paths

/**
 * Represents a position in some file.
 * Position instances must be able to describe themselves,
 * and should not hold resources that this function doesn't
 * need, eg the file contents. Only used for error messages.
 *
 * @author Clément Fournier
 */
interface Position {

    override fun toString(): String

}

data class LineAndCol(val line: Int, val column: Int) : Position

/**
 * Upgrade to a position with a full snippet.
 */
fun LineAndCol.upgrade(text: CharSequence, filePath: Path): Position =
    position(line, column, text, filePath)

fun position(textOffset: Int, text: CharSequence, filePath: Path): Position =
    GenericFilePosition(textOffset, text, filePath)

fun position(line: Int, column: Int, text: CharSequence, filePath: Path): Position =
    GenericFilePosition(line, column, text, filePath)


private class GenericFilePosition private constructor(
    val snippet: String
) : Position {


    override fun toString(): String = snippet

    companion object {
        // The goal of these "constructors" is to not store the full text!

        operator fun invoke(kemar: Mark, fname: String?): GenericFilePosition =
            with(kemar) {
                GenericFilePosition(
                    buildSnippet(
                        line,
                        column,
                        pointer,
                        String(buffer, 0, buffer.size),
                        fname ?: name
                    )
                )
            }

        operator fun invoke(offset: Int, text: CharSequence, filePath: Path): GenericFilePosition {
            val (l, c) = text.getColAndLine(offset)

            val snippet = buildSnippet(
                line = l,
                column = c,
                offset = offset,
                text = text,
                filePath = filePath.toString()
            )
            return GenericFilePosition(snippet)
        }

        operator fun invoke(line: Int, column: Int, text: CharSequence, filePath: Path): GenericFilePosition {
            val offset = LineAndCol(line, column).toOffset(text)
            val snippet = buildSnippet(
                line = line,
                column = column,
                offset = offset,
                text = text,
                filePath = filePath.toString()
            )
            return GenericFilePosition(snippet)

        }

        operator fun invoke(offset: Int, psiFile: PsiFile) =
            this(offset, psiFile.text, Paths.get(psiFile.virtualFile.path))

        private fun buildSnippet(line: Int, column: Int, offset: Int, text: CharSequence, filePath: String): String {
            return buildString {
                append(" in ")
                append(filePath)
                append(":").append(line + 1).append(":").append(column).append(":\n")
                append(getSnippet(buffer = text, textOffset = offset))
            }
        }
    }
}


fun Mark.toFilePos(name: String? = null): Position =
    GenericFilePosition(this, name)

/**
 * A Json pointer.
 */
data class JsonPosition(val path: List<String>) : Position {

    constructor(first: String) : this(listOf(first))

    fun resolve(key: String) = JsonPosition(path + key)

    override fun toString(): String = path.joinToString("/", prefix = "/")

    infix fun findIn(map: DataAstNode): DataAstNode? = map.findPointer(path)

    /**
     * Returns true if this pointer is a subpath of the [other] pointer.
     * Eg if
     *
     * ```
     * | p      | q      | p in q | q in p |
     * |--------|--------|--------|--------|
     * | /a/b   | /a     | true   | false  |
     * | /a/0   | /a/1   | false  | false  |
     * | /a/0/c | /a/0/c | true   | true   |
     *
     * ```
     *
     *
     */
    operator fun contains(other: JsonPosition): Boolean =
        path.zip(other.path).takeWhile { it.first == it.second }.size == other.path.size

    companion object {
        val Root = JsonPosition(emptyList())
    }

}


fun PsiElement.position(): Position = GenericFilePosition(textOffset, containingFile)


fun CharSequence.getColAndLine(offset: Int): LineAndCol {
    val line = StringUtil.offsetToLineNumber(this, offset)
    return LineAndCol(line, offset - StringUtil.lineColToOffset(this, line, 0))
}

fun LineAndCol.toOffset(text: CharSequence) = StringUtil.lineColToOffset(text, line, column)


private fun getSnippet(buffer: CharSequence, textOffset: Int): String = getSnippet(buffer, textOffset, 4, 75)

private fun getSnippet(buffer: CharSequence, textOffset: Int, indent: Int, maxLength: Int): String {
    val half = (maxLength / 2 - 1).toFloat()
    var start = textOffset
    var head = ""
    while (start > 0 && !isLineBreak(buffer[start - 1])) {
        start -= 1
        if (textOffset - start > half) {
            head = " ... "
            start += 5
            break
        }
    }
    var tail = ""
    var end = textOffset
    while (end < buffer.length && !isLineBreak(buffer[end])) {
        end += 1
        if (end - textOffset > half) {
            tail = " ... "
            end -= 5
            break
        }
    }

    val result = StringBuilder()
    for (i in 0 until indent) {
        result.append(" ")
    }
    result.append(head)
    for (i in start until end) {
        result.append(buffer[i])
    }
    result.append(tail)
    result.append("\n")
    for (i in 0 until indent + textOffset - start + head.length) {
        result.append(" ")
    }
    result.append("^")
    return result.toString()
}
