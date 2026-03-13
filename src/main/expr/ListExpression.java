package main.expr;

import java.util.List;

public class ListExpression extends Expression {
    public final List<Expression> elements;
    public ListExpression(List<Expression> elements) {
        this.elements = elements;
    }
    public <R> R accept(ExpressionVisitor<R> visitor) {
        return visitor.visitListExpression(this);
    }
    public boolean equals(Object o) {
        return o instanceof ListExpression l && l.elements.equals(elements);
    }
}
