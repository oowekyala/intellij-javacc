/**
 * Generated By:JavaCC: Do not edit this line. SimpleExprParser.java
 */
package com.jjtx.exprs;


import java.util.ArrayList;
import java.util.List;


/**
 * This is my parser declaration
 */
/* @bgen(jjtree) */
public class SimpleExprParser implements SimpleExprParserConstants , SimpleExprParserTreeConstants {
    /* @bgen(jjtree) */
    protected final JJTSimpleExprParserState jjtree = new com.jjtx.exprs.JJTSimpleExprParserState();

    public final void Expression() throws ParseException {
        jjtThis.setImage("Expr");
        switch ((jj_ntk) == (-1) ? jj_ntk() : jj_ntk) {
            case SimpleExprParserConstants.INTEGER :
            case 6 :
                BinaryExpression();
                break;
            case SimpleExprParserConstants.NULL :
                ASTNullLiteral nullLiteral = new ASTNullLiteral(JJTNULLLITERAL);
                boolean nullLiteralNeedsClose = true;
                jjtree.openNodeScope(nullLiteral, getToken(1));
                try {
                    jj_consume_token(SimpleExprParserConstants.NULL);
                } catch (Throwable nullLiteralException) {
                    if (nullLiteralNeedsClose) {
                        jjtree.closeNodeScope(nullLiteral, getToken(0), true);
                        nullLiteralNeedsClose = false;
                    } else {
                        jjtree.popNode();
                    }
                    // This chain of casts is meant to force you to declare
                    // checked exceptions explicitly on the productions, else it fails
                    // with a ClassCastException on the Error branch
                    if (nullLiteralException instanceof ParseException) {
                        throw ((ParseException) (nullLiteralException));
                    }
                    if (nullLiteralException instanceof RuntimeException) {
                        throw ((RuntimeException) (nullLiteralException));
                    }
                    {
                        throw ((Error) (nullLiteralException));
                    }
                } finally {
                    if (nullLiteralNeedsClose) {
                        jjtree.closeNodeScope(nullLiteral, getToken(0), true);
                    }
                }
                break;
            default :
                jj_la1[0] = jj_gen;
                jj_consume_token((-1));
                throw new ParseException();
        }
    }

    public final ASTBinaryExpr BinaryExpression() throws ParseException {
        /* @bgen(jjtree) BinaryExpr */
        ASTBinaryExpr binaryExpr = new ASTBinaryExpr(JJTBINARYEXPR);
        boolean binaryExprNeedsClose = true;
        jjtree.openNodeScope(binaryExpr, getToken(1));
        binaryExpr.setImage("Expr");
        try {
            UnaryExpr();
            binaryExpr.foo();
            switch ((jj_ntk) == (-1) ? jj_ntk() : jj_ntk) {
                case SimpleExprParserConstants.PLUS :
                case SimpleExprParserConstants.MINUS :
                    switch ((jj_ntk) == (-1) ? jj_ntk() : jj_ntk) {
                        case SimpleExprParserConstants.PLUS :
                            jj_consume_token(SimpleExprParserConstants.PLUS);
                            break;
                        case SimpleExprParserConstants.MINUS :
                            jj_consume_token(SimpleExprParserConstants.MINUS);
                            break;
                        default :
                            jj_la1[1] = jj_gen;
                            jj_consume_token((-1));
                            throw new ParseException();
                    }
                    UnaryExpr(binaryExpr);
                    break;
                default :
                    jj_la1[2] = jj_gen;
            }
            {
                return binaryExpr;
            }
        } catch (Throwable binaryExprException) {
            if (binaryExprNeedsClose) {
                jjtree.closeNodeScope(binaryExpr, getToken(0), ((jjtree.nodeArity()) > 1));
                binaryExprNeedsClose = false;
            } else {
                jjtree.popNode();
            }
            // This chain of casts is meant to force you to declare
            // checked exceptions explicitly on the productions, else it fails
            // with a ClassCastException on the Error branch
            if (binaryExprException instanceof ParseException) {
                throw ((ParseException) (binaryExprException));
            }
            if (binaryExprException instanceof RuntimeException) {
                throw ((RuntimeException) (binaryExprException));
            }
            {
                throw ((Error) (binaryExprException));
            }
        } finally {
            if (binaryExprNeedsClose) {
                jjtree.closeNodeScope(binaryExpr, getToken(0), ((jjtree.nodeArity()) > 1));
            }
        }
        throw new Error("Missing return statement in function");
    }

