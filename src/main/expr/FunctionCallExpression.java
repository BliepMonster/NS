package main.expr;

import java.util.ArrayList;

public class FunctionCallExpression extends Expression {
    public final Expression function;
    public final ArrayList<Expression> args;
    public FunctionCallExpression(Expression function, ArrayList<Expression> args) {
        this.function = function;
        this.args = args;
    }
    public <R> R accept(ExpressionVisitor<R> visitor) {
        return visitor.visitFunctionCallExpression(this);
    }
}
