package main.expr;

public class AssignmentExpression extends Expression {
    public final Expression target;
    public final Expression value;
    public AssignmentExpression(Expression target, Expression value) {
        this.target = target;
        this.value = value;
    }
    public <R> R accept(ExpressionVisitor<R> visitor) {
        return visitor.visitAssignmentExpression(this);
    }
}
