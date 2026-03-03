package main.expr;

import main.Statement;

public class LoopExpression extends Expression {
    public final Expression cond;
    public final Expression body;
    public LoopExpression(Expression cond, Expression body) {
        this.cond = cond;
        this.body = body;
    }
    public <R> R accept(ExpressionVisitor<R> visitor) {
        return visitor.visitLoopExpression(this);
    }
}
