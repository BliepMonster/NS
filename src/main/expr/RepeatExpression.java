package main.expr;

public class RepeatExpression extends Expression {
    public final Expression expr;
    public final int repeat;
    public RepeatExpression(Expression expr, int repeat) {
        this.expr = expr;
        this.repeat = repeat;
    }
    public <R> R accept(ExpressionVisitor<R> visitor) {
        return visitor.visitRepeatExpression(this);
    }
}
