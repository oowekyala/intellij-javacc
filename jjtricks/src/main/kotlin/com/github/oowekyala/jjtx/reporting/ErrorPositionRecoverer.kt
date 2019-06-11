package com.github.oowekyala.jjtx.reporting

import com.github.oowekyala.jjtx.util.LineAndCol
import com.github.oowekyala.jjtx.util.Position
import com.google.googlejavaformat.java.FormatterException
import org.apache.velocity.exception.ParseErrorException
import org.yaml.snakeyaml.scanner.ScannerException
import java.nio.file.Path
import java.nio.file.Paths


data class PositionedMessage(
    val message: String,
    val position: Position,
    val path: Path?
)

val PositionedMessage?.destructured: Triple<String?, Position?, Path?>
    get() =
        if (this == null) Triple(null, null, null)
        else Triple(message, position, path)

interface ErrorPositionRecoverer {

    fun extract(throwable: Throwable): PositionedMessage?

}

abstract class BasePositionParser<T : Throwable>(val tClass: Class<T>) : ErrorPositionRecoverer {

    override fun extract(throwable: Throwable): PositionedMessage? {
        if (!tClass.isInstance(throwable)) return null

        return throwable.message?.let {
            parseMessage(it)
        }
    }

    protected abstract fun parseMessage(errorMessage: String): PositionedMessage?


}


object VelocityExtractor : BasePositionParser<ParseErrorException>(ParseErrorException::class.java) {

    override fun parseMessage(errorMessage: String): PositionedMessage? =
        r.find(errorMessage)?.let { match ->
            val (path, line, col) = match.destructured

            val m = errorMessage.removeRange(match.range)

            PositionedMessage(
                m,
                LineAndCol(line.toInt(), col.toInt() + 1),
                Paths.get(path)
            )
        }


    private val r = Regex("at (.*)\\[line (\\d+), column (\\d+)]")

}

object GJFormatExtractor : BasePositionParser<FormatterException>(FormatterException::class.java) {

    override fun parseMessage(errorMessage: String): PositionedMessage? =
        lcRegex.find(errorMessage.trim())?.let {
            val (line, col, message) = it.destructured
            PositionedMessage(
                message,
                LineAndCol(line.toInt() - 1, col.toInt() - 1),
                null
            )
        }

    private val lcRegex = Regex("(\\d+):(\\d+): error:(.*)")

}

object SnakeYamlLineColExtractor : BasePositionParser<ScannerException>(ScannerException::class.java) {

    override fun parseMessage(errorMessage: String): PositionedMessage? =
        lcRegex.matchEntire(errorMessage.trim())?.let {
            val (message, line, col) = it.destructured
            PositionedMessage(
                message,
                LineAndCol(line.toInt() - 1, col.toInt() - 1),
                null
            )
        }

    private val lcRegex = Regex("(.*)in 'reader', line (\\d+), column (\\d+):.*", RegexOption.DOT_MATCHES_ALL)

}
