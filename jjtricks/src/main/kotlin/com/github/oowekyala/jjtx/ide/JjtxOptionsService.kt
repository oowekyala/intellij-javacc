package com.github.oowekyala.jjtx.ide

import com.github.oowekyala.ijcc.lang.model.GrammarNature
import com.github.oowekyala.ijcc.lang.model.IGrammarOptions
import com.github.oowekyala.ijcc.lang.psi.impl.GrammarOptionsService
import com.github.oowekyala.ijcc.lang.psi.impl.JccFileImpl
import com.github.oowekyala.jjtx.JjtxContext


open class JjtxOptionsService : GrammarOptionsService() {

    override fun buildOptions(jccFileImpl: JccFileImpl): IGrammarOptions =
        when {
            jccFileImpl.grammarNature < GrammarNature.JJTRICKS -> super.buildOptions(jccFileImpl)
            else                                               -> JjtxContext.buildCtx(jccFileImpl).jjtxOptsModel
        }

}


class JjtxFullOptionsService(val ctx: JjtxContext) : JjtxOptionsService() {


    override fun buildOptions(jccFileImpl: JccFileImpl): IGrammarOptions =
        when (jccFileImpl) {
            ctx.grammarFile -> ctx.jjtxOptsModel
            else            -> super.buildOptions(jccFileImpl)
        }

}
