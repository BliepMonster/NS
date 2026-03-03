package main;

public interface StatementVisitor<R> {
    R visitExpressionStatement(ExpressionStatement stmt);
}
