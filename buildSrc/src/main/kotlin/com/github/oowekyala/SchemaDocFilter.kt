package com.github.oowekyala

import org.gradle.api.Project
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import java.io.File


fun Project.genDocOutput(schemaPath: String, outputDir: Int) {


}

fun List<Element>.toElts(): Elements = Elements(this)

// SAMPLE INPUT
/*
 <dt>

    <span class="json-property-type">object[]</span>
    <span class="json-property-range" title="Value limits"></span>

</dt>
<dd>

    <section class="json-schema-array-items">
        <span class="json-property-type"><a class="json-schema-ref" href="#/definitions/nodeGenTask">nodeGenTask</a></span>
        <span class="json-property-range" title="Value limits"></span>
        <div class="json-inner-schema"></div>
    </section>            </dd>
<dt>
 */


fun Project.filterSchemaDoc(inDir: File, outDir: File) {

    val doc = Jsoup.parse(inDir.resolve("index.html"), Charsets.UTF_8.name())

    inDir.copyRecursively(outDir, overwrite = true)

    val arr = doc.select(".json-schema-array-items")!!

    for (items in arr) {

        val ref = items.selectFirst(".json-property-type .json-schema-ref")

        if (ref != null)
            items.parent().previousElementSibling()!!.selectFirst(".json-property-type").let { type ->
                Regex("object(\\[])+").matchEntire(type.text().trim())?.let { match ->
                    type.html(ref.outerHtml() + match.groupValues[1])
                }
            }

        // TODO maybe there are some other constraints
        // but not in the current schema
        items.remove()

    }

    outDir.resolve("index.html").bufferedWriter().use {
        doc.html(it)
    }
}
