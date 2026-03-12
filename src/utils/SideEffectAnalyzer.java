package utils;

import main.*;
import main.expr.*;

/**
 * Checks a statement or an expression for side effects, so they are not accidentally optimized away.<br><br>
 * An expression is said to have a side effect if one of the following is true:<ul>
 *     <li>The expression is a function call</li>
 *     <li>The expression is a native function call</li>
 *     <li>The expression is an assignment expression</li>
 *     <li>One of the expression's subexpressions has a side effect</li>
 *     <li>The expression is a return expression</li>
 * </ul>
 */
public class SideEffectAnalyzer implements StatementVisitor<Boolean>, ExpressionVisitor<Boolean> {
    public Boolean visitExpressionStatement(ExpressionStatement stmt) {
        return stmt.expr.accept(this);
    }
    public Boolean visitBlockExpression(main.expr.BlockExpression expr) {
        for (Statement stmt : expr.statements) {
            if (stmt.accept(this)) return true;
        }
        return false;
    }
    public Boolean visitReturnExpression(ReturnExpression expr) {
        return true;
    }
    public Boolean visitAssignmentExpression(AssignmentExpression expr) {
        return true;
    }
    public Boolean visitBinaryExpression(BinaryExpression expr) {
        // overloading is left-only
        if (Optimizer.getType(expr.left) == Optimizer.VariableType.UNKNOWN)
            return true;
        return expr.left.accept(this) || expr.right.accept(this);
    }
    public Boolean visitUnaryExpression(UnaryExpression expr) {
        if (Optimizer.getType(expr.expr) == Optimizer.VariableType.UNKNOWN)
            return true;
        return expr.expr.accept(this);
    }
    public Boolean visitFunctionCallExpression(FunctionCallExpression expr) {
        for (Expression arg : expr.args) {
            if (arg.accept(this)) return true;
        }
        if (expr.function instanceof FunctionDeclarationExpression fd) {
            return fd.body.accept(this); // in reality, this is just a glorified expression call with local argument assignments (which are popped after execution)
        }
        return true;
    }
    public Boolean visitNativeFunctionCallExpression(NativeFunctionCallExpression expr) {
        for (Expression arg : expr.args) {
            if (arg.accept(this)) return true;
        }
        return switch (expr.name) {
            case "str" -> {
                if (expr.args.size() != 1)
                    throw new RuntimeException("str() takes exactly one argument");
                Optimizer.VariableType type = Optimizer.getType(expr.args.getFirst());
                yield type == Optimizer.VariableType.UNKNOWN; // non-objects have no str() side effects
            }
            case "iter" -> {
                if (expr.args.size() != 1)
                    throw new RuntimeException("iter() takes exactly one argument");
                Optimizer.VariableType type = Optimizer.getType(expr.args.getFirst());
                yield type == Optimizer.VariableType.UNKNOWN;
            }
            case "ignoreLoopResult", "clock", "bignum", "step" -> false; // if the loop contains no side effects
            case "first", "last" -> {
                if (expr.args.size() != 1)
                    throw new RuntimeException("first() or last() take exactly one argument");
                Optimizer.VariableType type = Optimizer.getType(expr.args.getFirst());
                yield type == Optimizer.VariableType.UNKNOWN;
            }
            default -> true;
        };
    }
    public Boolean visitVariableLookupExpression(VariableLookupExpression expr) {
        return false;
    }
    public Boolean visitLiteralExpression(LiteralExpression expr) {
        return false;
    }
    public Boolean visitMemberExpression(MemberExpression expr) {
        return expr.expr.accept(this);
    }
    public Boolean visitIndexExpression(IndexExpression expr) {
        if (Optimizer.getType(expr.expr) == Optimizer.VariableType.UNKNOWN)
            return true;
        return expr.expr.accept(this) || expr.index.accept(this);
    }
    public Boolean visitThisExpression(ThisExpression expr) {
        return false;
    }
    public Boolean visitEnumDeclarationExpression(EnumDeclarationExpression expr) {
        return false;
    }
    public Boolean visitVectorExpression(VectorExpression expr) {
        for (Expression e : expr.elements) {
            if (e.accept(this)) return true;
        }
        return false;
    }
    public Boolean visitFunctionDeclarationExpression(FunctionDeclarationExpression expr) {
        return false; // a normal declaration without a call will not have a side effect
    }
    public Boolean visitLoopExpression(LoopExpression expr) {
        return expr.cond.accept(this) || expr.body.accept(this);
    }
    public Boolean visitClassDeclarationExpression(ClassDeclarationExpression expr) {
        return false; // expressions in classes are lazy
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
    public Boolean visitCatchExpression(CatchExpression expr) {
        return expr.expr.accept(this) || expr.fallback.accept(this);
    }
    public Boolean visitForExpression(ForExpression expr) {
        return expr.action.accept(this) || expr.list.accept(this);
    }
    public Boolean visitRepeatExpression(RepeatExpression expr) {
        return expr.expr.accept(this);
    }
}
