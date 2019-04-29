package com.github.oowekyala.ijcc.jjtx

import com.github.oowekyala.ijcc.JavaccParserDefinition
import com.github.oowekyala.ijcc.lang.psi.JccFile
import com.github.oowekyala.ijcc.lang.psi.allJjtreeDecls
import org.intellij.grammar.LightPsi
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

        val jccFile = LightPsi.parseFile(grammarFile, JavaccParserDefinition) as? JccFile ?: run {
            println("Couldn't read grammar file")
            exitProcess(-1)
        }

        val ctx = JjtxRunContext(config, jccFile)

        val names = jccFile.allJjtreeDecls.keys
        val typeHierarchy =
            ctx.jjtxOptsModel.typeHierarchy?.let {
                TypeHierarchyTree.fromJsonRoot(it, ctx)
            }?.let {
                it.expandRegex(names, ctx)[0]
            }

        if (typeHierarchy == null) {
            println("Couldn't read type hierarchy...")
            exitProcess(-1)
        }

        println(SimpleTreePrinter(TreeLikeWitness).dumpSubtree(typeHierarchy))
    }
}
