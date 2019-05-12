package com.github.oowekyala.jjtx.reporting

import com.github.oowekyala.jjtx.util.Position

/**
 * @author Clément Fournier
 */

data class PositionedMessage(
    val message: String,
    val position: Position
)

interface ErrorPositionRecoverer {

    fun extract(throwable: Throwable): PositionedMessage?

}
