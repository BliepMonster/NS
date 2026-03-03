package main.expr;

public class ThisExpression extends Expression {
    public <R> R accept(ExpressionVisitor<R> visitor) {
        return visitor.visitThisExpression(this);
    }
}
