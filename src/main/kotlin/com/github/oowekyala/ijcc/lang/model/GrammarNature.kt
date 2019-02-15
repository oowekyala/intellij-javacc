package com.github.oowekyala.ijcc.lang.model

/**
 * @author Clément Fournier
 * @since 1.2
 */
@Suppress("MemberVisibilityCanBePrivate")
enum class GrammarNature(val conventionalExtension: String) {
    JAVACC("jj"),
    JJTREE("jjt");


    val dotAndExtension = ".$conventionalExtension"
}