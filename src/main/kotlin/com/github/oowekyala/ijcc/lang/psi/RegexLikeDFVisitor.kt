package com.github.oowekyala.ijcc.lang.psi

/**
 * @author Clément Fournier
 * @since 1.0
 */
abstract class RegexLikeDFVisitor : DepthFirstVisitor() {


    abstract override fun visitLiteralRegularExpression(o: JccLiteralRegularExpression)

    abstract override fun visitNamedRegularExpression(o: JccNamedRegularExpression)

    abstract override fun visitEofRegularExpression(o: JccEofRegularExpression)

    abstract override fun visitRefRegularExpression(o: JccRefRegularExpression)

    abstract override fun visitContainerRegularExpression(o: JccContainerRegularExpression)

}