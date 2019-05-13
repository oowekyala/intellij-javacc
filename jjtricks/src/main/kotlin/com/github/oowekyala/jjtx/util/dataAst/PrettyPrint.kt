package com.github.oowekyala.jjtx.util.dataAst


fun DataAstNode.prettyPrint(indent: String = "    ", includeTypes: Boolean = true): String =
    StringBuilder().also {
        this.prettyPrintImpl(indent, "", includeTypes, it)
    }.toString()

private fun DataAstNode?.prettyPrintImpl(baseIndent: String, indent: String, includeTypes: Boolean, sb: StringBuilder) {
    val inindent = indent + baseIndent

    when (this) {
        is AstMap    -> {
            if (this.isEmpty()) sb.append("{}")
            else {
                sb.appendln("{")
                for ((k, v) in this) {
                    sb.append(inindent).append('"').append(k).append('"').append(": ")
                    v.prettyPrintImpl(baseIndent, inindent + baseIndent, includeTypes, sb)
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
                    v.prettyPrintImpl(baseIndent, inindent, includeTypes, sb)
                    sb.appendln()
                }
                sb.append(indent).appendln("]")
            }
        }
        is AstScalar -> {
            fun printStr(s: String) {
                when {
                    s.any { it == '\r' || it == '\n' } -> {
                        sb.appendln()
                        sb.append(s.replaceIndentByMargin(inindent))
                    }
                    else                               ->
                        sb.append('"').append(s).append('"')
                }

            }

            if (includeTypes) {
                sb.append(type).append("(")
                printStr(any)
                sb.append(indent).append(")")
            } else when (type) {
                ScalarType.STRING -> printStr(any)
                else              -> sb.append(any)
            }

        }
    }
}
