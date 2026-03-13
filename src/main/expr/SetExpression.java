package main.expr;

import java.util.List;

public class SetExpression extends Expression {
    public final List<Expression> expr;
    public SetExpression(List<Expression> expr) {
        this.expr = expr;
    }
    public <R> R accept(ExpressionVisitor<R> visitor) {
        return visitor.visitSetExpression(this);
    }
    public boolean equals(Object o) {
        return o instanceof SetExpression s && s.expr.equals(expr);
    }
}
