package com.github.oowekyala.jjtx.util.dataAst


fun DataAstNode.prettyPrint(indent: String = "    "): String =
    StringBuilder().also {
        this.prettyPrintImpl(indent, "", it)
    }.toString()

private fun DataAstNode?.prettyPrintImpl(baseIndent: String, indent: String, sb: StringBuilder) {
    val inindent = indent + baseIndent

    when (this) {
        is AstMap    -> {
            if (this.isEmpty()) sb.append("{}")
            else {
                sb.appendln("{")
                for ((k, v) in this) {
                    sb.append(inindent).append('"').append(k).append('"').append(": ")
                    v.prettyPrintImpl(baseIndent, inindent + baseIndent, sb)
                    sb.appendln()
                }
                sb.append(indent).appendln("}")

            }
        }

        is AstSeq    -> {
            if (this.isEmpty()) sb.append("[]")
            else {
                sb.appendln("[")
                for (v in this) {
                    sb.append(inindent)
                    v.prettyPrintImpl(baseIndent, inindent, sb)
                    sb.appendln()
                }
                sb.append(indent).appendln("]")
            }
        }
        is AstScalar -> {
            when {
                any.any { it == '\r' || it == '\n' } -> {
                    sb.appendln()
                    sb.append(any.replaceIndent(inindent))
                }
                type == ScalarType.STRING            -> sb.append('"').append(any).append('"')
                else                                 -> sb.append(any)
            }

        }
    }
}
