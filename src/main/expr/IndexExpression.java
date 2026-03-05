package main.expr;

public class IndexExpression extends Expression {
    public final Expression expr;
    public final Expression index;
    public IndexExpression(Expression expr, Expression index) {
        this.expr = expr;
        this.index = index;
    }
    public <R> R accept(ExpressionVisitor<R> visitor) {
        return visitor.visitIndexExpression(this);
    }
}
