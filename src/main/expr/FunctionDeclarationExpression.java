package main.expr;

import main.Token;

import java.util.ArrayList;

public class FunctionDeclarationExpression extends Expression {
    public final ArrayList<Token> args;
    public final Expression body;
    public FunctionDeclarationExpression(ArrayList<Token> args, Expression body) {
        this.args = args;
        this.body = body;
    }
    public <R> R accept(ExpressionVisitor<R> visitor) {
        return visitor.visitFunctionDeclarationExpression(this);
    }
    public boolean equals(Object o) {
        return o instanceof FunctionDeclarationExpression f && f.args.equals(args) && f.body.equals(body);
    }
}
