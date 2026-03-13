package main.expr;

import java.util.ArrayList;
import java.util.Objects;

public class MatchExpression extends Expression {
    public final Expression expr;
    public record Case(Expression pattern, Expression value) {}
    public final ArrayList<Case> cases;
    public final Expression other;
    public MatchExpression(Expression expr, ArrayList<Case> cases, Expression other) {
        this.expr = expr;
        this.cases = cases;
        this.other = other;
    }
    public <R> R accept(ExpressionVisitor<R> visitor) {
        return visitor.visitMatchExpression(this);
    }
    public boolean equals(Object o) {
        return o instanceof MatchExpression m && m.expr.equals(expr) && m.cases.equals(cases) && Objects.equals(m.other, other);
    }
}
