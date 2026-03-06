package main.interpreter;

import main.*;
import main.expr.*;
import main.interpreter.values.*;

import java.util.ArrayList;
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
        NullValue.init(this);
        BooleanValue.init(this);
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
            } else return expr.right.accept(this);
        } else if (t == TokenType.AND) {
            Value left = expr.left.accept(this);
            if (!(left instanceof BooleanValue bv)) {
                throw new RuntimeException("Expected boolean value, got " + left);
            } if (!bv.value) {
                return left;
            } else return expr.right.accept(this);
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
            default -> throw new RuntimeException("Invalid operator: " + expr.op);
        };
    }
    public Value visitUnaryExpression(UnaryExpression expr) {
        return switch (expr.op.type()) {
            case MINUS -> expr.expr.accept(this).neg();
            case BANG -> expr.expr.accept(this).inv();
            case HASH -> expr.expr.accept(this).toNumber();
            case QUESTION -> expr.expr.accept(this).isTruthy();
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
        return switch (expr.value) {
            case String s -> new StringValue(s, this);
            case Boolean b -> BooleanValue.fromBoolean(b);
            case Double d -> new NumericValue(d, this);
            case null -> NullValue.INSTANCE;
            default -> throw new RuntimeException("Invalid literal: " + expr.value);
        };
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
        return new VectorValue(values, this);
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
                    case ALL_ELEMENTS -> new ListValue(values, this);
                };
            }
            Value res = expr.body.accept(this);
            if (isFirst) {
                first = res;
                isFirst = false;
            }
            values.add(res);
            last = res;
        }
    }
    public Value visitFunctionDeclarationExpression(FunctionDeclarationExpression expr) {
        ArrayList<String> params = new ArrayList<>();
        for (Token t : expr.args) {
            params.add(t.text());
        }
        return new InterpretedFunctionValue(params, expr.body, this, scope);
    }
    public Value visitClassDeclarationExpression(ClassDeclarationExpression expr) {
        if (expr.superclass != null) {
            Value superClass = expr.superclass.accept(this);
            if (!(superClass instanceof InterpretedClassValue sc))
                throw new RuntimeException("Superclass must be a class");
            expr.members.putAll(sc.fields);
        }
        return new InterpretedClassValue(expr.members, this);
    }
    public Value visitBlockExpression(BlockExpression expr) {
        try {
            for (Statement s : expr.statements) {
                s.accept(this);
            }
            return NullValue.INSTANCE;
        } catch (Return r) {
            return r.value;
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
        if (expr.name.equals("ignoreWhileResult")) {
            if (expr.args.size() != 1) {
                throw new RuntimeException("ignoreWhileResult expects 1 argument");
            }
            Expression cond = expr.args.getFirst();
            if (!(cond instanceof LoopExpression loop)) {
                throw new RuntimeException("ignoreWhileResult expects a loop expression");
            }
            return evaluateLoop(loop, LoopEvaluationStrategy.NO_ELEMENTS);
        }
        switch (expr.name) {
            case "print" -> {
                Value v = expr.args.getFirst().accept(this);
                System.out.print(v.toString());
            }
            case "input" -> {
                System.out.println();
                System.out.print("> ");
                String input = System.console().readLine();
                return new StringValue(input, this);
            }
            case "println" -> {
                Value v = expr.args.getFirst().accept(this);
                System.out.println(v.toString());
            }
            case "str" -> {
                return new StringValue(expr.args.getFirst().accept(this).toString(), this);
            }
        }
        return NullValue.INSTANCE;
    }
    public Value visitListExpression(ListExpression expr) {
        ArrayList<Value> values = new ArrayList<>();
        for (Expression e : expr.elements) {
            values.add(e.accept(this));
        }
        return new ListValue(values, this);
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
        } catch (InvalidOperationException e) {
            return expr.fallback.accept(this);
        }
    }
    public Value visitEnumDeclarationExpression(EnumDeclarationExpression expr) {
        return new EnumValue(expr.members, this);
    }
}
