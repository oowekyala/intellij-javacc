package com.github.oowekyala.ijcc.lang.psi


/**
 * Unescapes a string that contains standard Java escape sequences.
 *
 *  * **&#92;b &#92;f &#92;n &#92;r &#92;t &#92;" &#92;'** :
 * BS, FF, NL, CR, TAB, double and single quote.
 *  * **&#92;X &#92;XX &#92;XXX** : Octal character
 * specification (0 - 377, 0x00 - 0xFF).
 *  * **&#92;uXXXX** : Hexadecimal based Unicode character.
 *
 *
 * Courtesy of @uklimaschewski: https://gist.github.com/uklimaschewski/6741769
 *
 * @return The translated string.
 */
fun String.unescapeJavaString(): String {
    val sb = StringBuilder(length)
    var i = 0

    loop@
    while (i < length) {
        var ch = this[i]
        if (ch == '\\') {
            val nextChar = if (i == length - 1) '\\' else this[i + 1]
            // Octal escape?
            if (nextChar in '0'..'7') {
                var code = "" + nextChar
                i++
                if (i < length - 1 && this[i + 1] >= '0' && this[i + 1] <= '7') {
                    code += this[i + 1]
                    i++
                    if (i < length - 1 && this[i + 1] >= '0' && this[i + 1] <= '7') {
                        code += this[i + 1]
                        i++
                    }
                }
                sb.append(code.toInt(8).toChar())
                i++
                continue
            }
            when (nextChar) {
                '\\' -> ch = '\\'
                'b'  -> ch = '\b'
                'f'  -> ch = 12.toChar() // '\f'
                'n'  -> ch = '\n'
                'r'  -> ch = '\r'
                't'  -> ch = '\t'
                '\"' -> ch = '\"'
                '\'' -> ch = '\''
                'u'  -> {
                    if (i >= length - 5) {
                        ch = 'u'
                    } else {
                        val code =
                            ("" + this[i + 2] + this[i + 3]
                                + this[i + 4] + this[i + 5]).toInt(16)
                        sb.append(Character.toChars(code))
                        i += 5
                        i++
                        continue@loop
                    }
                }
            }
            i++
        }
        sb.append(ch)
        i++
    }
    return sb.toString()
}

/**
 * Replaces unprintable characters by their escaped (or unicode escaped)
 * equivalents in the given string
 */
fun String.escapeJava(): String {
    val retval = java.lang.StringBuilder()

    for (ch in this) {
        when (ch) {
            0.toChar()           -> {
            }
            '\b'                 -> retval.append("\\b")
            '\t'                 -> retval.append("\\t")
            '\n'                 -> retval.append("\\n")
            12.toChar() /*'\f'*/ -> retval.append("\\f")
            '\r'                 -> retval.append("\\r")
            '\"'                 -> retval.append("\\\"")
            '\''                 -> retval.append("\\'")
            '\\'                 -> retval.append("\\\\")
            else                 -> if (ch.code < 0x20 || ch.code > 0x7e) {
                val s = "0000" + ch.code.toString(16)
                retval.append("\\u").append(s.substring(s.length - 4))
            } else {
                retval.append(ch)
            }
        }
    }
    return retval.toString()
}

