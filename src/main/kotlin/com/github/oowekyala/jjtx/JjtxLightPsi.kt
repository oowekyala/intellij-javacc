package com.github.oowekyala.jjtx

/*
 * Copyright 2011-present Greg Shrago
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.github.oowekyala.jjtx.util.Io
import com.github.oowekyala.jjtx.util.workingDirectory
import com.intellij.concurrency.AsyncFutureFactory
import com.intellij.concurrency.AsyncFutureFactoryImpl
import com.intellij.concurrency.JobLauncher
import com.intellij.concurrency.JobLauncherImpl
import com.intellij.ide.startup.impl.StartupManagerImpl
import com.intellij.lang.*
import com.intellij.lang.impl.PsiBuilderFactoryImpl
import com.intellij.lang.impl.PsiBuilderImpl
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.mock.*
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.extensions.ExtensionPoint
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.extensions.Extensions
import com.intellij.openapi.extensions.ExtensionsArea
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.impl.FileDocumentManagerImpl
import com.intellij.openapi.fileTypes.FileTypeFactory
import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.fileTypes.FileTypeRegistry
import com.intellij.openapi.fileTypes.PlainTextLanguage
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.impl.ProgressManagerImpl
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.DumbServiceImpl
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupManager
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.Getter
import com.intellij.openapi.util.KeyedExtensionCollector
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.encoding.EncodingManager
import com.intellij.openapi.vfs.encoding.EncodingManagerImpl
import com.intellij.psi.*
import com.intellij.psi.impl.PsiCachedValuesFactory
import com.intellij.psi.impl.PsiFileFactoryImpl
import com.intellij.psi.impl.search.CachesBasedRefSearcher
import com.intellij.psi.impl.search.PsiSearchHelperImpl
import com.intellij.psi.impl.source.CharTableImpl
import com.intellij.psi.impl.source.resolve.ResolveCache
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistryImpl
import com.intellij.psi.impl.source.tree.injected.InjectedLanguageManagerImpl
import com.intellij.psi.search.PsiSearchHelper
import com.intellij.psi.util.CachedValuesManager
import com.intellij.testFramework.LightVirtualFile
import com.intellij.util.CachedValuesManagerImpl
import com.intellij.util.io.createFile
import com.intellij.util.io.delete
import com.intellij.util.io.exists
import com.intellij.util.io.isFile
import com.intellij.util.messages.MessageBus
import com.intellij.util.messages.MessageBusFactory
import org.picocontainer.MutablePicoContainer
import org.picocontainer.PicoContainer
import org.picocontainer.PicoInitializationException
import org.picocontainer.PicoIntrospectionException
import org.picocontainer.defaults.AbstractComponentAdapter
import java.io.*
import java.lang.reflect.Modifier
import java.util.jar.JarEntry
import java.util.jar.JarOutputStream

/**
 * @author greg
 * @noinspection UseOfSystemOutOrSystemErr
 */
object JjtxLightPsi {

    private val ourParsing: MyParsing

    init {
        try {
            ourParsing = MyParsing()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }

    }

    fun init() {}

    @Throws(IOException::class)
    fun parseFile(file: File, parserDefinition: ParserDefinition): PsiFile? {
        val name = file.path
        val text = FileUtil.loadFile(file)
        return parseFile(name, text, parserDefinition)
    }

    fun parseFile(name: String, text: String, parserDefinition: ParserDefinition): PsiFile? =
        ourParsing.createFile(name, text, parserDefinition)

    fun parseText(text: String, parserDefinition: ParserDefinition): ASTNode =
        ourParsing.createAST(text, parserDefinition)

    fun parseLight(text: String, parserDefinition: ParserDefinition): SyntaxTraverser<LighterASTNode> =
        ourParsing.parseLight(text, parserDefinition)

    object GenerateClassLog {


        /**
         * Redirect stdout to get the class log, run with -verbose:class.
         */
        @JvmStatic
        fun main(args: Array<String>) {

            val closedOut = PrintStream(object : OutputStream() {
                override fun write(b: Int) {
                    // do nothing
                }
            })

            val io = Io(
                stdout = closedOut,
                exit = { _, _ -> throw Error() }
            )

            Jjtricks.main(io, "Java", "--dump-config", "-q")
        }

    }

    /*
     * Builds light-psi-all.jar from JVM class loader log (-verbose:class option)
     */
    @Throws(Throwable::class)
    @JvmStatic
    fun main(args: Array<String>) {
        if (args.size < 2) {
            println("Usage: Main <output-path> <classes.log.txt>")
            return
        }

        val jar = workingDirectory.resolve(args[0])
        val classesFile = workingDirectory.resolve(args[1])

        assert(classesFile.isFile())
        if (jar.exists()) jar.delete()
        jar.createFile()

        val count = mainImpl(classesFile.toFile(), jar)
        println(StringUtil.formatFileSize(jar.length()) + " and " + count + " classes written to " + jar.name)
    }

