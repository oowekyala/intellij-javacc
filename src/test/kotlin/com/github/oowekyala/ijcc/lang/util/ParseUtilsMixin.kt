package com.github.oowekyala.ijcc.lang.util

import com.github.oowekyala.ijcc.lang.psi.*
import com.github.oowekyala.ijcc.lang.psi.impl.jccEltFactory
import com.intellij.openapi.application.runWriteAction
//import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import org.intellij.lang.annotations.Language

/**
 * @author Clément Fournier
 * @since 1.0
 */
interface ParseUtilsMixin {
    fun getProject(): Project

    @Language("JavaCC")
    fun String.inExpansionCtx(): String = asExpansion().containingFile.text

    fun String.inExpansionCtx(vararg otherProdNames: String): String = inExpansionCtx().let {
        it + "\n" + otherProdNames.joinToString(separator = "\n") { "void $it():{} { \"f\" }" }
    }

    fun String.inExpansionCtx(vararg otherProdNamesAndExps: Pair<String, String>): String = inExpansionCtx().let {
        it + "\n" + otherProdNamesAndExps.joinToString(separator = "\n") { "void ${it.first}():{} { ${it.second} }" }
    }

    fun String.asExpansion(vararg otherProdNamesAndExps: Pair<String, String>): JccExpansion = inExpansionCtx().let {
        it + "\n" + otherProdNamesAndExps.joinToString(separator = "\n") { "void ${it.first}():{} { ${it.second} }" }
    }.asJccFile()
        .nonTerminalProductions
        .first()
        .let { it as JccBnfProduction }
        .expansion!!

    @Language("JavaCC")
    fun String.inGrammarCtx(): String =
        """
                $DummyHeader

                $this
            """

    fun String.asExpansion(): JccExpansion = getProject().jccEltFactory.createExpansion(this)

    fun String.asProduction(): JccProductionLike =
        asJccGrammar().grammarFileRoot!!.childrenSequence().filterIsInstance<JccProductionLike>().first()

    fun String.asJccFile(): JccFile = getProject().jccEltFactory.createFile(this)

    fun String.asJccGrammar(): JccFile =
        getProject().jccEltFactory.createFile("${JccTestBase.DummyHeader}$this")


    fun replaceCaretMarker(text: String) = text.replace("/*caret*/", "<caret>")

    /**
     * Selects the caret with the given [id] from [this] grammar.
     * This allows reusing the same grammar for tests that depend
     * on caret position. Just write several caret markers like `/*caret[someId]*/`
     * in the grammar, then call this method to keep only the caret
     * marker with the given id and replace it with `<caret>`.
     */
    fun String.selectCaretMarker(id: String): String =
        replace("/*caret[$id]*/", "<caret>")
            .also {
                check(this != it) {
                    "No caret with id [$id] found"
                }
            }
            .replace(Regex("/\\*caret.*?\\*/"), "")


    // got from intellij-kotlin,
    fun Document.extractMarkerOffset(project: Project, caretMarker: String = "<caret>"): Int =
        extractMultipleMarkerOffsets(project, caretMarker).singleOrNull() ?: -1

    private fun Document.extractMultipleMarkerOffsets(project: Project, caretMarker: String = "<caret>"): List<Int> {
        val offsets = ArrayList<Int>()

        runWriteAction {
            val text = StringBuilder(text)
            while (true) {
                val offset = text.indexOf(caretMarker)
                if (offset >= 0) {
                    text.delete(offset, offset + caretMarker.length)
                    setText(text.toString())

                    offsets += offset
                } else {
                    break
                }
            }
        }

        PsiDocumentManager.getInstance(project).commitAllDocuments()
        PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(this)

        return offsets
    }


    companion object {
        @Language("JavaCC")
        const val DummyHeader = """
                PARSER_BEGIN(dummy)

                public class dummy {}

                PARSER_END(dummy)
                """
    }

}
