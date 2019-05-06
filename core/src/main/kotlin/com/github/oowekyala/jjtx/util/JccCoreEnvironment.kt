package com.github.oowekyala.jjtx.util

import com.github.oowekyala.ijcc.JavaccFileType
import com.github.oowekyala.ijcc.JavaccParserDefinition
import com.github.oowekyala.ijcc.JjtreeFileType
import com.intellij.core.CoreApplicationEnvironment
import com.intellij.core.CoreProjectEnvironment
import com.intellij.openapi.Disposable
import com.intellij.openapi.extensions.Extensions
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vfs.VirtualFile
import java.nio.file.Path

/**
 * Used for CLI execution.
 *
 * @author Clément Fournier
 */
class JccCoreEnvironment private constructor(
    parentDisposable: Disposable,
    private val applicationEnvironment: CoreApplicationEnvironment
) : CoreProjectEnvironment(parentDisposable, applicationEnvironment) {


    fun localVirtualFile(path: Path): VirtualFile? =
        applicationEnvironment.localFileSystem.findFileByIoFile(path.toFile())


    companion object {

        private val APPLICATION_LOCK = Object()
        private var ourApplicationEnvironment: CoreApplicationEnvironment? = null

        private fun createRootEnv(parentDisposable: Disposable): JccCoreEnvironment =
            JccCoreEnvironment(parentDisposable, getOrCreateAppCoreEnv())


        fun withEnvironment(action: JccCoreEnvironment.() -> Unit) {
            val disposable = Disposer.newDisposable()
            val env = createRootEnv(disposable)

            action(env)

            disposable.dispose()
        }

        private fun getOrCreateAppCoreEnv(): CoreApplicationEnvironment {

            synchronized(APPLICATION_LOCK) {
                if (ourApplicationEnvironment == null) {
                    val disposable = Disposer.newDisposable()
                    ourApplicationEnvironment =
                        createApplicationEnvironment(disposable)
                    Disposer.register(disposable, Disposable {
                        synchronized(APPLICATION_LOCK) {
                            ourApplicationEnvironment = null
                        }
                    })
                }

                return ourApplicationEnvironment!!
            }
        }

        private fun createApplicationEnvironment(disposable: Disposable): CoreApplicationEnvironment {
            Extensions.cleanRootArea(disposable)
            val appEnv = CoreApplicationEnvironment(disposable)
            appEnv.registerApplicationServices()
            return appEnv
        }

        private fun CoreApplicationEnvironment.registerApplicationServices() {
            registerFileType(JavaccFileType, "jj")
            registerFileType(JjtreeFileType, "jjt")
            registerParserDefinition(JavaccParserDefinition)
        }
    }
}