    private fun mainImpl(classesFile: File, outJarFile: File): Int {
        val reader = BufferedReader(FileReader(classesFile))
        val jdk8Pattern = Regex("\\[Loaded (.*) from (?:file:)?(.*)]")
        val jdkAbovePattern = Regex("\\[[^]]++]\\[info]\\[class,load] (.*?) source: file:(.+)")

        val jar = JarOutputStream(FileOutputStream(outJarFile))
        addJarEntry(jar, "misc/registry.properties")

        return reader.lineSequence()
            .mapNotNull { jdk8Pattern.matchEntire(it) ?: jdkAbovePattern.matchEntire(it) }
            .filter { shouldAddEntry(it.groupValues[2]) }
            .fold(0) { sum, it ->
                addJarEntry(jar, it.groupValues[1].replace(".", "/") + ".class")
                sum + 1
            }.also {
                jar.close()
                reader.close()
            }
    }

    private fun shouldAddEntry(path: String): Boolean {
        if (!path.startsWith("/")) {
            return false
        }
        return !path.endsWith("/rt.jar") && !path.contains("/intellij-javacc/") && path.contains("idea", ignoreCase = true)
    }

    @Throws(IOException::class)
    private fun addJarEntry(jarFile: JarOutputStream, resourceName: String) {
        val stream = JjtxLightPsi::class.java.classLoader.getResourceAsStream(resourceName)
        if (stream == null) {
            System.err.println("Skipping missing $resourceName")
        } else {
            jarFile.putNextEntry(JarEntry(resourceName))
            FileUtil.copy(stream, jarFile)
            jarFile.closeEntry()
        }
    }

    private class MyParsing internal constructor() : Disposable {

        private val project: MockProject = Init.initAppAndProject(this)

        init {
            Init.initExtensions(project)
        }

        fun createFile(name: String, text: String, definition: ParserDefinition): PsiFile? {
            val language = definition.fileNodeType.language
            Init.addKeyedExtension(LanguageParserDefinitions.INSTANCE, language, definition, project)

            //            val file = LocalFileSystem.getInstance().findFileByIoFile(File(name))
            val virtualFile = PathedLightVirtualFile(name, language, text)
            return (PsiFileFactory.getInstance(project) as PsiFileFactoryImpl).trySetupPsiForFile(
                virtualFile, language, true, false
            )
        }

        class PathedLightVirtualFile(val myPath: String, language: Language, text: String) :
            LightVirtualFile(myPath.substringAfterLast('/'), language, text) {

            // this is used by LightJjtxContext
            override fun getPath(): String = "/$myPath"

        }

        fun createAST(text: String, definition: ParserDefinition): ASTNode {
            val parser = definition.createParser(project)
            val lexer = definition.createLexer(project)
            val psiBuilder = PsiBuilderImpl(project, null, definition, lexer, CharTableImpl(), text, null, null)
            return parser.parse(definition.fileNodeType, psiBuilder)
        }

        fun parseLight(text: String, definition: ParserDefinition): SyntaxTraverser<LighterASTNode> {
            val parser = definition.createParser(project) as LightPsiParser
            val lexer = definition.createLexer(project)
            val psiBuilder = PsiBuilderImpl(project, null, definition, lexer, CharTableImpl(), text, null, null)
            parser.parseLight(definition.fileNodeType, psiBuilder)
            return SyntaxTraverser.lightTraverser(psiBuilder)
        }

        override fun dispose() {}
    }

    private object Init {

        fun initExtensions(project: MockProject) {
            Extensions.getRootArea()
                .registerExtensionPoint("com.intellij.referencesSearch", "com.intellij.util.QueryExecutor")
            Extensions.getRootArea()
                .registerExtensionPoint("com.intellij.useScopeEnlarger", "com.intellij.psi.search.UseScopeEnlarger")
            Extensions.getRootArea()
                .registerExtensionPoint("com.intellij.useScopeOptimizer", "com.intellij.psi.search.UseScopeOptimizer")
            Extensions.getRootArea()
                .registerExtensionPoint("com.intellij.languageInjector", "com.intellij.psi.LanguageInjector")
            Extensions.getArea(project).registerExtensionPoint(
                "com.intellij.multiHostInjector",
                "com.intellij.lang.injection.MultiHostInjector"
            )
            Extensions.getRootArea().registerExtensionPoint(
                "com.intellij.codeInsight.containerProvider",
                "com.intellij.codeInsight.ContainerProvider"
            )
            Extensions.getRootArea().getExtensionPoint<Any>("com.intellij.referencesSearch")
                .registerExtension(CachesBasedRefSearcher())
            registerApplicationService(project, PsiReferenceService::class.java, PsiReferenceServiceImpl::class.java)
            registerApplicationService(project, JobLauncher::class.java, JobLauncherImpl::class.java)
            registerApplicationService(project, AsyncFutureFactory::class.java, AsyncFutureFactoryImpl::class.java)
            project.registerService(PsiSearchHelper::class.java, PsiSearchHelperImpl::class.java)
            project.registerService(DumbService::class.java, DumbServiceImpl::class.java)
            project.registerService(ResolveCache::class.java, ResolveCache::class.java)
            project.registerService(PsiFileFactory::class.java, PsiFileFactoryImpl::class.java)

            project.registerService(InjectedLanguageManager::class.java, InjectedLanguageManagerImpl::class.java)
            ProgressManager.getInstance()
        }

