package main.expr;

import java.util.HashMap;

public class ClassDeclarationExpression extends Expression {
    public final HashMap<String, Expression> members;
    public final Expression superclass;
    public ClassDeclarationExpression(HashMap<String, Expression> members, Expression superclass) {
        this.members = members;
        this.superclass = superclass;
    }
    public <R> R accept(ExpressionVisitor<R> visitor) {
        return visitor.visitClassDeclarationExpression(this);
    }
}
