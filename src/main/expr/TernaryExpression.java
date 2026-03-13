package main.expr;

public class TernaryExpression extends Expression {
    public final Expression condition;
    public final Expression trueExpr;
    public final Expression falseExpr;
    public TernaryExpression(Expression condition, Expression trueExpr, Expression falseExpr) {
        this.condition = condition;
        this.trueExpr = trueExpr;
        this.falseExpr = falseExpr;
    }
    public <R> R accept(ExpressionVisitor<R> visitor) {
        return visitor.visitTernaryExpression(this);
    }
    public boolean equals(Object o) {
        return o instanceof TernaryExpression t && t.condition.equals(condition) && t.trueExpr.equals(trueExpr) && t.falseExpr.equals(falseExpr);
    }
}
