package main.expr;

import java.util.HashMap;
import java.util.Objects;

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
    public boolean equals(Object o) {
        return o instanceof ClassDeclarationExpression c && c.members.equals(members) && Objects.equals(c.superclass, superclass);
    }
}
