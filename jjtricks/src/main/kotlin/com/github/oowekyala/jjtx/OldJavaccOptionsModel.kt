package com.github.oowekyala.jjtx

import com.github.oowekyala.ijcc.lang.model.InlineGrammarOptions
import com.github.oowekyala.ijcc.lang.psi.JccFile
import com.github.oowekyala.jjtx.preprocessor.JavaccGenOptions
import com.github.oowekyala.jjtx.templates.ClassVBean
import com.github.oowekyala.jjtx.templates.FileGenBean
import com.github.oowekyala.jjtx.templates.GrammarGenerationScheme
import com.github.oowekyala.jjtx.templates.NodeVBean

/**
 * Wraps an [InlineGrammarOptions] and implements [JjtxOptsModel].
 * Those have less precedence than regular jjtopts files.
 *
 * @author Clément Fournier
 */
internal class OldJavaccOptionsModel(grammarFile: JccFile) : JjtxOptsModel {

    private val grammarOptions = InlineGrammarOptions(grammarFile)

    override val inlineBindings: InlineGrammarOptions = grammarOptions

    override val isTrackTokens: Boolean = grammarOptions.isTrackTokens

    override val isDefaultVoid: Boolean = grammarOptions.isDefaultVoid

    override val parentModel: JjtxOptsModel? = null

    override val nodePackage: String = grammarOptions.nodePackage

    override val nodePrefix: String = grammarOptions.nodePrefix

    override val commonGen: Map<String, FileGenBean> = emptyMap()

    override val nodeGen: GrammarGenerationScheme? = null // TODO JJTree generation scheme

    override val javaccGen: JavaccGenOptions = JavaccGenOptions.FullJjtreeCompat


    override val typeHierarchy: NodeVBean = NodeVBean(
        name = grammarOptions.rootNodeClass.substringAfterLast('.'),
        `class` = ClassVBean(grammarOptions.rootNodeClass),
        superNode = null,
        subNodes = emptyList()
    )

    override val templateContext: Map<String, Any> = emptyMap()
}
