package com.github.oowekyala.ijcc.lang.model

/**
 * Type of preprocessor used by a grammar. JJTree is a superset
 * of JavaCC so we can implement both with the exact same PSI.
 * The [nature][com.github.oowekyala.ijcc.lang.psi.grammarNature] of
 * a file is determined by file extension.
 *
 * Grammar natures are comparable, their ordering is determined
 * by inclusion relation of the languages.
 *
 * @author Clément Fournier
 * @since 1.2
 */
@Suppress("MemberVisibilityCanBePrivate")
enum class GrammarNature(val conventionalExtension: String) {
    JAVACC("jj"),
    JJTREE("jjt");


    val dotAndExtension = ".$conventionalExtension"
}