    public final void UnaryExpr() throws ParseException {
        switch ((jj_ntk) == (-1) ? jj_ntk() : jj_ntk) {
            case 6 :
                ASTParenthesizedExpr parenthesizedExpr = new ASTParenthesizedExpr(JJTPARENTHESIZEDEXPR);
                boolean parenthesizedExprNeedsClose = true;
                jjtree.openNodeScope(parenthesizedExpr, getToken(1));
                try {
                    jj_consume_token(6);
                    Expression();
                    jj_consume_token(7);
                    jjtThisManRocks.foo();
                    parenthesizedExpr.foo();
                } catch (Throwable parenthesizedExprException) {
                    if (parenthesizedExprNeedsClose) {
                        jjtree.closeNodeScope(parenthesizedExpr, getToken(0), true);
                        parenthesizedExprNeedsClose = false;
                    } else {
                        jjtree.popNode();
                    }
                    // This chain of casts is meant to force you to declare
                    // checked exceptions explicitly on the productions, else it fails
                    // with a ClassCastException on the Error branch
                    if (parenthesizedExprException instanceof ParseException) {
                        throw ((ParseException) (parenthesizedExprException));
                    }
                    if (parenthesizedExprException instanceof RuntimeException) {
                        throw ((RuntimeException) (parenthesizedExprException));
                    }
                    {
                        throw ((Error) (parenthesizedExprException));
                    }
                } finally {
                    if (parenthesizedExprNeedsClose) {
                        jjtree.closeNodeScope(parenthesizedExpr, getToken(0), true);
                    }
                }
                jjtThis.foo();
                break;
            case SimpleExprParserConstants.INTEGER :
                Integer();
                break;
            default :
                jj_la1[3] = jj_gen;
                jj_consume_token((-1));
                throw new ParseException();
        }
    }

    public final void Integer() throws ParseException {
        /* @bgen(jjtree) IntegerLiteral */
        ASTIntegerLiteral integerLiteral = new ASTIntegerLiteral(JJTINTEGERLITERAL);
        boolean integerLiteralNeedsClose = true;
        jjtree.openNodeScope(integerLiteral, getToken(1));
        try {
            jj_consume_token(SimpleExprParserConstants.INTEGER);
        } catch (Throwable integerLiteralException) {
            if (integerLiteralNeedsClose) {
                jjtree.closeNodeScope(integerLiteral, getToken(0), true);
                integerLiteralNeedsClose = false;
            } else {
                jjtree.popNode();
            }
            // This chain of casts is meant to force you to declare
            // checked exceptions explicitly on the productions, else it fails
            // with a ClassCastException on the Error branch
            if (integerLiteralException instanceof ParseException) {
                throw ((ParseException) (integerLiteralException));
            }
            if (integerLiteralException instanceof RuntimeException) {
                throw ((RuntimeException) (integerLiteralException));
            }
            {
                throw ((Error) (integerLiteralException));
            }
        } finally {
            if (integerLiteralNeedsClose) {
                jjtree.closeNodeScope(integerLiteral, getToken(0), true);
            }
        }
    }

    /**
     * Generated Token Manager.
     */
    public SimpleExprParserTokenManager token_source;

    /**
     * Current token.
     */
    public Token token;

    /**
     * Next token.
     */
    public Token jj_nt;

    private int jj_ntk;

    private int jj_gen;

    private final int[] jj_la1 = new int[4];

    private static int[] jj_la1_0;

    static {
        SimpleExprParser.jj_la1_init_0();
    }

    private static void jj_la1_init_0() {
        SimpleExprParser.jj_la1_0 = new int[]{ 88, 6, 6, 80 };
    }

