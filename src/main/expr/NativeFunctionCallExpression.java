package main.expr;

import java.util.List;

public class NativeFunctionCallExpression extends Expression {
    public final String name;
    public final List<Expression> args;
    public NativeFunctionCallExpression(String name, List<Expression> args) {
        this.name = name;
        this.args = args;
    }
    public <R> R accept(ExpressionVisitor<R> visitor) {
        return visitor.visitNativeFunctionCallExpression(this);
    }
}
