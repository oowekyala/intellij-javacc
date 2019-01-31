package com.github.oowekyala.ijcc.insight.inspections

import com.github.oowekyala.ijcc.util.JccAnnotationTestBase

abstract class JccInspectionTestBase(
    val inspection: JavaccInspectionBase
) : JccAnnotationTestBase() {

    private fun enableInspection() = myFixture.enableInspections(inspection)

    override fun configureByText(text: String) {
        super.configureByText(text)
        enableInspection()
    }

}