    /**
     * Constructor with user supplied CharStream.
     */
    public SimpleExprParser(CharStream stream) {
        token_source = new SimpleExprParserTokenManager(stream);
        token = new Token();
        jj_ntk = -1;
        jj_gen = 0;
        for (int i = 0; i < 4; i++)
            jj_la1[i] = -1;

    }

    /**
     * Reinitialise.
     */
    public void ReInit(CharStream stream) {
        token_source.ReInit(stream);
        token = new Token();
        jj_ntk = -1;
        jj_gen = 0;
        for (int i = 0; i < 4; i++)
            jj_la1[i] = -1;

    }

    /**
     * Constructor with generated Token Manager.
     */
    public SimpleExprParser(SimpleExprParserTokenManager tm) {
        token_source = tm;
        token = new Token();
        jj_ntk = -1;
        jj_gen = 0;
        for (int i = 0; i < 4; i++)
            jj_la1[i] = -1;

    }

    /**
     * Reinitialise.
     */
    public void ReInit(SimpleExprParserTokenManager tm) {
        token_source = tm;
        token = new Token();
        jj_ntk = -1;
        jj_gen = 0;
        for (int i = 0; i < 4; i++)
            jj_la1[i] = -1;

    }

    private Token jj_consume_token(int kind) throws ParseException {
        Token oldToken;
        if (((oldToken = token).next) != null)
            token = token.next;
        else
            token = setTokenNext(token, token_source.getNextToken());

        jj_ntk = -1;
        if ((token.kind) == kind) {
            (jj_gen)++;
            return token;
        }
        token = oldToken;
        jj_kind = kind;
        throw generateParseException();
    }

    /**
     * Get the next Token.
     */
    public final Token getNextToken() {
        if ((token.next) != null)
            token = token.next;
        else
            token = setTokenNext(token, token_source.getNextToken());

        jj_ntk = -1;
        (jj_gen)++;
        return token;
    }

    /**
     * Get the specific Token.
     */
    public final Token getToken(int index) {
        Token t = token;
        for (int i = 0; i < index; i++) {
            if ((t.next) != null)
                t = t.next;
            else
                t = setTokenNext(t, token_source.getNextToken());

        }
        return t;
    }

    private int jj_ntk() {
        if ((jj_nt = token.next) == null)
            return jj_ntk = (token.next = token_source.getNextToken()).kind;
        else
            return jj_ntk = jj_nt.kind;

    }

    private List<int[]> jj_expentries = new ArrayList<int[]>();

    private int[] jj_expentry;

    private int jj_kind = -1;

    /**
     * Generate ParseException.
     */
    public ParseException generateParseException() {
        jj_expentries.clear();
        boolean[] la1tokens = new boolean[8];
        if ((jj_kind) >= 0) {
            la1tokens[jj_kind] = true;
            jj_kind = -1;
        }
        for (int i = 0; i < 4; i++) {
            if ((jj_la1[i]) == (jj_gen)) {
                for (int j = 0; j < 32; j++) {
                    if (((SimpleExprParser.jj_la1_0[i]) & (1 << j)) != 0) {
                        la1tokens[j] = true;
                    }
                }
            }
        }
        for (int i = 0; i < 8; i++) {
            if (la1tokens[i]) {
                jj_expentry = new int[1];
                jj_expentry[0] = i;
                jj_expentries.add(jj_expentry);
            }
        }
        int[][] exptokseq = new int[jj_expentries.size()][];
        for (int i = 0; i < (jj_expentries.size()); i++) {
            exptokseq[i] = jj_expentries.get(i);
        }
        return new ParseException(token, exptokseq, SimpleExprParserConstants.tokenImage);
    }

    /**
     * Enable tracing.
     */
    public final void enable_tracing() {
    }

    /**
     * Disable tracing.
     */
    public final void disable_tracing() {
    }

    private static Token setTokenNext(Token lhs, Token rhs) {
        lhs.next = rhs;
        return rhs;
    }
}
