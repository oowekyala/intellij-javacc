package com.github.oowekyala.ijcc.lang.refs

import com.intellij.lang.refactoring.JavaNamesValidator
import com.intellij.openapi.project.Project

/**
 * Validates names for the rename refactoring.
 *
 * @author Clément Fournier
 * @since 1.0
 */
object JccNamesValidator : JavaNamesValidator() {
    private val jccKeywords = listOf(
        "LOOKAHEAD",
        "IGNORE_CASE",
        "PARSER_BEGIN",
        "PARSER_END",
        "JAVACODE",
        "TOKEN",
        "SPECIAL_TOKEN",
        "MORE",
        "SKIP",
        "TOKEN_MGR_DECLS",
        "EOF"
    )

    override fun isKeyword(name: String, project: Project?): Boolean =
            jccKeywords.contains(name) || super.isKeyword(name, project)

    override fun isIdentifier(name: String, project: Project?): Boolean =
            !jccKeywords.contains(name) && super.isIdentifier(name, project)

}