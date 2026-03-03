package main.expr;

import java.util.ArrayList;

public class ListExpression extends Expression {
    public final ArrayList<Expression> elements;
    public ListExpression(ArrayList<Expression> elements) {
        this.elements = elements;
    }
    public <R> R accept(ExpressionVisitor<R> visitor) {
        return visitor.visitListExpression(this);
    }
}
