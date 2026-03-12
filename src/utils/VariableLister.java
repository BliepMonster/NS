package utils;

import main.ExpressionStatement;
import main.Statement;
import main.StatementVisitor;
import main.expr.*;

public class VariableLister implements ExpressionVisitor<VariableList>, StatementVisitor<VariableList> {
    public VariableList visitExpressionStatement(ExpressionStatement stmt) {
        return stmt.expr.accept(this);
    }
    public VariableList visitVariableLookupExpression(VariableLookupExpression expr) {
        return new VariableList(expr.name);
    }
    public VariableList visitAssignmentExpression(AssignmentExpression expr) {
        return expr.target.accept(this).with(expr.value.accept(this));
    }
    public VariableList visitBinaryExpression(BinaryExpression expr) {
        return expr.left.accept(this).with(expr.right.accept(this));
    }
    public VariableList visitUnaryExpression(UnaryExpression expr) {
        return expr.expr.accept(this);
    }
    public VariableList visitFunctionCallExpression(FunctionCallExpression expr) {
        VariableList list = new VariableList();
        for (Expression arg : expr.args) {
            list.addAll(arg.accept(this));
        }
        return list.with(expr.function.accept(this));
    }
    public VariableList visitMemberExpression(MemberExpression expr) {
        return expr.expr.accept(this);
    }
    public VariableList visitIndexExpression(IndexExpression expr) {
        return expr.expr.accept(this).with(expr.index.accept(this));
    }
    public VariableList visitVectorExpression(VectorExpression expr) {
        VariableList list = new VariableList();
        for (Expression e : expr.elements) {
            list.addAll(e.accept(this));
        }
        return list;
    }
    public VariableList visitFunctionDeclarationExpression(FunctionDeclarationExpression expr) {
        return expr.body.accept(this); // no args checking because they are shadowed and thus pure
    }
    public VariableList visitLoopExpression(LoopExpression expr) {
        return expr.cond.accept(this).with(expr.body.accept(this));
    }
    public VariableList visitClassDeclarationExpression(ClassDeclarationExpression expr) {
        VariableList list = new VariableList();
        for (Expression e : expr.members.values()) {
            list.addAll(e.accept(this));
        }
        return list.with(expr.superclass != null ? expr.superclass.accept(this) : new VariableList());
    }
    public VariableList visitDictionaryExpression(DictionaryExpression expr) {
        VariableList list = new VariableList();
        for (DictionaryExpression.Pair pair : expr.pairs) {
            list.addAll(pair.key().accept(this));
            list.addAll(pair.value().accept(this));
        }
        return list;
    }
    public VariableList visitListExpression(ListExpression expr) {
        VariableList list = new VariableList();
        for (Expression e : expr.elements) {
            list.addAll(e.accept(this));
        }
        return list;
    }
    public VariableList visitTernaryExpression(TernaryExpression expr) {
        return expr.condition.accept(this).with(expr.trueExpr.accept(this)).with(expr.falseExpr.accept(this));
    }
    public VariableList visitMatchExpression(MatchExpression expr) {
        VariableList list = expr.expr.accept(this);
        for (MatchExpression.Case arm : expr.cases) {
            list.addAll(arm.pattern().accept(this));
            list.addAll(arm.value().accept(this));
        }
        return list.with(expr.other != null ? expr.other.accept(this) : new VariableList());
    }
    public VariableList visitRangeExpression(RangeExpression expr) {
        return expr.start.accept(this).with(expr.end.accept(this));
    }
    public VariableList visitSetExpression(SetExpression expr) {
        VariableList list = new VariableList();
        for (Expression expression : expr.expr) {
            list.addAll(expression.accept(this));
        }
        return list;
    }
    public VariableList visitThisExpression(ThisExpression expr) {
        return new VariableList();
    }
    public VariableList visitLiteralExpression(LiteralExpression expr) {
        return new VariableList();
    }
    public VariableList visitEnumDeclarationExpression(EnumDeclarationExpression expr) {
        return new VariableList();
    }
    public VariableList visitBlockExpression(BlockExpression expr) {
        VariableList list = new VariableList();
        for (Statement stmt : expr.statements)
            list.addAll(stmt.accept(this));
        return list;
    }
    public VariableList visitReturnExpression(ReturnExpression expr) {
        return expr.value.accept(this);
    }
    public VariableList visitCatchExpression(CatchExpression expr) {
        return expr.expr.accept(this).with(expr.fallback.accept(this));
    }
    public VariableList visitNativeFunctionCallExpression(NativeFunctionCallExpression expr) {
        VariableList list = new VariableList();
        for (Expression arg : expr.args) {
            list.addAll(arg.accept(this));
        }
        return list;
    }
    public VariableList visitForExpression(ForExpression expr) {
        VariableList list = expr.action.accept(this);
        list.remove(expr.variable.text()); // shadowed
        list.addAll(expr.list.accept(this));
        return list;
    }
    public VariableList visitRepeatExpression(RepeatExpression expr) {
        return expr.expr.accept(this);
    }
}
