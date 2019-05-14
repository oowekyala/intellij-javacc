package com.github.oowekyala.jjtx.util.dataAst

import com.github.oowekyala.jjtx.util.JsonPointer
import io.kotlintest.specs.WordSpec
import kotlinx.collections.immutable.immutableListOf

/**
 * @author Clément Fournier
 */
class PositionTests : WordSpec({


    "A json pointer" should {

        "contain its children" {
            assert(JsonPointer("a", "b") in JsonPointer("a"))
            assert(JsonPointer("a") !in JsonPointer("a", "b"))
        }

        "contain its descendants" {
            assert(JsonPointer("a", "b", "c") in JsonPointer("a"))
            assert(JsonPointer("a") !in JsonPointer("a", "b", "c") )
        }

        "contain itself" {
            assert(JsonPointer("a", "b", "c") in JsonPointer("a", "b", "c"))
            assert(JsonPointer.Root in JsonPointer.Root)
        }

    }

    "The root position" should {

        "contain any pointer" {
            assert(JsonPointer("a", "b", "c") in JsonPointer())
        }

        "have no segments" {
            assert(JsonPointer().path.isEmpty())
            assert(JsonPointer() == JsonPointer.Root)
            assert(JsonPointer(immutableListOf()) == JsonPointer.Root)
        }
    }


})
