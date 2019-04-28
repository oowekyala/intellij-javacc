package com.github.oowekyala.ijcc.lang.psi

import com.github.oowekyala.ijcc.lang.psi.stubs.indices.JccParserQnameIndexer
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.*
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.FileBasedIndex

/*
    Extensions to link JavaCC to and from Java project classes.
 */



fun getJavaClassFromQname(context: JccFile, fqcn: String): PsiClass? =
    JavaPsiFacade.getInstance(context.project).findClass(fqcn, context.grammarSearchScope)


/**
 * Scope in which a grammar file will be searched for a matching
 * JJTree declaration. This is also used to search for java files
 * from the grammar file.
 */
val PsiFile.grammarSearchScope: GlobalSearchScope
    get() = GlobalSearchScope.projectScope(project)


val JccFile.parserFile: PsiClass?
    get() = getJavaClassFromQname(this, grammarOptions.parserQualifiedName)


val JccNonTerminalProduction.parserMethod: PsiMethod?
    get() =
        containingFile.parserFile
            ?.findMethodsByName(name, false)
            ?.firstOrNull()

/**
 * If this is the generated parser class of a known grammar,
 * returns the grammar file.
 */
val PsiClass.grammarForParserClass: JccFile?
    get() = takeUnless { InjectedLanguageManager.getInstance(project).isInjectedFragment(containingFile) }
        ?.qualifiedName
        ?.let { qname ->
            var f: VirtualFile? = null
            FileBasedIndex.getInstance().getFilesWithKey(
                JccParserQnameIndexer.NAME, setOf(qname), {
                    f = it
                    true
                },
                GlobalSearchScope.allScope(project)
            )
            f
        }
        ?.let { vf ->
            PsiManager.getInstance(project).findFile(vf)  as? JccFile
        }
        // filter out the injected compilation unit in PARSER_BEGIN
        ?.takeUnless { it == InjectedLanguageManager.getInstance(project).getTopLevelFile(this) }

