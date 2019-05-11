package com.github.oowekyala.jjtx.preprocessor

import com.github.oowekyala.jjtx.JjtxTestBase
import io.kotlintest.shouldBe

/**
 * @author Clément Fournier
 */
class JavaccTranslationTest : JjtxTestBase() {


    fun testSimple() = doTest {

        val jcc = """

        options {
            LOOKAHEAD = 1;
            CHOICE_AMBIGUITY_CHECK = 2;
            FORCE_LA_CHECK = false;
        }

        PARSER_BEGIN(DummyExprParser)

            package org.javacc.jjtree;

            /**
             *  This is my parser declaration
             */
            public class DummyExprParser {

              void jjtreeOpenNodeScope(Node n) {
                ((JJTreeNode)n).setFirstToken(getToken(1));
              }

            }

        PARSER_END(DummyExprParser)

        // Some token declarations
        <DEFAULT>
        TOKEN :{
          < PLUS: "+" >
        | < MINUS: "-" >
        | < NULL: "null" >
        | < INTEGER: ["+",  "-"] <DIGITS> >
        | < #DIGITS: (["0"-"9"])+ >
        }


        void Expression(): {}
        {
              BinaryExpression()
            | <NULL>   #NullLiteral
        }

        void BinaryExpression() #BinaryExpression(>1): {}
        {
            UnaryExpression() [ ( "+" | "-" ) UnaryExpression() ]
        }

        void UnaryExpression() #void: {}
        {
          "(" Expression() ")" | Integer()
        }

        void Integer() #IntegerLiteral: {}
        {
          <INTEGER>
        }

        """.trimIndent().asJccFile()

        val str = toJavaccString(jcc)

        str shouldBe "fjieif"


    }


}
