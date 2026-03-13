package main.expr;

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
    public boolean equals(Object o) {
        return o instanceof LoopExpression l && l.cond.equals(cond) && l.body.equals(body);
    }
}
