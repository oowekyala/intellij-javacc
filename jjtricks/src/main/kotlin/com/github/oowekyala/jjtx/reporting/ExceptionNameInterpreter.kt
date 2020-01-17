package com.github.oowekyala.jjtx.reporting

import com.google.googlejavaformat.java.FormatterException as JavaFormatError
import org.apache.velocity.exception.ParseErrorException as VtlSyntaxError
import org.yaml.snakeyaml.scanner.ScannerException as YamlSyntaxError

/**
 * @author Clément Fournier
 */
object ExceptionNameInterpreter {


    fun getHeader(thrown: Throwable): String =
        when (thrown) {
            is JavaFormatError -> "Java formatter exception"
            is VtlSyntaxError  -> "Velocity parse error"
            is YamlSyntaxError -> "YAML syntax error"
            else               -> thrown.javaClass.name
        }


}
