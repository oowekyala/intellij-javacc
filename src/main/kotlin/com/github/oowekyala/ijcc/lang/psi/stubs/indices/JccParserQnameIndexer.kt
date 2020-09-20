package com.github.oowekyala.ijcc.lang.psi.stubs.indices

import com.github.oowekyala.ijcc.JavaccFileType
import com.github.oowekyala.ijcc.JjtreeFileType
import com.github.oowekyala.ijcc.lang.model.parserQualifiedName
import com.github.oowekyala.ijcc.lang.psi.JccFile
import com.intellij.util.indexing.*
import com.intellij.util.io.EnumeratorStringDescriptor
import com.intellij.util.io.KeyDescriptor


/**
 * Indexes the qualified name of the parser class of a grammar file,
 * in order to provide links from the generated parser to the grammar.
 *
 * @author Clément Fournier
 * @since 1.2
 */
object JccParserQnameIndexer : ScalarIndexExtension<String>() {
    val NAME = ID.create<String, Void>("jccParserQname")

    override fun getName(): ID<String, Void> = NAME

    override fun getVersion(): Int = 1

    override fun dependsOnFileContent(): Boolean = true // FIXME?

    override fun getIndexer(): DataIndexer<String, Void, FileContent> = MyDataIndexer

    override fun getInputFilter(): FileBasedIndex.InputFilter =
        DefaultFileTypeSpecificInputFilter(JavaccFileType, JjtreeFileType)

    override fun getKeyDescriptor(): KeyDescriptor<String> = EnumeratorStringDescriptor.INSTANCE


    private object MyDataIndexer : DataIndexer<String, Void, FileContent> {
        override fun map(inputData: FileContent): Map<String, Void?> {

            val file = inputData.psiFile as? JccFile ?: return emptyMap()

            return mapOf(file.grammarOptions.parserQualifiedName to null)
        }
    }

}
