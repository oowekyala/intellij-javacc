package com.github.oowekyala.jjtx.tasks

import com.github.oowekyala.jjtx.JjtxContext
import java.nio.file.Path

data class TaskCtx(
    val ctx: JjtxContext,
    val outputDir: Path,
    val otherSourceRoots: List<Path>
)
