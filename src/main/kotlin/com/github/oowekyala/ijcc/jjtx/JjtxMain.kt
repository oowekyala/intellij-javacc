package com.github.oowekyala.ijcc.jjtx

import com.github.oowekyala.ijcc.JavaccParserDefinition
import com.github.oowekyala.ijcc.jjtx.typeHierarchy.TreeLikeWitness
import com.github.oowekyala.ijcc.jjtx.typeHierarchy.TypeHierarchyTree
import com.github.oowekyala.ijcc.lang.model.GrammarNature
import com.github.oowekyala.ijcc.lang.psi.JccFile
import com.github.oowekyala.ijcc.lang.psi.allJjtreeDecls
import com.github.oowekyala.ijcc.lang.psi.impl.JccFileImpl
import kotlin.system.exitProcess

/**
 * @author Clément Fournier
 */
object JjtxMain {


    @JvmStatic
    fun main(vararg args: String) {

        val config = JjtxParams.parse(*args) ?: exitProcess(-1)


        val grammarFile = config.mainGrammarFile ?: run {
            println("Grammar file not found: ${config.mainGrammarFile}")
            exitProcess(-1)
        }

        val jccFile = JjtxLightPsi.parseFile(grammarFile, JavaccParserDefinition) as? JccFile ?: run {
            println("Couldn't read grammar file")
            exitProcess(-1)
        }

        (jccFile as JccFileImpl).grammarNature = GrammarNature.JJTREE

        val ctx = JjtxRunContext(config, jccFile)

        val jjtDecls = jccFile.allJjtreeDecls
        val typeHierarchy =
            ctx.jjtxOptsModel.typeHierarchy?.let {
                TypeHierarchyTree.buildFully(it, jjtDecls, ctx)
            }


        if (typeHierarchy == null) {
            println("Couldn't read type hierarchy...")
            exitProcess(-1)
        }

        println(SimpleTreePrinter(TreeLikeWitness).dumpSubtree(typeHierarchy))
    }
}
