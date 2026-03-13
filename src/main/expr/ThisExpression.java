package main.expr;

public class ThisExpression extends Expression {
    public <R> R accept(ExpressionVisitor<R> visitor) {
        return visitor.visitThisExpression(this);
    }
    public boolean equals(Object o) {
        return o instanceof ThisExpression;
    }
}
