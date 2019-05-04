package com.github.oowekyala.ijcc.lang


const val TestResourcesPath = "src/test/resources/"
const val PackagePath = "com/github/oowekyala/ijcc/"

private const val Fixtures = "fixtures"
const val FoldingTestDataPath = "$TestResourcesPath${PackagePath}lang/folding/$Fixtures"
const val OptionsTestDataPath = "$TestResourcesPath${PackagePath}lang/options/$Fixtures"
const val InjectionTestDataPath = "$TestResourcesPath${PackagePath}lang/injection/$Fixtures"
const val ParserTestDataPath = "$TestResourcesPath${PackagePath}lang/parser/$Fixtures"


//fun KClass<*>.dataPath(vararg addSegments: String) =
//        this.java.`package`.name
//            .replace('.', '/')
//            .let { "$it/${addSegments.joinToString(separator = "/")}" }