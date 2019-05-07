package com.github.oowekyala.jjtx.util

import com.github.oowekyala.ijcc.JavaccFileType
import com.github.oowekyala.ijcc.JavaccParserDefinition
import com.github.oowekyala.ijcc.JjtreeFileType
import com.github.oowekyala.ijcc.lang.psi.impl.GrammarOptionsService
import com.github.oowekyala.jjtx.ide.JjtxOptionsService
import com.intellij.core.CoreApplicationEnvironment
import com.intellij.core.CoreProjectEnvironment
import com.intellij.openapi.Disposable
import com.intellij.openapi.extensions.Extensions
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vfs.VirtualFile
import java.nio.file.Path

/**
 * IntelliJ environment that provides a project and other facilities
 * of the intellij platform when executing from CLI.
 *
 * @author Clément Fournier
 */
class JjtxCoreEnvironment private constructor(
    parentDisposable: Disposable,
    val applicationEnvironment: CoreApplicationEnvironment
) : CoreProjectEnvironment(parentDisposable, applicationEnvironment) {


    fun localVirtualFile(path: Path): VirtualFile? =
        applicationEnvironment.localFileSystem.findFileByIoFile(path.toFile())


    companion object {

        private val APPLICATION_LOCK = Object()
        private var ourApplicationEnvironment: CoreApplicationEnvironment? = null


        private fun createRootEnv(parentDisposable: Disposable): JjtxCoreEnvironment =
            JjtxCoreEnvironment(parentDisposable, getOrCreateAppCoreEnv())

        fun createTestEnvironment(): JjtxCoreEnvironment {
            val disposable = Disposer.newDisposable()
            return createRootEnv(disposable).also {
                it.registerProjectComponent(GrammarOptionsService::class.java, JjtxOptionsService())
            }
        }

        fun withEnvironment(action: JjtxCoreEnvironment.() -> Unit) {
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
            // registerApplicationService(GrammarOptionsService::class.java, JjtxOptionsService())
        }
    }
}

