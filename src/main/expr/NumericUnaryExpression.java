package main.expr;

import main.Token;

public class NumericUnaryExpression extends Expression {
    public final Expression expr;
    public final Token op;
    public NumericUnaryExpression(Expression expr, Token op) {
        this.expr = expr;
        this.op = op;
    }
    public <R> R accept(ExpressionVisitor<R> visitor) {
        return visitor.visitNumericUnaryExpression(this);
    }
    public boolean equals(Object o) {
        return o instanceof NumericUnaryExpression n && n.op == op && n.expr.equals(expr);
    }
}
