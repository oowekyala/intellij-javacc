package com.github.oowekyala.jjtx.util

import com.github.oowekyala.ijcc.lang.psi.lineNumber
import com.github.oowekyala.jjtx.JjtxRunContext
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import org.yaml.snakeyaml.error.Mark

/**
 * @author Clément Fournier
 */
interface Position {

    fun toString(jjtxRunContext: JjtxRunContext): String = toString()

}

data class YamlPosition(
    val startMark: Mark?,
    val endMark: Mark?
) : Position {

    override fun toString(): String = startMark.toString()
}


data class JsonPosition(val path: List<String>) : Position {

    constructor(first: String) : this(listOf(first))

    fun resolve(key: String) = JsonPosition(path + key)

    override fun toString(): String = "At " + path.joinToString(" / ") { "\"$it\"" }
}


data class FilePosition(val line: Int, val column: Int, val file: PsiFile) : Position {

    override fun toString(jjtxRunContext: JjtxRunContext): String = "in '${file.name}' [$line, $column]"

}


fun PsiElement.position(): FilePosition {
    val line = lineNumber
    return FilePosition(
        line = line,
        column = textOffset - StringUtil.lineColToOffset(containingFile.text, line, 0),
        file = containingFile
    )
}
