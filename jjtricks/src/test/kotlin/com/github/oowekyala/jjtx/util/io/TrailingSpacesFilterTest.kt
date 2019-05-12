package com.github.oowekyala.jjtx.util.io

import io.kotlintest.shouldBe
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.PrintStream

/**
 * @author Clément Fournier
 */
class TrailingSpacesFilterTest {


    private fun printTest(ps: PrintStream.() -> Unit): String {

        val bos = ByteArrayOutputStream()
        val filter = PrintStream(TrailingSpacesFilterOutputStream(bos))
        filter.ps()
        filter.flush()
        return bos.toString(Charsets.UTF_8.name())
    }

    @Test
    fun `test simple print`() = printTest {
        print("Foo ")
    } shouldBe "Foo "

    @Test
    fun `test simple println`() = printTest {
        println("Foo ")
    } shouldBe "Foo\n"


    @Test
    fun `test other println`() = printTest {
        println("Foo ")
        print("Foo ")
    } shouldBe "Foo\nFoo "


}
