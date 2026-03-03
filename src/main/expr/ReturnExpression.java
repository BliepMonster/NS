package main.expr;

public class ReturnExpression extends Expression {
    public final Expression value;
    public ReturnExpression(Expression value) {
        this.value = value;
    }
    public <R> R accept(ExpressionVisitor<R> visitor) {
        return visitor.visitReturnExpression(this);
    }
}
