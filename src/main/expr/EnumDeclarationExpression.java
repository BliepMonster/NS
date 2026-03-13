package main.expr;

import java.util.ArrayList;

public class EnumDeclarationExpression extends Expression {
    public final ArrayList<String> members;
    public EnumDeclarationExpression(ArrayList<String> members) {
        this.members = members;
    }
    public <R> R accept(ExpressionVisitor<R> visitor) {
        return visitor.visitEnumDeclarationExpression(this);
    }
    public boolean equals(Object o) {
        return o instanceof EnumDeclarationExpression e && e.members.equals(members);
    }
}
