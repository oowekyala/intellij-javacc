package com.github.oowekyala.ijcc.insight.quickdoc

import com.github.oowekyala.ijcc.insight.quickdoc.JccDocUtil.buildQuickDoc
import com.github.oowekyala.ijcc.insight.quickdoc.JccNonTerminalDocMaker.BnfSectionName
import com.github.oowekyala.ijcc.lang.psi.JccScopedExpansionUnit
import com.github.oowekyala.ijcc.lang.psi.nodeIdentifier

/**
 * @author Clément Fournier
 * @since 1.0
 */
object JjtNodeDocMaker {

    fun makeDoc(scopedUnit: JccScopedExpansionUnit): String? =
            scopedUnit.nodeIdentifier?.let { id ->
                buildQuickDoc {
                    buildDefinition {
                        append("#${id.name}") // use the unprefixed name
                    }
                    sections {
                        buildSection(BnfSectionName, sectionDelim = " ::=") {
                            scopedUnit.expansionUnit.let {
                                JccNonTerminalDocMaker.ExpansionMinifierVisitor(this).startOn(it)
                            }
                        }
                        jjtreeSection(scopedUnit)

                    }
                }
            }
}