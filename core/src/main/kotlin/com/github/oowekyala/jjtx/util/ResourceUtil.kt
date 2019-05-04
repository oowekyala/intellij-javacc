package com.github.oowekyala.jjtx.util

import org.apache.commons.io.FilenameUtils
import java.io.File
import java.io.IOException
import java.net.MalformedURLException
import java.net.URI
import java.net.URISyntaxException
import java.net.URL
import java.nio.file.*
import java.util.stream.Collectors


private val FILE_SYSTEM_LOCK = Any()


/** Finds the classes in the given package by looking in the classpath directories.  */
fun getClassesInPackage(packageName: String,
                        classLoader: ClassLoader = Thread.currentThread().contextClassLoader): Sequence<Class<*>> {
    return pathsInResource(classLoader, packageName.replace('.', '/'))
        .map { toClass(it, packageName) }
        .filterNotNull()
}

/** Finds the nested, local, and anon classes of this class.
 */
fun Class<*>.getDependentClasses(): Sequence<Class<*>> {
    val pname = `package`.name
    return pathsInResource(classLoader, pname)
        .map {
            toClass(it, pname) {
                it.matches("$simpleName$.*")
            }
        }
        .filterNotNull()
}

private fun pathsInResource(classLoader: ClassLoader,
                            resourcePath: String): Sequence<Path> {
    var resources: Sequence<URL>

    try {
        resources = classLoader.getResources(resourcePath).iterator().asSequence()
    } catch (e: IOException) {
        return emptySequence()
    }

    if (resourcePath.isEmpty()) {
        resources = resources.flatMap { url ->
            if (url.toString().matches(".*META-INF/versions/\\d+/?")) {
                try {
                    return@flatMap sequenceOf(url, URL(url, "../../.."))
                } catch (ignored: MalformedURLException) {

                }

            }
            sequenceOf(url)
        }
    }

    return resources.distinct().flatMap { resource ->
        try {
            getPathsInDir(resource, 1).asSequence()
        } catch (e: Exception) {
            e.printStackTrace()
            emptySequence<Path>()
        }
    }
}


/** Maps paths to classes.  */
private fun toClass(path: Path?, packageName: String, filter: (String) -> Boolean = { true }): Class<*>? {
    return path
        ?.takeIf { p -> "class".equals(FilenameUtils.getExtension(path.toString()), ignoreCase = true) }
        ?.takeIf { p -> filter(p.fileName.toFile().nameWithoutExtension) }
        ?.let { p ->
            try {
                Class.forName(packageName + "." + FilenameUtils.getBaseName(path.fileName.toString()))
            } catch (e: ClassNotFoundException) {
                e.printStackTrace()
                null
            }
        }
}

private fun getJarRelativePath(uri: URI): String {
    return if ("jar" == uri.scheme) {
        // we have to cut out the path to the jar + '!'
        // to get a path that's relative to the root of the jar filesystem
        // This is equivalent to a packageName.replace('.', '/') but more reusable
        val schemeSpecific = uri.schemeSpecificPart
        schemeSpecific.substring(schemeSpecific.indexOf('!') + 1)
    } else {
        uri.schemeSpecificPart
    }
}


@Throws(URISyntaxException::class, IOException::class)
private fun getPathsInDir(url: URL, maxDepth: Int): List<Path> {
    var depth = maxDepth

    val uri = url.toURI().normalize()

    return if ("jar" == uri.scheme) {
        // we have to do this to look inside a jar
        getFileSystem(uri).use { fs ->
            var path = fs.getPath(getJarRelativePath(uri))
            while (depth < 0) {
                path = path.resolve("..")
                depth++
            }

            Files.walk(path, depth)
                .collect(Collectors.toList()) // buffer everything, before closing the filesystem


        }
    } else {
        var path = url.toPath()
        while (depth < 0) {
            path = path.resolve("..")
            depth++
        }
        Files.walk(path, depth).use { paths ->
            paths.collect(Collectors.toList()) // buffer everything, before closing the original stream
        }
    }
}

private fun URL.toPath(): Path = File(file).toPath()


@Throws(IOException::class)
fun getFileSystem(uri: URI): FileSystem {

    synchronized(FILE_SYSTEM_LOCK) {
        return try {
            FileSystems.getFileSystem(uri)
        } catch (e: FileSystemNotFoundException) {
            FileSystems.newFileSystem(uri, emptyMap<String, Any?>())
        }
    }
}
