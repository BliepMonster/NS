package main.expr;

import main.Statement;

import java.util.ArrayList;

public class BlockExpression extends Expression {
    public final ArrayList<Statement> statements;
    public BlockExpression(ArrayList<Statement> statements) {
        this.statements = statements;
    }
    public <R> R accept(ExpressionVisitor<R> visitor) {
        return visitor.visitBlockExpression(this);
    }
    public boolean equals(Object o) {
        return o instanceof BlockExpression b && b.statements.equals(statements);
    }
}
