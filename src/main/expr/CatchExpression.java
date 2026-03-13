package main.expr;

public class CatchExpression extends Expression {
    public final Expression expr, fallback;
    public CatchExpression(Expression expr, Expression fallback) {
        this.expr = expr;
        this.fallback = fallback;
    }
    public <R> R accept(ExpressionVisitor<R> visitor) {
        return visitor.visitCatchExpression(this);
    }
    public boolean equals(Object o) {
        return o instanceof CatchExpression c && c.expr.equals(expr) && c.fallback.equals(fallback);
    }
}