        private fun <T, S : T> registerApplicationService(project: Project, intfClass: Class<T>, implClass: Class<S>) {
            val application = ApplicationManager.getApplication() as MockApplicationEx
            application.registerService(intfClass, implClass)
            Disposer.register(project, Disposable { application.picoContainer.unregisterComponent(intfClass.name) })
        }

        fun initAppAndProject(rootDisposable: Disposable): MockProject {
            val application = initApplication(rootDisposable)
            val component = application.picoContainer.getComponentAdapter(ProgressManager::class.java.name)
            if (component == null) {
                application.picoContainer.registerComponent(object :
                    AbstractComponentAdapter(ProgressManager::class.java.name, Any::class.java) {
                    @Throws(PicoInitializationException::class, PicoIntrospectionException::class)
                    override fun getComponentInstance(container: PicoContainer): Any = ProgressManagerImpl()

                    @Throws(PicoIntrospectionException::class)
                    override fun verify(container: PicoContainer) {
                    }
                })
            }
            Extensions.registerAreaClass("IDEA_PROJECT", null)
            val project = MockProjectEx(rootDisposable)
            val appContainer = application.picoContainer
            registerComponentInstance(
                appContainer,
                MessageBus::class.java,
                MessageBusFactory.newMessageBus(application)
            )
            val editorFactory = MockEditorFactory()
            registerComponentInstance(appContainer, EditorFactory::class.java, editorFactory)
            registerComponentInstance(
                appContainer,
                FileDocumentManager::class.java,
                MockFileDocumentManagerImpl(
                    { editorFactory.createDocument(it) },
                    FileDocumentManagerImpl.HARD_REF_TO_DOCUMENT_KEY
                )
            )
            registerComponentInstance(appContainer, PsiDocumentManager::class.java, MockPsiDocumentManager())
            registerComponentInstance(
                appContainer,
                FileTypeManager::class.java,
                MockFileTypeManager(MockLanguageFileType(PlainTextLanguage.INSTANCE, "txt"))
            )
            // don't do that
            // registerComponentInstance(appContainer, ProjectManager.class, new ProjectManagerImpl(new ProgressManagerImpl()));

            registerApplicationService(project, PsiBuilderFactory::class.java, PsiBuilderFactoryImpl::class.java)
            registerApplicationService(project, DefaultASTFactory::class.java, DefaultASTFactoryImpl::class.java)
            registerApplicationService(
                project,
                ReferenceProvidersRegistry::class.java,
                ReferenceProvidersRegistryImpl::class.java
            )
            project.registerService(PsiManager::class.java, MockPsiManager::class.java)
            project.registerService(PsiFileFactory::class.java, PsiFileFactoryImpl::class.java)
            project.registerService(StartupManager::class.java, StartupManagerImpl::class.java)
            project.registerService(
                CachedValuesManager::class.java,
                CachedValuesManagerImpl(project, PsiCachedValuesFactory(PsiManager.getInstance(project)))
            )

            registerExtensionPoint(FileTypeFactory.FILE_TYPE_FACTORY_EP, FileTypeFactory::class.java)
            registerExtensionPoint(MetaLanguage.EP_NAME, MetaLanguage::class.java)
            return project
        }

        fun initApplication(rootDisposable: Disposable): MockApplicationEx {
            val instance = MockApplicationEx(rootDisposable)
            ApplicationManager.setApplication(
                instance,
                Getter<FileTypeRegistry> { FileTypeManager.getInstance() },
                rootDisposable
            )
            instance.registerService(EncodingManager::class.java, EncodingManagerImpl::class.java)
            return instance
        }

        fun <T> registerExtensionPoint(extensionPointName: ExtensionPointName<T>, aClass: Class<T>) {
            registerExtensionPoint(Extensions.getRootArea(), extensionPointName, aClass)
        }

        fun <T> registerExtensionPoint(area: ExtensionsArea,
                                       extensionPointName: ExtensionPointName<T>,
                                       aClass: Class<out T>) {
            val name = extensionPointName.name
            if (!area.hasExtensionPoint(name)) {
                val kind =
                    if (aClass.isInterface || aClass.modifiers and Modifier.ABSTRACT != 0) ExtensionPoint.Kind.INTERFACE else ExtensionPoint.Kind.BEAN_CLASS
                area.registerExtensionPoint(name, aClass.name, kind)
            }
        }

        fun <T> registerComponentInstance(container: MutablePicoContainer, key: Class<T>, implementation: T) {
            container.unregisterComponent(key)
            container.registerComponentInstance(key, implementation)
        }

        fun <T, KeyT> addKeyedExtension(instance: KeyedExtensionCollector<T, KeyT>,
                                        key: KeyT,
                                        `object`: T,
                                        disposable: Disposable?) {
            instance.addExplicitExtension(key, `object`)
            if (disposable != null) {
                Disposer.register(disposable, Disposable { instance.removeExplicitExtension(key, `object`) })
            }
        }
    }
}
