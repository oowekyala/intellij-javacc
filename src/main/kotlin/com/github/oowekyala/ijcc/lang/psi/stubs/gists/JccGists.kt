package com.github.oowekyala.ijcc.lang.psi.stubs.gists

import com.github.oowekyala.ijcc.lang.model.GrammarOptions
import com.github.oowekyala.ijcc.lang.model.LexicalGrammar
import com.github.oowekyala.ijcc.lang.model.SyntaxGrammar
import com.github.oowekyala.ijcc.lang.psi.JccFile
import com.intellij.openapi.project.Project
import com.intellij.psi.util.CachedValueProvider
import com.intellij.util.CachedValueBase
import com.intellij.util.gist.GistManager
import com.intellij.util.gist.PsiFileGist
import com.intellij.util.io.DataExternalizer
import java.io.DataInput
import java.io.DataOutput
import java.lang.ref.WeakReference

/**
 * @author Clément Fournier
 * @since 1.2
 */
object JccGists {


    private val LexicalGrammarGist: PsiFileGist<LexicalGrammar?> =
        GistManager.getInstance().newPsiFileGist("jcc.LexicalGrammar", 1, InMemoryExternalizer()) { file ->
            when (file) {
                is JccFile -> LexicalGrammar(file)
                else       -> null
            }
        }

    private val SyntaxGrammarGist: PsiFileGist<SyntaxGrammar?> =
        GistManager.getInstance().newPsiFileGist("jcc.SyntaxGrammar", 1, InMemoryExternalizer()) { file ->
            when (file) {
                is JccFile -> SyntaxGrammar(file)
                else       -> null
            }
        }

    private val GrammarOptionsGist: PsiFileGist<GrammarOptions?> =
        GistManager.getInstance().newPsiFileGist("jcc.GrammarOptions", 1, InMemoryExternalizer()) { file ->
            when (file) {
                is JccFile -> GrammarOptions(file)
                else       -> null
            }
        }

    fun getLexicalGrammar(jccFile: JccFile): LexicalGrammar =
        LexicalGrammarGist.getFileData(jccFile)
            ?: LexicalGrammar(jccFile)

    fun getSyntaxGrammar(jccFile: JccFile): SyntaxGrammar =
        SyntaxGrammarGist.getFileData(jccFile)
            ?: SyntaxGrammar(jccFile)

    fun getGrammarOptions(jccFile: JccFile): GrammarOptions =
        GrammarOptionsGist.getFileData(jccFile)
            ?: GrammarOptions(jccFile)


    private fun <T> nullExternaliser(): DataExternalizer<T> =
        @Suppress("UNCHECKED_CAST")
        (NullDataExternaliser as DataExternalizer<T>)


    private object NullDataExternaliser : DataExternalizer<Any> {
        override fun save(out: DataOutput, value: Any?) {
        }

        override fun read(`in`: DataInput): Any? = null
    }


    /**
     * Caches something in memory if it implements hashcode.
     */
    private class InMemoryExternalizer<T> : DataExternalizer<T> {

        // TODO periodically remove dead references from the map
        private val myTs = mutableMapOf<Int, WeakReference<T?>>()


        override fun save(out: DataOutput, value: T) {
            // VirtualFileGist ensures no null value is passed here,
            // it adds a boolean attribute to the serialized output
            out.writeInt(value.hashCode())
            myTs[value.hashCode()] = WeakReference(value)
        }

        override fun read(`in`: DataInput): T? =
            `in`.readInt().let { myTs.getValue(it).get() }

    }

}