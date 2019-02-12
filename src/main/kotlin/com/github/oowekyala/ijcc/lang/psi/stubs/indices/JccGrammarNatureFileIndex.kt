package com.github.oowekyala.ijcc.lang.psi.stubs.indices

import com.github.oowekyala.ijcc.JavaccFileType
import com.github.oowekyala.ijcc.lang.psi.JccFile
import com.intellij.util.indexing.*
import com.intellij.util.io.EnumeratorStringDescriptor
import com.intellij.util.io.KeyDescriptor


/**
 * @author Clément Fournier
 * @since 1.2
 */
object JccGrammarNatureFileIndex : ScalarIndexExtension<String>() {
    val NAME = ID.create<String, Void>("jccGrammarNature")

    override fun getName(): ID<String, Void> = NAME

    override fun getVersion(): Int = 1

    override fun dependsOnFileContent(): Boolean = true // FIXME?

    override fun getIndexer(): DataIndexer<String, Void, FileContent> = MyDataIndexer

    override fun getInputFilter(): FileBasedIndex.InputFilter = DefaultFileTypeSpecificInputFilter(JavaccFileType)

    override fun getKeyDescriptor(): KeyDescriptor<String> = EnumeratorStringDescriptor.INSTANCE


    private object MyDataIndexer : DataIndexer<String, Void, FileContent> {
        override fun map(inputData: FileContent): Map<String, Void?> {

            val file = inputData.psiFile as? JccFile ?: return emptyMap()

            return mapOf(file.grammarNature.name to null)
        }
    }

}