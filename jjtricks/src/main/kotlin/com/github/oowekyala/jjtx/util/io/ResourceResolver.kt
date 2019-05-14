package com.github.oowekyala.jjtx.util.io

import com.github.oowekyala.jjtx.Jjtricks
import com.github.oowekyala.jjtx.util.isFile
import java.nio.file.Path

/**
 * Resolves resource paths.
 *
 * @author Clément Fournier
 */
interface ResourceResolver<T> {

    fun getResource(path: String): T?

}


data class DefaultResourceResolver(val ctxDir: Path) : ResourceResolver<NamedInputStream> {


    override fun getResource(path: String): NamedInputStream? =
        fromClasspathResource(path) ?: fromFile(path)

    private fun fromClasspathResource(path: String): NamedInputStream? =
        Jjtricks::class.java.getResourceAsStream(expandResourcePath(path))?.use { _ ->
            NamedInputStream({
                // Repeat, so as not to capture the open input stream
                Jjtricks::class.java.getResourceAsStream(
                    expandResourcePath(path)
                )
            }, path, path)
        }


    private fun fromFile(path: String): NamedInputStream? =
        ctxDir.resolve(path).takeIf { it.isFile() }?.namedInputStream()

    private fun expandResourcePath(path: String): String {
        return when {
            path.startsWith("/jjtx") -> path.replaceFirst("/jjtx", "/com/github/oowekyala/jjtx")
            else                     -> path
        }
    }


}
