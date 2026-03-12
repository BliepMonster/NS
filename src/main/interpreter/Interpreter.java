package main.interpreter;

import main.*;
import main.expr.*;
import main.interpreter.values.builtins.*;
import main.interpreter.values.builtins.BooleanValue;
import main.interpreter.values.builtins.NullValue;
import main.interpreter.values.builtins.NumericValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import static main.TokenType.*;

// native functions may use other strategies, like $ignoreWhileResult(loop) which is made for optimization
enum LoopEvaluationStrategy {
    NO_ELEMENTS,
    LAST_ONLY,
    ALL_ELEMENTS,
    FIRST_ONLY
}

public class Interpreter implements StatementVisitor<Void>, ExpressionVisitor<Value>, Executor {
    private Scope scope = new Scope();
    private final List<Statement> statements;
    public Interpreter(List<Statement> statements) {
        ExecutorHolder.EXECUTOR = this;
        this.statements = statements;
    }
    public void interpret() {
        for (Statement stmt : statements) {
            stmt.accept(this);
        }
    }
    public Value visitTernaryExpression(TernaryExpression expr) {
        Value v = expr.condition.accept(this);
        if (!(v instanceof BooleanValue bv))
            throw new RuntimeException("Expected boolean value, got " + v);
        if (bv.value)
            return expr.trueExpr.accept(this);
        return expr.falseExpr.accept(this);
    }
    public Void visitExpressionStatement(ExpressionStatement stmt) {
        stmt.expr.accept(this);
        return null;
    }
    public Value visitAssignmentExpression(AssignmentExpression expr) {
        Expression left = expr.target;
        Value val;
        if (expr.token.type() == EQ)
            val = expr.value.accept(this);
        else switch(expr.token.type()) {
            case PLUS_EQ -> val = left.accept(this).add(expr.value.accept(this));
            case MINUS_EQ -> val = left.accept(this).sub(expr.value.accept(this));
            case STAR_EQ -> val = left.accept(this).mul(expr.value.accept(this));
            case SLASH_EQ -> val = left.accept(this).div(expr.value.accept(this));
            case MOD_EQ -> val = left.accept(this).mod(expr.value.accept(this));
            default -> throw new RuntimeException("Invalid assignment operator: " + expr.token);
        }
        switch (left) {
            case VariableLookupExpression vle: {
                scope.assign(vle.name, val);
                return val;
            }
            case MemberExpression me: {
                Value target = me.expr.accept(this);
                target.setMember(me.member, val);
                return val;
            }
            case IndexExpression ie: {
                Value target = ie.expr.accept(this);
                target.setIndex(ie.index.accept(this), val);
                return val;
            }
            default:
                throw new RuntimeException("Invalid assignment target: " + left);
        }
    }
    public Value visitVariableLookupExpression(VariableLookupExpression expr) {
        return scope.lookup(expr.name);
    }
    public Value visitBinaryExpression(BinaryExpression expr) {
        TokenType t = expr.op.type();
        if (t == TokenType.OR) {
            Value left = expr.left.accept(this);
            if (!(left instanceof BooleanValue bv)) {
                throw new RuntimeException("Expected boolean value, got " + left);
            } if (bv.value) {
                return left;
            } else {
                Value v = expr.right.accept(this);
                if (!(v instanceof BooleanValue bv2)) {
                    throw new RuntimeException("Expected boolean value, got " + v);
                }
                return bv2;
            }
        } else if (t == TokenType.AND) {
            Value left = expr.left.accept(this);
            if (!(left instanceof BooleanValue bv)) {
                throw new RuntimeException("Expected boolean value, got " + left);
            } if (!bv.value) {
                return left;
            } else {
                Value v = expr.right.accept(this);
                if (!(v instanceof BooleanValue bv2)) {
                    throw new RuntimeException("Expected boolean value, got " + v);
                }
                return bv2;
            }
        }
        return switch (t) {
            case PLUS -> expr.left.accept(this).add(expr.right.accept(this));
            case MINUS -> expr.left.accept(this).sub(expr.right.accept(this));
            case STAR -> expr.left.accept(this).mul(expr.right.accept(this));
            case SLASH -> expr.left.accept(this).div(expr.right.accept(this));
            case MOD -> expr.left.accept(this).mod(expr.right.accept(this));
            case LT -> expr.left.accept(this).lt(expr.right.accept(this));
            case LTEQ -> expr.left.accept(this).lte(expr.right.accept(this));
            case GT -> expr.left.accept(this).gt(expr.right.accept(this));
            case GTEQ -> expr.left.accept(this).gte(expr.right.accept(this));
            case EQEQ -> expr.left.accept(this).eq(expr.right.accept(this));
            case BANG_EQ -> expr.left.accept(this).neq(expr.right.accept(this));
            case PIPE -> expr.left.accept(this).merge(expr.right.accept(this));
            case IN -> expr.right.accept(this).contains(expr.left.accept(this));
            default -> throw new RuntimeException("Invalid operator: " + expr.op);
        };
    }
    public Value visitUnaryExpression(UnaryExpression expr) {
        return switch (expr.op.type()) {
            case MINUS -> expr.expr.accept(this).neg();
            case BANG -> expr.expr.accept(this).inv();
            case HASH -> expr.expr.accept(this).toNumber();
            case QUESTION -> expr.expr.accept(this).isTruthy();
            case ARTIFICIAL_NBOOL -> BooleanValue.fromBoolean(!expr.expr.accept(this).isTruthy().value);
            default -> throw new RuntimeException("Invalid operator: " + expr.op);
        };
    }
    public Value visitFunctionCallExpression(FunctionCallExpression expr) {
        List<Value> args = expr.args.stream().map(arg -> arg.accept(this)).toList();
        return expr.function.accept(this).call(args);
    }
    public Value visitMemberExpression(MemberExpression expr) {
        return expr.expr.accept(this).getMember(expr.member);
    }
    public Value visitIndexExpression(IndexExpression expr) {
        return expr.expr.accept(this).index(expr.index.accept(this));
    }
    public Value visitThisExpression(ThisExpression expr) {
        return scope.getThis();
    }
    public Value visitLiteralExpression(LiteralExpression expr) {
        return expr.value;
    }
    public Value visitVectorExpression(VectorExpression expr) {
        NumericValue[] values = new NumericValue[expr.elements.size()];
        int i = 0;
        for (Expression e : expr.elements) {
            Value v = e.accept(this);
            if (!(v instanceof NumericValue nv)) {
                throw new RuntimeException("Expected numeric value, got " + v);
            }
            values[i++] = nv;
        }
        return new VectorValue(values);
    }
    public Value visitLoopExpression(LoopExpression expr) {
        return evaluateLoop(expr, LoopEvaluationStrategy.ALL_ELEMENTS);
    }
    private Value evaluateLoop(LoopExpression expr, LoopEvaluationStrategy strategy) {
        Expression cond = expr.cond;
        ArrayList<Value> values = new ArrayList<>();
        boolean isFirst = true;
        Value last = NullValue.INSTANCE, first = NullValue.INSTANCE;
        if (cond == null)
            throw new RuntimeException("Loop condition is missing");
        while (true) {
            Value v = cond.accept(this);
            if (!(v instanceof BooleanValue bv)) {
                throw new RuntimeException("Expected boolean value, got " + v);
            }
            if (!bv.value) {
                return switch (strategy) {
                    case LAST_ONLY -> last;
                    case NO_ELEMENTS -> NullValue.INSTANCE;
                    case FIRST_ONLY -> first;
                    case ALL_ELEMENTS -> new ListValue(values);
                };
            }
            Value res = expr.body.accept(this);
            if (isFirst) {
                first = res;
                isFirst = false;
            }
            if (strategy == LoopEvaluationStrategy.ALL_ELEMENTS)
                values.add(res);
            last = res;
        }
    }
    public Value visitFunctionDeclarationExpression(FunctionDeclarationExpression expr) {
        ArrayList<String> params = new ArrayList<>();
        for (Token t : expr.args) {
            params.add(t.text());
        }
        return new InterpretedFunctionValue(params, expr.body, scope);
    }
    public Value visitClassDeclarationExpression(ClassDeclarationExpression expr) {
        if (expr.superclass != null) {
            Value superClass = expr.superclass.accept(this);
            if (!(superClass instanceof InterpretedClassValue sc))
                throw new RuntimeException("Superclass must be a class");
            expr.members.putAll(sc.fields);
        }
        return new InterpretedClassValue(expr.members);
    }
    public Value visitBlockExpression(BlockExpression expr) {
        scope = new Scope(scope);
        try {
            for (Statement s : expr.statements) {
                s.accept(this);
            }
            return NullValue.INSTANCE;
        } catch (Return r) {
            return r.value;
        } finally {
            scope = scope.parent;
        }
    }
    public Value visitReturnExpression(ReturnExpression expr) {
        if (expr.value != null) {
            Value v = expr.value.accept(this);
            throw new Return(v);
        } else {
            throw new Return(NullValue.INSTANCE);
        }
    }
    public Value visitNativeFunctionCallExpression(NativeFunctionCallExpression expr) {
        switch (expr.name) {
            case "ignoreLoopResult" -> {
                if (expr.args.size() != 1) {
                    throw new RuntimeException("ignoreLoopResult expects 1 argument");
                }
                Expression cond = expr.args.getFirst();
                if (!(cond instanceof LoopExpression loop)) {
                    if (cond instanceof ForExpression forExpr) {
                        return executeForLoop(forExpr, LoopEvaluationStrategy.NO_ELEMENTS);
                    } else if (cond instanceof RepeatExpression repExpr) {
                        return executeRepeat(repExpr, LoopEvaluationStrategy.NO_ELEMENTS);
                    }
                    throw new RuntimeException("ignoreLoopResult expects a loop expression");
                }
                return evaluateLoop(loop, LoopEvaluationStrategy.NO_ELEMENTS);
            }
            case "last" -> {
                if (expr.args.size() != 1) {
                    throw new RuntimeException("ignoreWhileResult expects 1 argument");
                }
                Expression cond = expr.args.getFirst();
                if (!(cond instanceof LoopExpression loop)) {
                    if (cond instanceof ForExpression forExpr) {
                        return executeForLoop(forExpr, LoopEvaluationStrategy.LAST_ONLY);
                    } else if (cond instanceof RepeatExpression repExpr) {
                        return executeRepeat(repExpr, LoopEvaluationStrategy.LAST_ONLY);
                    }
                    return cond.accept(this).last();
                }
                return evaluateLoop(loop, LoopEvaluationStrategy.LAST_ONLY);
            }
            case "first" -> {
                if (expr.args.size() != 1) {
                    throw new RuntimeException("ignoreWhileResult expects 1 argument");
                }
                Expression cond = expr.args.getFirst();
                if (!(cond instanceof LoopExpression loop)) {
                    if (cond instanceof ForExpression forExpr) {
                        return executeForLoop(forExpr, LoopEvaluationStrategy.FIRST_ONLY);
                    } else if (cond instanceof RepeatExpression repExpr) {
                        return executeRepeat(repExpr, LoopEvaluationStrategy.FIRST_ONLY);
                    }
                    return cond.accept(this).first();
                }
                return evaluateLoop(loop, LoopEvaluationStrategy.FIRST_ONLY);
            }
        }
        List<Value> args = expr.args.stream().map(arg -> arg.accept(this)).toList();
        CompiledFunctionValue f = FunctionRegistry.functions.get(expr.name);
        if (f == null) {
            throw new RuntimeException("Unknown function: " + expr.name);
        }
        return f.call(args);
    }
    public Value visitListExpression(ListExpression expr) {
        ArrayList<Value> values = new ArrayList<>();
        for (Expression e : expr.elements) {
            values.add(e.accept(this));
        }
        return new ListValue(values);
    }
    public Value evaluate(Expression expr) {
        return expr.accept(this);
    }
    public void execute(Statement stmt) {
        stmt.accept(this);
    }
    public Value callFunction(InterpretedFunctionValue f, List<Value> args, Value thisArg) {
        Scope newScope = new Scope(f.closure);
        if (thisArg != null) {
            // special method so it doesn't override other scope this
            newScope.bindThis(thisArg);
        }
        for (int i = 0; i < f.parameters.size(); i++) {
            newScope.assignLocal(f.parameters.get(i), args.get(i));
        }
        Scope oldScope = scope;
        scope = newScope;
        try {
            return f.body.accept(this);
        } finally {
            scope = oldScope;
        }
    }
    public Value visitCatchExpression(CatchExpression expr) {
        try {
            return expr.expr.accept(this);
        } catch (RuntimeException e) {
            return expr.fallback.accept(this);
        }
    }
    public Value visitEnumDeclarationExpression(EnumDeclarationExpression expr) {
        return new EnumValue(expr.members);
    }
    public Value visitMatchExpression(MatchExpression expr) {
        Value v = expr.expr.accept(this);
        for (MatchExpression.Case c : expr.cases) {
            Value w = c.pattern().accept(this);
            BooleanValue eq = v.eq(w);
            if (eq.value)
                return c.value().accept(this);
        } if (expr.other == null)
            throw new RuntimeException("Match expression did not match any case");
        return expr.other.accept(this);
    }
    public Value visitRangeExpression(RangeExpression expr) {
        Value v1 = expr.start.accept(this);
        Value v2 = expr.end.accept(this);
        if (!(v1 instanceof NumericValue n1))
            throw new RuntimeException("Range start must be numeric");
        if (!(v2 instanceof NumericValue n2))
            throw new RuntimeException("Range end must be numeric");
        if (n1.number > n2.number) {
            return new RangeValue(n1.number, n2.number, RangeValue.DESCENDING);
        }
        return new RangeValue(n1.number, n2.number, RangeValue.ASCENDING);
    }
    public Value visitDictionaryExpression(DictionaryExpression expr) {
        HashMap<Value, Value> map = new HashMap<>();
        for (DictionaryExpression.Pair pair : expr.pairs) {
            Value key = pair.key().accept(this);
            Value value = pair.value().accept(this);
            if (map.containsKey(key))
                throw new RuntimeException("Map contains same key twice");
            map.put(key, value);
        }
        return new DictionaryValue(map);
    }
    public Value visitSetExpression(SetExpression expr) {
        HashSet<Value> set = new HashSet<>();
        for (Expression e : expr.expr) {
            set.add(e.accept(this));
        }
        return new SetValue(set);
    }
    public Value visitForExpression(ForExpression expr) {
        return executeForLoop(expr, LoopEvaluationStrategy.ALL_ELEMENTS);
    }
    Value executeForLoop(ForExpression expr, LoopEvaluationStrategy strategy) {
        Value list = expr.list.accept(this);
        ArrayList<Value> values = null;
        if (strategy == LoopEvaluationStrategy.ALL_ELEMENTS)
            values = new ArrayList<>();
        scope = new Scope(scope);
        try {
            Value last = NullValue.INSTANCE, first = NullValue.INSTANCE;
            boolean isFirst = true;
            for (Value v : list) {
                scope.assignLocal(expr.variable.text(), v);
                Value val = expr.action.accept(this);
                if (isFirst) {
                    isFirst = false;
                    first = val;
                }
                last = val;
                if (strategy == LoopEvaluationStrategy.ALL_ELEMENTS)
                    values.add(val);
            }
            return switch (strategy) {
                case ALL_ELEMENTS -> new ListValue(values);
                case LAST_ONLY -> last;
                case FIRST_ONLY -> first;
                case NO_ELEMENTS -> NullValue.INSTANCE;
            };
        } finally {
            scope = scope.parent;
        }
    }
    Value executeRepeat(RepeatExpression expr, LoopEvaluationStrategy strategy) {
        try {
            ArrayList<Value> values = null;
            if (strategy == LoopEvaluationStrategy.ALL_ELEMENTS)
                values = new ArrayList<>();
            scope = new Scope(scope);
            Value last = NullValue.INSTANCE, first = NullValue.INSTANCE;
            boolean isFirst = true;
            for (int i = 0; i < expr.repeat; ++i) {
                Value v = expr.expr.accept(this);
                if (isFirst) {
                    isFirst = false;
                    first = v;
                }
                last = v;
                if (strategy == LoopEvaluationStrategy.ALL_ELEMENTS)
                    values.add(v);
            }
            return switch (strategy) {
                case ALL_ELEMENTS -> new ListValue(values);
                case LAST_ONLY -> last;
                case FIRST_ONLY -> first;
                case NO_ELEMENTS -> NullValue.INSTANCE;
            };
        } finally {
            scope = scope.parent;
        }
    }
    public Value visitRepeatExpression(RepeatExpression expr) {
        return executeRepeat(expr, LoopEvaluationStrategy.ALL_ELEMENTS);
    }
    public Value visitNumericBinaryExpression(NumericBinaryExpression expr) {
        double left = ((NumericValue) expr.left.accept(this)).number;
        double right = ((NumericValue) expr.right.accept(this)).number;
        return switch (expr.op.type()) {
            case PLUS -> NumericValue.of(left + right);
            case MINUS -> NumericValue.of(left - right);
            case STAR -> NumericValue.of(left * right);
            case SLASH -> NumericValue.of(left / right);
            case MOD -> NumericValue.of(left % right);
            case LTEQ -> BooleanValue.fromBoolean(left <= right);
            case GTEQ -> BooleanValue.fromBoolean(left >= right);
            case LT -> BooleanValue.fromBoolean(left < right);
            case GT -> BooleanValue.fromBoolean(left > right);
            case EQEQ -> BooleanValue.fromBoolean(left == right);
            case BANG_EQ -> BooleanValue.fromBoolean(left != right);
            default -> throw new RuntimeException("Invalid operator: " + expr.op);
        };
    }
    public Value visitNumericUnaryExpression(NumericUnaryExpression expr) {
        NumericValue v = (NumericValue) expr.expr.accept(this);
        double value = v.number;
        return switch (expr.op.type()) {
            case MINUS -> NumericValue.of(-value);
            case QUESTION -> BooleanValue.fromBoolean(value != 0);
            case HASH -> v;
            case ARTIFICIAL_NBOOL -> BooleanValue.fromBoolean(value == 0);
            default -> throw new RuntimeException("Invalid operator: " + expr.op);
        };
    }
}
