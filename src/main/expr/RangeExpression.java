package main.expr;

public class RangeExpression extends Expression {
    public final Expression start, end;
    public RangeExpression(Expression start, Expression end) {
        this.start = start;
        this.end = end;
    }
    public <R> R accept(ExpressionVisitor<R> visitor) {
        return visitor.visitRangeExpression(this);
    }
}
