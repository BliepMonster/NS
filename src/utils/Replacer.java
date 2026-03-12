package utils;

import main.*;
import main.expr.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

// ONLY USE IN PURE CODE: NO ASSIGNMENTS (function calls okay-ish)
public class Replacer implements ExpressionVisitor<Expression>, StatementVisitor<Statement> {
    private final Expression replacement;
    private final String target;
    public Replacer(Expression replacement, String replacementText) {
        this.replacement = replacement;
        this.target = replacementText;
    }
    public Expression visitVariableLookupExpression(VariableLookupExpression expr) {
        if (expr.name.equals(target)) return replacement;
        return expr;
    }
    public Statement visitExpressionStatement(ExpressionStatement stmt) {
        return new ExpressionStatement(stmt.expr.accept(this));
    }
    public Expression visitReturnExpression(ReturnExpression expr) {
        return new ReturnExpression(expr.value.accept(this));
    }
    public Expression visitCatchExpression(CatchExpression expr) {
        return new CatchExpression(expr.expr.accept(this), expr.fallback.accept(this));
    }
    public Expression visitAssignmentExpression(AssignmentExpression expr) {
        throw new RuntimeException("Replacer should not be used on assignments: undefined behavior");
    }
    public Expression visitBinaryExpression(BinaryExpression expr) {
        return new BinaryExpression(expr.left.accept(this), expr.op, expr.right.accept(this));
    }
    public Expression visitUnaryExpression(UnaryExpression expr) {
        return new UnaryExpression(expr.op, expr.expr.accept(this));
    }
    public Expression visitFunctionCallExpression(FunctionCallExpression expr) {
        ArrayList<Expression> newArgs = new ArrayList<>();
        for (Expression arg : expr.args) {
            newArgs.add(arg.accept(this));
        }
        return new FunctionCallExpression(expr.function.accept(this), newArgs);
    }
    public Expression visitMemberExpression(MemberExpression expr) {
        return new MemberExpression(expr.expr.accept(this), expr.member);
    }
    public Expression visitIndexExpression(IndexExpression expr) {
        return new IndexExpression(expr.expr.accept(this), expr.index.accept(this));
    }
    public Expression visitVectorExpression(VectorExpression expr) {
        ArrayList<Expression> newElements = new ArrayList<>();
        for (Expression e : expr.elements) {
            newElements.add(e.accept(this));
        }
        return new VectorExpression(newElements);
    }
    public Expression visitThisExpression(ThisExpression expr) {
        return expr;
    }
    public Expression visitEnumDeclarationExpression(EnumDeclarationExpression expr) {
        return expr;
    }
    public Expression visitLiteralExpression(LiteralExpression expr) {
        return expr;
    }
    public Expression visitFunctionDeclarationExpression(FunctionDeclarationExpression expr) {
        for (Token t : expr.args) {
            if (t.text().equals(target))
                return expr;
        }
        return new FunctionDeclarationExpression(expr.args, expr.body.accept(this)); // may behave weirdly??
    }
    public Expression visitLoopExpression(LoopExpression expr) {
        return new LoopExpression(expr.cond.accept(this), expr.body.accept(this));
    }
    public Expression visitClassDeclarationExpression(ClassDeclarationExpression expr) {
        HashMap<String, Expression> newMembers = new HashMap<>();
        for (Map.Entry<String, Expression> s : expr.members.entrySet()) {
            newMembers.put(s.getKey(), s.getValue().accept(this));
        }
        return new ClassDeclarationExpression(newMembers, expr.superclass != null ? expr.superclass.accept(this) : null);
    }
    public Expression visitBlockExpression(BlockExpression expr) {
        ArrayList<Statement> newStatements = new ArrayList<>();
        for (Statement stmt : expr.statements) {
            newStatements.add(stmt.accept(this));
        }
        return new BlockExpression(newStatements);
    }
    public Expression visitDictionaryExpression(DictionaryExpression expr) {
        ArrayList<DictionaryExpression.Pair> newPairs = new ArrayList<>();
        for (DictionaryExpression.Pair pair : expr.pairs) {
            newPairs.add(new DictionaryExpression.Pair(pair.key().accept(this), pair.value().accept(this)));
        }
        return new DictionaryExpression(newPairs);
    }
    public Expression visitListExpression(ListExpression expr) {
        ArrayList<Expression> newElements = new ArrayList<>();
        for (Expression e : expr.elements) {
            newElements.add(e.accept(this));
        }
        return new ListExpression(newElements);
    }
    public Expression visitTernaryExpression(TernaryExpression expr) {
        return new TernaryExpression(expr.condition.accept(this), expr.trueExpr.accept(this), expr.falseExpr.accept(this));
    }
    public Expression visitMatchExpression(MatchExpression expr) {
        ArrayList<MatchExpression.Case> newCases = new ArrayList<>();
        for (MatchExpression.Case c : expr.cases) {
            newCases.add(new MatchExpression.Case(c.pattern().accept(this), c.value().accept(this)));
        }
        return new MatchExpression(expr.expr.accept(this), newCases, expr.other != null ? expr.other.accept(this) : null);
    }
    public Expression visitNativeFunctionCallExpression(NativeFunctionCallExpression expr) {
        ArrayList<Expression> newArgs = new ArrayList<>();
        for (Expression arg : expr.args) {
            newArgs.add(arg.accept(this));
        }
        return new NativeFunctionCallExpression(expr.name, newArgs);
    }
    public Expression visitRangeExpression(RangeExpression expr) {
        return new RangeExpression(expr.start.accept(this), expr.end.accept(this));
    }
    public Expression visitSetExpression(SetExpression expr) {
        ArrayList<Expression> exprs = new ArrayList<>();
        for (Expression e : expr.expr) {
            exprs.add(e.accept(this));
        }
        return new SetExpression(exprs);
    }
    public Expression visitForExpression(ForExpression expr) {
        if (expr.variable.text().equals(target)) {
            return new ForExpression(expr.action, expr.list.accept(this), expr.variable);
        }
        return new ForExpression(expr.action.accept(this), expr.list.accept(this), expr.variable);
    }
    public Expression visitRepeatExpression(RepeatExpression expr) {
        return new RepeatExpression(expr.expr.accept(this), expr.repeat);
    }
    public Expression visitNumericBinaryExpression(NumericBinaryExpression expr) {
        return new NumericBinaryExpression(expr.left.accept(this), expr.op, expr.right.accept(this));
    }
    public Expression visitNumericUnaryExpression(NumericUnaryExpression expr) {
        return new NumericUnaryExpression(expr.expr.accept(this), expr.op);
    }
}
