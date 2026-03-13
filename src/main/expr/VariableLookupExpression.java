package main.expr;

public class VariableLookupExpression extends Expression {
    public final String name;
    public VariableLookupExpression(String name) {
        this.name = name;
    }
    public <R> R accept(ExpressionVisitor<R> visitor) {
        return visitor.visitVariableLookupExpression(this);
    }
    public boolean equals(Object o) {
        return o instanceof VariableLookupExpression v && v.name.equals(name);
    }
}
