package com.github.oowekyala.jjtx.reporting

import com.github.oowekyala.jjtx.util.LineAndCol
import com.github.oowekyala.jjtx.util.Position
import java.nio.file.Path


/**
 * Thrown after a fatal exception has been reported.
 * No need to log it further, can be caught on top level.
 */
class DoExitNowError : Error()


class JjtricksExceptionWrapper(override val cause: Throwable, message: String?, val position: Position?)
    : RuntimeException(message, cause) {


    companion object {

        private fun positionedMessage(cause: Throwable) =
            VelocityExtractor.extract(cause)
                ?: GJFormatExtractor.extract(cause)

        fun withKnownFileCtx(cause: Throwable, fileContents: String, path: Path): JjtricksExceptionWrapper =
            positionedMessage(cause)?.let {
                val (_, pos, _) = it

                if (pos is LineAndCol) it.copy(position = pos.upgrade(fileContents, path))
                else it
            }?.let {
                JjtricksExceptionWrapper(cause, it.message, it.position)
            } ?: JjtricksExceptionWrapper(cause, null, null)


        fun wrapIdem(throwable: Throwable): JjtricksExceptionWrapper =
            if (throwable is JjtricksExceptionWrapper) throwable
            else JjtricksExceptionWrapper(throwable, null, null)

    }
}
