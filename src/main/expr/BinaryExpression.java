package main.expr;

import main.Token;

public class BinaryExpression extends Expression {
    public final Expression left, right;
    public final Token op;
    public BinaryExpression(Expression left, Token op, Expression right) {
        this.left = left;
        this.op = op;
        this.right = right;
    }
    public <R> R accept(ExpressionVisitor<R> visitor) {
        return visitor.visitBinaryExpression(this);
    }
}
