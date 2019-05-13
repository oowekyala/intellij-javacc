package com.github.oowekyala.jjtx.util.dataAst

import com.github.oowekyala.jjtx.Jjtricks
import com.github.oowekyala.jjtx.JjtxOptsModel
import com.github.oowekyala.jjtx.util.io.NamedInputStream
import io.kotlintest.matchers.string.shouldContain
import org.everit.json.schema.ValidationException
import org.junit.Test


class SchemaValidationTest {

    private fun toNis(fname: String): NamedInputStream = Jjtricks.getResourceAsStream("/jjtx/util/dataAst/$fname")!!

    fun neg(fname: String, neg: Boolean = true, errors: ValidationException.() -> Unit = {}) {
        neg(toNis(fname), neg, errors)
    }

    fun neg(nis: NamedInputStream, neg: Boolean = true, errors: ValidationException.() -> Unit = {}) {

        val ast = parseGuessFromExtension(nis)
        var hasErrors = false

        ast.validateJjtopts {
            errors()
            hasErrors = true
        }

        if (neg && !hasErrors) {
            throw AssertionError("Expected schema validation failure!")
        }

    }

    fun pos(fname: String): Unit = pos(toNis(fname))

    fun pos(nis: NamedInputStream): Unit = neg(nis, false) {
        throw this
    }


    @Test
    fun neg1() = neg("Neg1.yaml") {
        message shouldContain "fooBr"
    }

    @Test
    fun pos1() = pos("Pos1.yaml")

    @Test
    fun visitorPos() = pos("VisitorPos.yaml")


    @Test
    fun typeHierarchyPos() = pos("TypeHierarchyPos.yaml")


    @Test
    fun assertRootValidates() = pos(JjtxOptsModel.RootJjtOpts)


}
