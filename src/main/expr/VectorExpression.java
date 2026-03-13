package main.expr;

import java.util.ArrayList;

public class VectorExpression extends Expression {
    public final ArrayList<Expression> elements;
    public VectorExpression(ArrayList<Expression> elements) {
        this.elements = elements;
    }
    public <R> R accept(ExpressionVisitor<R> visitor) {
        return visitor.visitVectorExpression(this);
    }
    public boolean equals(Object o) {
        return o instanceof VectorExpression v && v.elements.equals(elements);
    }
}
