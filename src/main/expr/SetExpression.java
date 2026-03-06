package main.expr;

import java.util.Set;

public class SetExpression extends Expression {
    public final Set<Expression> expr;
    public SetExpression(Set<Expression> expr) {
        this.expr = expr;
    }
    public <R> R accept(ExpressionVisitor<R> visitor) {
        return visitor.visitSetExpression(this);
    }
}
