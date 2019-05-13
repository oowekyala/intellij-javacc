package com.github.oowekyala.jjtx.util.io

import com.github.oowekyala.jjtx.Jjtricks
import com.github.oowekyala.jjtx.util.inputStream
import com.github.oowekyala.jjtx.util.isFile
import java.nio.file.Path

/**
 * Resolves resource paths.
 *
 * @author Clément Fournier
 */
interface ResourceResolver {

    fun getStreamable(path: String): NamedInputStream?

}

data class DefaultResourceResolver(val ctxDir: Path) : ResourceResolver {


    override fun getStreamable(path: String): NamedInputStream? =
        fromClasspathResource(path) ?: fromFile(path)

    private fun fromClasspathResource(path: String): NamedInputStream? =
        Jjtricks::class.java.getResourceAsStream(expandResourcePath(path))
            ?.let {
                NamedInputStream({
                    Jjtricks::class.java.getResourceAsStream(
                        expandResourcePath(path)
                    )
                }, path)
            }


    private fun fromFile(path: String): NamedInputStream? =
        ctxDir.resolve(path).takeIf { it.isFile() }?.let {
            NamedInputStream({ it.inputStream() }, path)
        }

    private fun expandResourcePath(path: String): String {
        return when {
            path.startsWith("/jjtx") -> path.replaceFirst("/jjtx", "/com/github/oowekyala/jjtx")
            else                     -> path
        }
    }


}
