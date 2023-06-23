package com.github.oowekyala.ijcc.lang

import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.matchers.shouldBe


const val TestResourcesPath = "src/test/resources/"
const val PackagePath = "com/github/oowekyala/ijcc/"

private const val Fixtures = "fixtures"
const val FoldingTestDataPath = "$TestResourcesPath${PackagePath}lang/folding/$Fixtures"
const val OptionsTestDataPath = "$TestResourcesPath${PackagePath}lang/options/$Fixtures"
const val InjectionTestDataPath = "$TestResourcesPath${PackagePath}lang/injection/$Fixtures"
const val ParserTestDataPath = "$TestResourcesPath${PackagePath}lang/parser/$Fixtures"


inline fun <reified T : Any> Any?.shouldBeA(t: (T) -> Unit) {
    this.shouldBeInstanceOf<T>()
    t(this as T)
}

fun <T : Any> Collection<T>.shouldContainOneSuch(t: (T) -> Unit) {
    this.any {
        try {
            t(it)
            true
        } catch (ass: AssertionError) {
            false
        }
    } shouldBe true
}

//fun KClass<*>.dataPath(vararg addSegments: String) =
//        this.java.`package`.name
//            .replace('.', '/')
//            .let { "$it/${addSegments.joinToString(separator = "/")}" }
