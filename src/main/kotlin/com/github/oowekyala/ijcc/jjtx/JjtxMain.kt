package com.github.oowekyala.ijcc.jjtx

import com.github.oowekyala.ijcc.JavaccParserDefinition
import com.github.oowekyala.ijcc.jjtx.typeHierarchy.TreeLikeWitness
import com.github.oowekyala.ijcc.lang.model.GrammarNature
import com.github.oowekyala.ijcc.lang.model.presentValue
import com.github.oowekyala.ijcc.lang.psi.JccFile
import com.github.oowekyala.ijcc.lang.psi.impl.JccFileImpl
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.tylerthrailkill.helpers.prettyprint.pp
import org.yaml.snakeyaml.Yaml
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

        val yaml: Any = Yaml().load(config.jjtxConfigFile?.bufferedReader())

        yaml.pp()

        GsonBuilder().setPrettyPrinting().create().toJson(yaml).pp()

        val typeHierarchy = ctx.jjtxOptsModel.typeHierarchy.process(ctx)

        println(SimpleTreePrinter(TreeLikeWitness).dumpSubtree(typeHierarchy))
    }
}
