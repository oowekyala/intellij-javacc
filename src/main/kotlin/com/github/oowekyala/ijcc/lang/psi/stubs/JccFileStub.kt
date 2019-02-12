package com.github.oowekyala.ijcc.lang.psi.stubs

import com.github.oowekyala.ijcc.JavaccLanguage
import com.github.oowekyala.ijcc.lang.psi.JccFile
import com.intellij.psi.stubs.PsiFileStub
import com.intellij.psi.stubs.PsiFileStubImpl
import com.intellij.psi.tree.IStubFileElementType

/**
 * @author Clément Fournier
 * @since 1.2
 */
object JccFileStubElementType : IStubFileElementType<JccFileStub>("JCC_FILE", JavaccLanguage)

interface JccFileStub : PsiFileStub<JccFile>

class JccFileStubImpl(file: JccFile) : PsiFileStubImpl<JccFile>(file), JccFileStub