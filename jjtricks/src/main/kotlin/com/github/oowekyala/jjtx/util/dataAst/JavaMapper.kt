package com.github.oowekyala.jjtx.util.dataAst

import com.github.oowekyala.jjtx.OptsModelImpl
import com.github.oowekyala.jjtx.typeHierarchy.TypeHierarchyTree
import com.github.oowekyala.jjtx.util.dataAst.ScalarType.*
import java.lang.reflect.Method
import java.util.*
import org.yaml.snakeyaml.nodes.Node as YamlNode

internal fun OptsModelImpl.toYaml(): String = toDataNode().toYamlString()

internal fun OptsModelImpl.toDataNode(): DataAstNode {

    val visitors = visitorBeans.toDataNode()

    val th = resolvedTypeHierarchy.toDataNode()

    return AstMap(
        mapOf(
            "jjtx" to AstMap(
                mapOf(
                    "nodePrefix" to nodePrefix.toDataNode(),
                    "nodePackage" to nodePackage.toDataNode(),
                    "visitors" to visitors,
                    "templateContext" to templateContext.toDataNode(),
                    "typeHierarchy" to th
                )
            )
        )
    )
}


internal fun TypeHierarchyTree.toDataNode(): DataAstNode =
    if (children.isEmpty())
        AstScalar(nodeName, STRING)
    else
        AstMap(
            mapOf(
                nodeName to AstSeq(
                    children.map { it.toDataNode() })
            )
        )


internal fun Any?.toDataNode(): DataAstNode {
    return when (this) {
        null             -> AstScalar("null", NULL)
        is String        -> AstScalar(this, STRING)
        is Number        -> AstScalar(this.toString(), NUMBER)
        is Boolean       -> AstScalar(this.toString(), BOOLEAN)
        is Collection<*> -> AstSeq(this.map { it.toDataNode() })
        is Map<*, *>     ->
            AstMap(
                this.mapNotNull { (k, v) ->
                    v?.let { Pair(k.toDataNode(), v.toDataNode()) }
                }.toMap()
            )
        else             -> {
            AstMap(this.propertiesMap().filterValues { it !is AstScalar || it.type != NULL })
        }
    }
}

private inline fun <reified T : Any> T.propertiesMap(): Map<String, DataAstNode> =
    javaClass.properties.map { Pair(it.name, it.getter(this).toDataNode()) }.toMap()


// avoids endless loop in the case of cycle
private val propertyMapCache = WeakHashMap<Class<*>, List<JProperty<*, *>>>()

private val <T : Any> Class<T>.properties: List<JProperty<T, *>>
    get() = propertyMapCache.computeIfAbsent(this) {
        methods.toList().mapNotNull { JProperty.toPropertyOrNull(it) }
    } as List<JProperty<T, *>>

private class JProperty<in T : Any, out V> private constructor(
    val name: String,
    val getter: (T) -> V?
) {


    companion object {

        private val GetterRegex = Regex("get([A-Z].*)")
        private val BoolGetterRegex = Regex("is[A-Z].*")


        fun toPropertyOrNull(method: Method): JProperty<*, *>? {
            return method.propertyName?.let {
                JProperty<Any, Any?>(it) {
                    try {
                        method.invoke(it)
                    } catch (e: Exception) {
                        null
                    }
                }
            }
        }

        private val Method.propertyName: String?
            get() {

                if (this == java.lang.Object::class.java.getDeclaredMethod("getClass")) return null

                if (parameterCount != 0 || isBridge) return null

                GetterRegex.matchEntire(name)?.groupValues?.get(1)?.let {
                    return it.decapitalize()
                }

                if (returnType == Boolean::class.java || returnType == java.lang.Boolean.TYPE) {
                    BoolGetterRegex.matchEntire(name)?.groupValues?.get(0)?.let {
                        return it.decapitalize()
                    }
                }
                return null
            }

    }
}
