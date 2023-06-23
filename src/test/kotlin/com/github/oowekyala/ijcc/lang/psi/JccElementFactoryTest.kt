package com.github.oowekyala.ijcc.lang.psi

import com.github.oowekyala.ijcc.lang.psi.impl.jccEltFactory
import com.github.oowekyala.ijcc.lang.util.JccTestBase
import org.junit.Test

/**
 * @author Clément Fournier
 * @since 1.0
 */
class JccElementFactoryTest : JccTestBase() {

    // mostly catches ClassCastExceptions

    
    fun testCreateOptionValue() {
        //TODO

    }

    
    fun testCreateRegexReference() {
        project.jccEltFactory.createRegexElement<JccTokenReferenceRegexUnit>("<FOO>")
    }

    
    fun testCreateLiteralRegexUnit() {
        project.jccEltFactory.createRegexElement<JccLiteralRegexUnit>("\"foo\"")
    }

    
    fun testCreateBnfExpansion() {
        // TODO
    }

    
    fun testCreateIdentifier() {
        project.jccEltFactory.createIdentifier("mlady")
    }


    
    fun testCreateJavaExpression() {
        project.jccEltFactory.createJavaExpression("1+2")
    }


    
    fun testCreateJavaBlock() {
        project.jccEltFactory.createJavaBlock("{ hey(); }")
    }


    
    fun testCreateAssignmentLhs() {
        project.jccEltFactory.createAssignmentLhs("foo.bar")
    }


}
