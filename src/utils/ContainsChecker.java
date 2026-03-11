package utils;

import main.ExpressionStatement;
import main.Statement;
import main.StatementVisitor;
import main.expr.*;

import java.util.Map;

// does a given expression or statement contain a given string? -> optimization
public class ContainsChecker implements ExpressionVisitor<Boolean>, StatementVisitor<Boolean> {
    private final String s;
    public ContainsChecker(String s) {
        this.s = s;
    }
    public Boolean visitVariableLookupExpression(VariableLookupExpression expr) {
        return expr.name.equals(s);
    }
    public Boolean visitReturnExpression(ReturnExpression expr) {
        return expr.value.accept(this);
    }
    public Boolean visitCatchExpression(CatchExpression expr) {
        return expr.expr.accept(this) || expr.fallback.accept(this);
    }
    public Boolean visitThisExpression(ThisExpression expr) {
        return false;
    }
    public Boolean visitEnumDeclarationExpression(EnumDeclarationExpression expr) {
        return false;
    }
    public Boolean visitBinaryExpression(BinaryExpression expr) {
        return expr.left.accept(this) || expr.right.accept(this);
    }
    public Boolean visitUnaryExpression(UnaryExpression expr) {
        return expr.expr.accept(this);
    }
    public Boolean visitLiteralExpression(LiteralExpression expr) {
        return false;
    }
    public Boolean visitAssignmentExpression(AssignmentExpression expr) {
        return expr.target.accept(this) || expr.value.accept(this);
    }

    @Override
    public Boolean visitBlockExpression(BlockExpression expr) {
        for (Statement stmt : expr.statements) {
            if (stmt.accept(this)) return true;
        }
        return false;
    }
    public Boolean visitExpressionStatement(ExpressionStatement stmt) {
        return stmt.expr.accept(this);
    }
    public Boolean visitLoopExpression(LoopExpression expr) {
        return expr.cond.accept(this) || expr.body.accept(this);
    }
    public Boolean visitFunctionCallExpression(FunctionCallExpression expr) {
        if (expr.function.accept(this)) return true;
        for (Expression arg : expr.args) {
            if (arg.accept(this)) return true;
        }
        return false;
    }
    public Boolean visitMemberExpression(MemberExpression expr) {
        return expr.expr.accept(this);
    }
    public Boolean visitIndexExpression(IndexExpression expr) {
        return expr.expr.accept(this) || expr.index.accept(this);
    }
    public Boolean visitVectorExpression(VectorExpression expr) {
        for (Expression e : expr.elements) {
            if (e.accept(this)) return true;
        }
        return false;
    }
    public Boolean visitFunctionDeclarationExpression(FunctionDeclarationExpression expr) {
        return expr.body.accept(this);
    }
    public Boolean visitClassDeclarationExpression(ClassDeclarationExpression expr) {
        for (Map.Entry<String, Expression> entry : expr.members.entrySet()) {
            if (entry.getValue().accept(this)) return true;
        }
        return expr.superclass != null && expr.superclass.accept(this);
    }
    public Boolean visitNativeFunctionCallExpression(NativeFunctionCallExpression expr) {
        for (Expression arg : expr.args) {
            if (arg.accept(this)) return true;
        }
        return false;
    }
    public Boolean visitDictionaryExpression(DictionaryExpression expr) {
        for (DictionaryExpression.Pair pair : expr.pairs) {
            if (pair.key().accept(this) || pair.value().accept(this)) return true;
        }
        return false;
    }
    public Boolean visitListExpression(ListExpression expr) {
        for (Expression e : expr.elements) {
            if (e.accept(this)) return true;
        }
        return false;
    }
    public Boolean visitTernaryExpression(TernaryExpression expr) {
        return expr.condition.accept(this) || expr.trueExpr.accept(this) || expr.falseExpr.accept(this);
    }
    public Boolean visitMatchExpression(MatchExpression expr) {
        if (expr.expr.accept(this))
            return true;
        for (MatchExpression.Case caseExpr : expr.cases) {
            if (caseExpr.pattern().accept(this) || caseExpr.value().accept(this))
                return true;
        }
        if (expr.other != null)
            return expr.other.accept(this);
        return false;
    }
    public Boolean visitRangeExpression(RangeExpression expr) {
        return expr.start.accept(this) || expr.end.accept(this);
    }
    public Boolean visitSetExpression(SetExpression expr) {
        for (Expression expression : expr.expr) {
            if (expression.accept(this)) return true;
        }
        return false;
    }
    public Boolean visitForExpression(ForExpression expr) {
        return expr.action.accept(this) || expr.list.accept(this) || expr.variable.text().equals(s);
    }
    public Boolean visitRepeatExpression(RepeatExpression expr) {
        return expr.expr.accept(this);
    }
}
