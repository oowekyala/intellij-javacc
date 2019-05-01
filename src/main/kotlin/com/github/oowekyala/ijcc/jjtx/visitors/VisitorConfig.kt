package com.github.oowekyala.ijcc.jjtx.visitors

import com.github.oowekyala.ijcc.jjtx.JjtxRunContext
import com.intellij.util.io.isDirectory
import org.apache.velocity.VelocityContext
import org.apache.velocity.app.VelocityEngine
import org.apache.velocity.exception.ResourceNotFoundException
import java.io.File
import java.net.URL
import java.nio.file.Path

/**
 * @author Clément Fournier
 */
data class VisitorConfig(var template: String,
                         val output: Path,
                         val context: Map<String, Any?>) {


    fun resolveTemplate() {

        if (template[0] == '@') {

            val fname = template.substring(1)

            val url: URL =
                VisitorConfig::class.java.classLoader.getResource(fname) ?: throw ResourceNotFoundException(template)

            template = File(url.toExternalForm()).readText()
        }
    }

    fun execute(ctx: JjtxRunContext) {

        val engine = VelocityEngine()
        val baseCtx = VelocityContext()

        baseCtx["grammar"] = GrammarBean(ctx.jjtxParams.grammarName, ctx.jjtxParams.mainGrammarFile!!)
        baseCtx["nodes"] = NodeBean.dump(ctx.jjtxOptsModel.typeHierarchy, ctx)
        baseCtx["user"] = context

        if (output.isDirectory()) {
            throw IllegalStateException("Output file $output is directory")
        }

        engine.evaluate(baseCtx, output.toFile().bufferedWriter(), "visitor-template", template)

    }

}
