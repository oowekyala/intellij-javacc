package com.github.oowekyala.jjtx

import com.github.oowekyala.ijcc.lang.model.InlineGrammarOptions
import com.github.oowekyala.jjtx.templates.VisitorConfig
import com.github.oowekyala.jjtx.typeHierarchy.TypeHierarchyTree

/**
 * @author Clément Fournier
 */
class OldJavaccOptionsModel(
    grammarOptions: InlineGrammarOptions
) : JjtxOptsModel {

    override val inlineBindings: InlineGrammarOptions = grammarOptions

    override val isDefaultVoid: Boolean = grammarOptions.isDefaultVoid

    override val parentModel: JjtxOptsModel? = null

    override val nodePackage: String = grammarOptions.nodePackage

    override val nodePrefix: String = grammarOptions.nodePrefix

    override val visitors: List<VisitorConfig> = emptyList()

    override val typeHierarchy: TypeHierarchyTree = TypeHierarchyTree.default()
}
