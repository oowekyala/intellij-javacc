package com.github.oowekyala.jjtx

import com.github.oowekyala.ijcc.lang.psi.JccFile

/**
 * @author Clément Fournier
 */
class JjtxRunContext(jjtxParams: JjtxParams,
                     grammarFile: JccFile) : JjtxContext(grammarFile, jjtxParams.configChain) {


    override val errorCollector = ErrorCollectorImpl(this)


}
