package com.github.oowekyala.jjtx.postprocessor

import com.github.oowekyala.ijcc.lang.model.addParserPackage
import com.github.oowekyala.jjtx.JjtxContext
import com.github.oowekyala.jjtx.templates.vbeans.ClassVBean


val JjtxContext.tokenClass: ClassVBean
    get() = ClassVBean(
        jjtxOptsModel.javaccGen.supportFiles["token"]?.genFqcn
            ?: jjtxOptsModel.addParserPackage("Token")
    )
