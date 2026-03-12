package main.expr;

import main.Token;

public class NumericBinaryExpression extends Expression {
    public final Expression left, right;
    public final Token op;
    public NumericBinaryExpression(Expression left, Token op, Expression right) {
        this.left = left;
        this.op = op;
        this.right = right;
    }
    public <R> R accept(ExpressionVisitor<R> visitor) {
        return visitor.visitNumericBinaryExpression(this);
    }
}
