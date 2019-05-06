package com.github.oowekyala.jjtx.ide

import com.github.oowekyala.ijcc.lang.model.GrammarNature
import com.github.oowekyala.ijcc.lang.model.IGrammarOptions
import com.github.oowekyala.ijcc.lang.psi.impl.GrammarOptionsService
import com.github.oowekyala.ijcc.lang.psi.impl.JccFileImpl
import com.github.oowekyala.jjtx.JjtxLightContext
import com.github.oowekyala.jjtx.JjtxRunContext


open class JjtxOptionsService : GrammarOptionsService() {

    override fun buildOptions(jccFileImpl: JccFileImpl): IGrammarOptions =
        when {
            jccFileImpl.grammarNature < GrammarNature.JJTRICKS -> super.buildOptions(jccFileImpl)
            else                                               -> JjtxLightContext(jccFileImpl).jjtxOptsModel
        }

}


class JjtxFullOptionsService(val ctx: JjtxRunContext) : JjtxOptionsService() {


    override fun buildOptions(jccFileImpl: JccFileImpl): IGrammarOptions =
        when (jccFileImpl) {
            ctx.grammarFile -> ctx.jjtxOptsModel
            else            -> super.buildOptions(jccFileImpl)
        }

}
