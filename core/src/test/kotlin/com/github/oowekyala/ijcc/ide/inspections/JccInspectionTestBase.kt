package com.github.oowekyala.ijcc.ide.inspections

import com.github.oowekyala.ijcc.util.JccAnnotationTestBase
import com.intellij.psi.PsiFile

abstract class JccInspectionTestBase(
    private val inspection: JccInspectionBase
) : JccAnnotationTestBase() {

    override fun configureByText(text: String): PsiFile =
        super.configureByText(text)
            .also {
                myFixture.enableInspections(inspection)
            }
}