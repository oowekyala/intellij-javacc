package com.github.oowekyala.jjtx

import com.github.oowekyala.jjtx.util.ErrorCollectorImpl
import com.github.oowekyala.jjtx.util.Io

/**
 * @author Clément Fournier
 */
class JjtxRunContext(jjtxParams: JjtxParams) : JjtxContext(jjtxParams.mainGrammarFile, jjtxParams.configChain) {

    override val io: Io = jjtxParams.io

    override val errorCollector = ErrorCollectorImpl(this)

}
