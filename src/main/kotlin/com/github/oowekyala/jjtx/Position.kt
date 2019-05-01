package com.github.oowekyala.jjtx

import com.github.oowekyala.ijcc.lang.psi.lineNumber
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile

/**
 * @author Clément Fournier
 */
interface Position {

    fun toString(jjtxRunContext: JjtxRunContext): String

}


data class JsonPosition(val path: List<String>) : Position {

    constructor(first: String) : this(listOf(first))

    fun resolve(key: String) = JsonPosition(path + key)

    override fun toString(jjtxRunContext: JjtxRunContext): String = toString()

    override fun toString(): String = path.joinToString(" / ") { "\"$it\"" }
}


data class FilePosition(val line: Int, val column: Int, val file: PsiFile) : Position {

    override fun toString(jjtxRunContext: JjtxRunContext): String = "${file.name} [$line, $column]"

}


fun PsiElement.position(): FilePosition {
    val line = lineNumber
    return FilePosition(
        line = line,
        column = textOffset - StringUtil.lineColToOffset(containingFile.text, line, 0),
        file = containingFile
    )
}
