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
 * @author Clément Fournier
 */
interface Position {

    override fun toString(): String

}

data class YamlPosition(
    val startMark: Mark?,
    val endMark: Mark?
) : Position {

    override fun toString(): String = startMark.toString()
}

data class LineAndCol(val line: Int, val column: Int) : Position {

    fun upgrade(text: CharSequence, filePath: Path): Position =
        position(line, column, text, filePath)

}

fun position(textOffset: Int, text: CharSequence, filePath: Path): Position =
    GenericFilePosition(textOffset, text, filePath)

fun position(line: Int, column: Int, text: CharSequence, filePath: Path): Position =
    GenericFilePosition(line, column, text, filePath)


private class GenericFilePosition(
    val snippet: String
) : Position {


    override fun toString(): String = snippet

    companion object {
        // The goal of these "constructors" is to not store the full text!

        operator fun invoke(offset: Int, text: CharSequence, filePath: Path): GenericFilePosition {
            val (l, c) = text.getColAndLine(offset)

            val snippet = buildSnippet(
                line = l,
                column = c,
                offset = offset,
                text = text,
                filePath = filePath
            )
            return GenericFilePosition(snippet)
        }

        operator fun invoke(line: Int, column: Int, text: CharSequence, filePath: Path): GenericFilePosition {
            val offset = (line to column).toOffset(text)
            val snippet = buildSnippet(
                line = line,
                column = column,
                offset = offset,
                text = text,
                filePath = filePath
            )
            return GenericFilePosition(snippet)

        }

        operator fun invoke(offset: Int, psiFile: PsiFile) =
            this(offset, psiFile.text, Paths.get(psiFile.virtualFile.path))

        private fun buildSnippet(line: Int, column: Int, offset: Int, text: CharSequence, filePath: Path): String {
            return buildString {
                append(" in ")
                append(filePath)
                append(":").append(line + 1).append(":").append(column).append(":\n")
                append(getSnippet(buffer = text, textOffset = offset))
            }
        }
    }
}




fun YamlPosition.addName(name: String?) = YamlPosition(
    startMark?.addName(name),
    endMark?.addName(name)
)

private fun Mark.addName(name: String?): Mark =
    name?.let {
        Mark(
            it,
            this.index,
            this.line,
            this.column,
            this.buffer,
            this.pointer
        )
    } ?: this

typealias LineAndColumn = Pair<Int, Int>


data class JsonPosition(val path: List<String>) : Position {

    constructor(first: String) : this(listOf(first))

    fun resolve(key: String) = JsonPosition(path + key)

    override fun toString(): String = "At " + path.joinToString(" / ") { "\"$it\"" }

    infix fun findIn(map: DataAstNode): DataAstNode? = map.findPointer(path)

}


fun PsiElement.position(): Position = GenericFilePosition(textOffset, containingFile)


fun CharSequence.getColAndLine(offset: Int): LineAndColumn {
    val line = StringUtil.offsetToLineNumber(this, offset)
    return Pair(line, offset - StringUtil.lineColToOffset(this, line, 0))
}

fun LineAndColumn.toOffset(text: CharSequence) = StringUtil.lineColToOffset(text, first, second)


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
