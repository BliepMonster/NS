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
    public boolean equals(Object o) {
        return o instanceof RangeExpression r && r.start.equals(start) && r.end.equals(end);
    }
}
