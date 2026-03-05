package main.expr;

import main.Token;

public class AssignmentExpression extends Expression {
    public final Expression target;
    public final Expression value;
    public final Token token;
    public AssignmentExpression(Expression target, Expression value, Token token) {
        this.target = target;
        this.value = value;
        this.token = token;
    }
    public <R> R accept(ExpressionVisitor<R> visitor) {
        return visitor.visitAssignmentExpression(this);
    }
}
