package com.github.oowekyala.jjtx

import com.github.oowekyala.jjtx.reporting.MessageCollector

/**
 * @author Clément Fournier
 */
class JjtxRunContext(jjtxParams: JjtxParams,
                     collector: MessageCollector)
    : JjtxContext(jjtxParams.mainGrammarFile, jjtxParams.configChain, collector, jjtxParams.io)
