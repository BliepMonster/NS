package main.expr;

public abstract class Expression {
    public abstract <R> R accept(ExpressionVisitor<R> visitor);
    public abstract boolean equals(Object object);
}
