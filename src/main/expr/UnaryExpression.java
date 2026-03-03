package main.expr;

import main.Token;

public class UnaryExpression extends Expression {
    public final Expression expr;
    public final Token op;
    public UnaryExpression(Token op, Expression expr) {
        this.op = op;
        this.expr = expr;
    }
    public <R> R accept(ExpressionVisitor<R> visitor) {
        return visitor.visitUnaryExpression(this);
    }
}
