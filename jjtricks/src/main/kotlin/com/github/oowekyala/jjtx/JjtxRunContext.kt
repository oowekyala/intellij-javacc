package com.github.oowekyala.jjtx

import com.github.oowekyala.jjtx.util.MessageCollector
import com.github.oowekyala.jjtx.util.MessageCollectorImpl
import com.github.oowekyala.jjtx.util.Io
import com.github.oowekyala.jjtx.util.Severity

/**
 * @author Clément Fournier
 */
class JjtxRunContext(jjtxParams: JjtxParams,
                     collectorBuilder: (JjtxRunContext) -> MessageCollector= {
                         MessageCollectorImpl(it, Severity.WARN)
                     })
    : JjtxContext(jjtxParams.mainGrammarFile, jjtxParams.configChain) {

    override val io: Io = jjtxParams.io

    override val messageCollector: MessageCollector = collectorBuilder(this)

}
