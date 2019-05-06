package com.github.oowekyala.jjtx

import com.github.oowekyala.ijcc.lang.model.InlineGrammarOptions
import com.github.oowekyala.ijcc.lang.psi.JccFile
import com.github.oowekyala.jjtx.templates.VisitorGenerationTask
import com.github.oowekyala.jjtx.typeHierarchy.TypeHierarchyTree

/**
 * Wraps an [InlineGrammarOptions] and implements [JjtxOptsModel].
 * Those have less precedence than regular jjtopts files.
 *
 * @author Clément Fournier
 */
internal class OldJavaccOptionsModel(
    grammarFile: JccFile
) : JjtxOptsModel {

    private val grammarOptions = InlineGrammarOptions(grammarFile)

    override val inlineBindings: InlineGrammarOptions = grammarOptions

    override val isDefaultVoid: Boolean = grammarOptions.isDefaultVoid

    override val parentModel: JjtxOptsModel? = null

    override val nodePackage: String = grammarOptions.nodePackage

    override val nodePrefix: String = grammarOptions.nodePrefix

    override val visitors: Map<String, VisitorGenerationTask> = emptyMap()

    override val typeHierarchy: TypeHierarchyTree = TypeHierarchyTree.default()

    override val templateContext: Map<String, Any> = emptyMap()
}
