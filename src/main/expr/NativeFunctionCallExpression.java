package main.expr;

import java.util.ArrayList;

public class NativeFunctionCallExpression extends Expression {
    public final String name;
    public final ArrayList<Expression> args;
    public NativeFunctionCallExpression(String name, ArrayList<Expression> args) {
        this.name = name;
        this.args = args;
    }
    public <R> R accept(ExpressionVisitor<R> visitor) {
        return visitor.visitNativeFunctionCallExpression(this);
    }
}
