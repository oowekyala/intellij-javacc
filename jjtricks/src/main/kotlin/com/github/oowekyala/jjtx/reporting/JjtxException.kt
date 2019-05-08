package com.github.oowekyala.jjtx.reporting


/**
 * Thrown after a fatal exception has been reported.
 * No need to log it further, can be caught on top level.
 */
class DoExitNowError : Error()
