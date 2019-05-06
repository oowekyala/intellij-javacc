package com.github.oowekyala.jjtx.ide

import com.github.oowekyala.ijcc.lang.model.GrammarNature
import com.github.oowekyala.ijcc.lang.model.IGrammarOptions
import com.github.oowekyala.ijcc.lang.psi.impl.GrammarOptionsService
import com.github.oowekyala.ijcc.lang.psi.impl.JccFileImpl
import com.github.oowekyala.jjtx.JjtxLightContext


class JjtxOptionsService : GrammarOptionsService() {

    override fun buildOptions(jccFileImpl: JccFileImpl): IGrammarOptions =
        if (jccFileImpl.grammarNature < GrammarNature.JJTRICKS)
            super.buildOptions(jccFileImpl)
        else JjtxLightContext(jccFileImpl).jjtxOptsModel

}

