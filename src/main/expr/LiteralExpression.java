package main.expr;

import main.interpreter.values.builtins.Value;

public class LiteralExpression extends Expression {
    public final Value value;
    public LiteralExpression(Value value) {
        this.value = value;
    }
    public <R> R accept(ExpressionVisitor<R> visitor) {
        return visitor.visitLiteralExpression(this);
    }
}
