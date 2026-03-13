package main.expr;

import main.Token;

public class ForExpression extends Expression {
    public final Expression action, list;
    public final Token variable;
    public ForExpression(Expression action, Expression list, Token variable) {
        this.action = action;
        this.list = list;
        this.variable = variable;
    }
    public <R> R accept(ExpressionVisitor<R> visitor) {
        return visitor.visitForExpression(this);
    }
    public boolean equals(Object o) {
        return o instanceof ForExpression f && f.action.equals(action) && f.list.equals(list) && f.variable == variable;
    }
}
