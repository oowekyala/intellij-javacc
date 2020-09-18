package com.github.oowekyala.ijcc.lang.psi

import com.github.oowekyala.ijcc.lang.psi.impl.jccEltFactory
import com.github.oowekyala.ijcc.lang.util.JccCoreTestBase
import com.github.oowekyala.ijcc.lang.util.project
import org.junit.Test

/**
 * @author Clément Fournier
 * @since 1.0
 */
class JccElementFactoryTest : JccCoreTestBase() {

    // mostly catches ClassCastExceptions

    @Test
    fun testCreateOptionValue() {
        //TODO

    }

    @Test
    fun testCreateRegexReference() {
        project.jccEltFactory.createRegexElement<JccTokenReferenceRegexUnit>("<FOO>")
    }

    @Test
    fun testCreateLiteralRegexUnit() {
        project.jccEltFactory.createRegexElement<JccLiteralRegexUnit>("\"foo\"")
    }

    @Test
    fun testCreateBnfExpansion() {
        // TODO
    }

    @Test
    fun testCreateIdentifier() {
        project.jccEltFactory.createIdentifier("mlady")
    }


    @Test
    fun testCreateJavaExpression() {
        project.jccEltFactory.createJavaExpression("1+2")
    }


    @Test
    fun testCreateJavaBlock() {
        project.jccEltFactory.createJavaBlock("{ hey(); }")
    }


    @Test
    fun testCreateAssignmentLhs() {
        project.jccEltFactory.createAssignmentLhs("foo.bar")
    }


}
