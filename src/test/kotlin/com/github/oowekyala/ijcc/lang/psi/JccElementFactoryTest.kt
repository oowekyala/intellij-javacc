package com.github.oowekyala.ijcc.lang.psi

import com.github.oowekyala.ijcc.lang.psi.impl.JccElementFactory
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase

/**
 * @author Clément Fournier
 * @since 1.0
 */
class JccElementFactoryTest : LightCodeInsightFixtureTestCase() {

    // mostly catches ClassCastExceptions


    fun testCreateOptionValue() {
        //TODO

    }

    fun testCreateRegexReference() {
        JccElementFactory.createRegexReferenceUnit(project, "<FOO>")
    }

    fun testCreateLiteralRegexUnit() {
        JccElementFactory.createLiteralRegexUnit(project, "\"foo\"")
    }

    fun testCreateBnfExpansion() {
        // TODO
    }

    fun testCreateIdentifier() {
        JccElementFactory.createIdentifier(project, "mlady")
    }


    fun testCreateJavaExpression() {
        JccElementFactory.createJavaExpression(project, "1+2")
    }


    fun testCreateJavaBlock() {
        JccElementFactory.createJavaBlock(project, "{ hey(); }")
    }


    fun testCreateAssignmentLhs() {

        JccElementFactory.createAssignmentLhs(project, "foo.bar")
    }


}