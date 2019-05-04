package com.github.oowekyala.jjtx

import com.github.oowekyala.jjtx.util.ErrorCollector
import com.github.oowekyala.jjtx.util.ErrorCollectorImpl
import com.github.oowekyala.jjtx.util.Io
import com.github.oowekyala.jjtx.util.Severity

/**
 * @author Clément Fournier
 */
class JjtxRunContext(jjtxParams: JjtxParams,
                     collectorBuilder: (JjtxRunContext) -> ErrorCollector= {
                         ErrorCollectorImpl(it, Severity.WARN)
                     })
    : JjtxContext(jjtxParams.mainGrammarFile, jjtxParams.configChain) {

    override val io: Io = jjtxParams.io

    override val errorCollector: ErrorCollector = collectorBuilder(this)

}
