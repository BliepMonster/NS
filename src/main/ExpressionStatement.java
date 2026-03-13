package main;

import main.expr.Expression;

public class ExpressionStatement extends Statement {
    public final Expression expr;

    public ExpressionStatement(Expression expr) {
        this.expr = expr;
    }

    @Override
    public <R> R accept(StatementVisitor<R> visitor) {
        return visitor.visitExpressionStatement(this);
    }
    public boolean equals(Object o) {
        return o instanceof ExpressionStatement s && s.expr.equals(expr);
    }
}
