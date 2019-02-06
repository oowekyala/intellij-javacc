package com.github.oowekyala.ijcc.insight.inspections

import com.github.oowekyala.ijcc.util.JccAnnotationTestBase

abstract class JccInspectionTestBase(
    private val inspection: JccInspectionBase
) : JccAnnotationTestBase() {

    override fun configureByText(text: String) {
        super.configureByText(text)
        myFixture.enableInspections(inspection)
    }
}