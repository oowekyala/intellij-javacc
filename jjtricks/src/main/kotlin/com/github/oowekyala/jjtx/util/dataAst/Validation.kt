package com.github.oowekyala.jjtx.util.dataAst

import com.github.oowekyala.jjtx.Jjtricks
import com.github.oowekyala.jjtx.JjtxContext
import com.github.oowekyala.jjtx.reporting.reportFatal
import com.github.oowekyala.jjtx.reporting.reportNonFatal
import com.github.oowekyala.jjtx.util.JsonPosition
import org.everit.json.schema.ValidationException
import org.everit.json.schema.loader.SchemaLoader
import org.json.JSONObject
import org.json.JSONTokener


fun DataAstNode.validateJjtopts(ctx: JjtxContext): Int =
    validateJjtopts {
        causingExceptions.forEach { ex ->
            val pos = findPointer(ex.pointerToViolation)?.position
            ctx.messageCollector.reportNonFatal(ex.sanitizedMessage(this), pos)
        }
        ctx.messageCollector.reportFatal(message!!)
    }

private fun ValidationException.sanitizedMessage(parent: ValidationException): String {

    if (parent.isAt("jjtx", "typeHierarchy")) {
        Regex("(maximum|minimum) size: \\[1], found: \\[(\\d+)]").matchEntire(errorMessage.trim())?.let {
            return "Expected a unique name, found ${it.groupValues[2]}"
        }
    }

    return errorMessage.replace("JSONObject", "Map")
}


private fun ValidationException.isAt(vararg path: String) =
    jsonPointerPosition(pointerToViolation).path == path.toList()


fun AstMap.findJsonPointer(pointer: String): DataAstNode? =
    jsonPointerPosition(pointer) findIn this


fun DataAstNode.validateJjtopts(onErrors: ValidationException.() -> Unit): Int =
    Jjtricks.getResourceAsStream("/jjtx/schema/jjtopts.schema.json")!!.newInputStream().use { inputStream ->
        val rawSchema = JSONObject(JSONTokener(inputStream))
        val schema = SchemaLoader.load(rawSchema)
        val doc = this.toJson().toString()
        try {
            schema.validate(JSONObject(doc))
        } catch (e: ValidationException) {
            e.onErrors()
            e.violationCount
        }
        0
    }

fun jsonPointerPosition(pointer: String): JsonPosition =
    JsonPosition(pointer.removePrefix("#/").split('/'))
