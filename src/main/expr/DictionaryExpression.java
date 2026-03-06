package main.expr;

import java.util.ArrayList;

public class DictionaryExpression extends Expression {
    public record Pair(Expression key, Expression value) {}
    public final ArrayList<Pair> pairs;
    public DictionaryExpression(ArrayList<Pair> pairs) {
        this.pairs = pairs;
    }
    public <R> R accept(ExpressionVisitor<R> visitor) {
        return visitor.visitDictionaryExpression(this);
    }
}
