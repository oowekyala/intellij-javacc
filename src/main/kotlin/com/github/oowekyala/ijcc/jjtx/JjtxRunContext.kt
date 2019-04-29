package com.github.oowekyala.ijcc.jjtx

import com.github.oowekyala.ijcc.jjtx.typeHierarchy.TypeHierarchyTree
import com.github.oowekyala.ijcc.lang.psi.JccFile
import com.github.oowekyala.ijcc.lang.psi.allJjtreeDecls

/**
 * @author Clément Fournier
 */
class JjtxRunContext(val jjtxParams: JjtxParams,
                     val grammarFile: JccFile) {


    val errorCollector = ErrorCollector(this)

    val jjtxOptsModel: JjtxOptsModel =
        jjtxParams.jjtxConfigFile?.let {
            JjtxOptsModel.parse(it)
        } ?: JjtxOptsModel.default()


    /**
     * A type hierarchy, if it was specified in the jjtOpts.
     */
    val typeHierarchy: TypeHierarchyTree? by lazy {
        val jjtDecls = grammarFile.allJjtreeDecls
        jjtxOptsModel.typeHierarchy?.let {
            TypeHierarchyTree.buildFully(it, jjtDecls, this)
        }
    }




}
