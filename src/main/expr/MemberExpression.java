package main.expr;

public class MemberExpression extends Expression {
    public final Expression expr;
    public final String member;
    public MemberExpression(Expression expr, String member) {
        this.expr = expr;
        this.member = member;
    }
    public <R> R accept(ExpressionVisitor<R> visitor) {
        return visitor.visitMemberExpression(this);
    }
    public boolean equals(Object o) {
        return o instanceof MemberExpression m && m.expr.equals(expr) && m.member.equals(member);
    }
}
