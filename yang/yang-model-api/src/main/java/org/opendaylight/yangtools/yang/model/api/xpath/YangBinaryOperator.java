package org.opendaylight.yangtools.yang.model.api.xpath;

import static java.util.Objects.requireNonNull;

/**
 * YANG XPath binary operator.
 *
 * @author Robert Varga
 */
public enum YangBinaryOperator {
    /**
     * @see <a href="https://www.w3.org/TR/1999/REC-xpath-19991116/#NT-EqualityExpr">EqualityExpr</a>
     */
    EQUALS("="),
    /**
     * @see <a href="https://www.w3.org/TR/1999/REC-xpath-19991116/#NT-EqualityExpr">EqualityExpr</a>
     */
    NOT_EQUALS("!="),

    /**
     * @see <a href="https://www.w3.org/TR/1999/REC-xpath-19991116/#NT-RelationalExpr">RelationalExpr</a>
     */
    GT(">"),
    /**
     * @see <a href="https://www.w3.org/TR/1999/REC-xpath-19991116/#NT-RelationalExpr">RelationalExpr</a>
     */
    GTE(">="),
    /**
     * @see <a href="https://www.w3.org/TR/1999/REC-xpath-19991116/#NT-RelationalExpr">RelationalExpr</a>
     */
    LT("<"),
    /**
     * @see <a href="https://www.w3.org/TR/1999/REC-xpath-19991116/#NT-RelationalExpr">RelationalExpr</a>
     */
    LTE("<="),

    /**
     * @see <a href="https://www.w3.org/TR/1999/REC-xpath-19991116/#NT-AndExpr">AndExpr</a>
     */
    AND("and"),
    /**
     * @see <a href="https://www.w3.org/TR/1999/REC-xpath-19991116/#NT-OrExpr">OrExpr</a>
     */
    OR("or"),

    /**
     * @see <a href="https://www.w3.org/TR/1999/REC-xpath-19991116/#NT-AdditiveExpr">AdditiveExpr</a>
     */
    PLUS("+"),
    /**
     * @see <a href="https://www.w3.org/TR/1999/REC-xpath-19991116/#NT-AdditiveExpr">AdditiveExpr</a>
     */
    MINUS("-"),

    /**
     * @see <a href="https://www.w3.org/TR/1999/REC-xpath-19991116/#NT-MultiplicativeExpr">MultiplicativeExpr</a>
     */
    MUL("*"),
    /**
     * @see <a href="https://www.w3.org/TR/1999/REC-xpath-19991116/#NT-MultiplicativeExpr">MultiplicativeExpr</a>
     */
    DIV("div"),
    /**
     * @see <a href="https://www.w3.org/TR/1999/REC-xpath-19991116/#NT-MultiplicativeExpr">MultiplicativeExpr</a>
     */
    MOD("mod"),

    /**
     * @see <a href="https://www.w3.org/TR/1999/REC-xpath-19991116/#NT-UnionExpr">UnionExpr</a>
     */
    UNION("|");

    private final String str;

    private YangBinaryOperator(final String str) {
        this.str = requireNonNull(str);
    }

    @Override
    public String toString() {
        return str;
    }
}
