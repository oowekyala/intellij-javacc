package com.github.oowekyala.jjtx.util

import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.util.text.StringUtil.isLineBreak
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import org.yaml.snakeyaml.error.Mark

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


class PsiFilePosition(
    val textOffset: Int,
    val psiFile: PsiFile
) : Position {

    val line: Int
    val column: Int

    init {
        val (l, c) = getColAndLine(psiFile, textOffset)
        line = l
        column = c
    }

    override fun toString(): String = buildString {
        append(" in ")
        append(psiFile.virtualFile.path)
        append(":").append(line + 1).append(":").append(column).append(":\n")
        append(getSnippet(buffer = psiFile.text, textOffset = textOffset))
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
}


fun PsiElement.position(): PsiFilePosition = PsiFilePosition(textOffset, containingFile)

//kept for posterity
private fun getColAndLine(psiFile: PsiFile, offset: Int): LineAndColumn {
    val text = psiFile.text
    val line = StringUtil.offsetToLineNumber(text, offset)
    return Pair(line, offset - StringUtil.lineColToOffset(text, line, 0))
}


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
