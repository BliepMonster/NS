package main;

import main.expr.*;
import main.interpreter.values.InvalidOperationException;
import main.interpreter.values.builtins.*;
import main.interpreter.values.natives.IteratorValue;
import main.interpreter.values.natives.RangeIterator;
import main.interpreter.values.natives.StepRange;

import java.util.*;

enum VariableType {
    NUMBER, BOOLEAN, STRING, ITERATOR, LIST, SET,
    DICTIONARY, CLASS, VECTOR, RANGE, FILE, NULL, FUNCTION,
    STEP_LITERAL,
    UNKNOWN
}

public class Optimizer implements StatementVisitor<Statement>, ExpressionVisitor<Expression> {
    private final ArrayList<Statement> statements;
    public Optimizer(ArrayList<Statement> statements) {
        this.statements = statements;
    }
    public ArrayList<Statement> optimize() {
        ArrayList<Statement> optimizedStatements = new ArrayList<>();
        for (Statement statement : statements) {
            optimizedStatements.add(statement.accept(this));
        }
        return optimizedStatements;
    }
    public Statement visitExpressionStatement(ExpressionStatement stmt) {
        return new ExpressionStatement(stmt.expr.accept(this));
    }
    public Expression visitReturnExpression(ReturnExpression expr) {
        return new ReturnExpression(expr.value.accept(this));
    }
    public Expression visitVariableLookupExpression(VariableLookupExpression expr) {
        return expr;
    }
    public Expression visitThisExpression(ThisExpression expr) {
        return expr;
    }
    public Expression visitAssignmentExpression(AssignmentExpression expr) {
        return new AssignmentExpression(expr.target, expr.value.accept(this), expr.token);
    }
    // operator overloading prevents us from going any deeper
    public Expression visitBinaryExpression(BinaryExpression expr) {
        Expression left = expr.left.accept(this);
        Expression right = expr.right.accept(this);
        if (left instanceof LiteralExpression lv && right instanceof LiteralExpression rv) {
            return new LiteralExpression(switch (expr.op.type()) {
                case EQEQ -> lv.value.eq(rv.value);
                case GT -> lv.value.gt(rv.value);
                case LT -> lv.value.lt(rv.value);
                case GTEQ -> lv.value.gte(rv.value);
                case LTEQ -> lv.value.lte(rv.value);
                case PLUS -> lv.value.add(rv.value);
                case MINUS -> lv.value.sub(rv.value);
                case STAR -> lv.value.mul(rv.value);
                case SLASH -> lv.value.div(rv.value);
                case MOD -> lv.value.mod(rv.value);
                case PIPE -> lv.value.merge(rv.value);
                case OR -> {
                    if (!(lv.value instanceof BooleanValue lb))
                        throw new RuntimeException("Cannot OR non-boolean value");
                    if (!(rv.value instanceof BooleanValue rb))
                        throw new RuntimeException("Cannot OR non-boolean value");
                    yield BooleanValue.fromBoolean(lb.value || rb.value);
                }
                case AND -> {
                    if (!(lv.value instanceof BooleanValue lb))
                        throw new RuntimeException("Cannot AND non-boolean value");
                    if (!(rv.value instanceof BooleanValue rb))
                        throw new RuntimeException("Cannot AND non-boolean value");
                    yield BooleanValue.fromBoolean(lb.value && rb.value);
                }
                case IN -> rv.value.contains(lv.value);
                // SHOULD NEVER HAPPEN
                default -> throw new RuntimeException("Invalid operator");
            });
        } else if (left instanceof LiteralExpression lv) {
            return switch (expr.op.type()) {
                case OR -> {
                    if (!(lv.value instanceof BooleanValue lb))
                        throw new RuntimeException("Cannot OR non-boolean value");
                    if (!lb.value)
                        yield right;
                    yield left;
                }
                case AND -> {
                    if (!(lv.value instanceof BooleanValue lb))
                        throw new RuntimeException("Cannot AND non-boolean value");
                    if (lb.value)
                        yield right;
                    yield left;
                }
                default -> expr;
            };
        } else if (right instanceof LiteralExpression rv) {
            return switch (expr.op.type()) {
                case OR -> {
                    if (!(rv.value instanceof BooleanValue rb))
                        throw new RuntimeException("Cannot OR non-boolean value");
                    if (!rb.value)
                        yield left;
                    yield new BinaryExpression(left, expr.op, rv);
                }
                case AND -> {
                    if (!(rv.value instanceof BooleanValue rb))
                        throw new RuntimeException("Cannot OR non-boolean value");
                    if (rb.value)
                        yield left;
                    yield new BinaryExpression(left, expr.op, rv);
                }
                default -> expr;
            };
        } if (expr.op.type() == TokenType.EQEQ) {
            VariableType ltype = fromType(left);
            if (ltype == VariableType.UNKNOWN)
                return new BinaryExpression(left, expr.op, right);
            VariableType rtype = fromType(right);
            if (rtype == VariableType.UNKNOWN)
                return new BinaryExpression(left, expr.op, right);
            if (ltype != rtype)
                return new LiteralExpression(BooleanValue.FALSE);
        } if (expr.op.type() == TokenType.BANG_EQ) {
            VariableType ltype = fromType(left);
            if (ltype == VariableType.UNKNOWN)
                return new BinaryExpression(left, expr.op, right);
            VariableType rtype = fromType(right);
            if (rtype == VariableType.UNKNOWN)
                return new BinaryExpression(left, expr.op, right);
            if (ltype != rtype)
                return new LiteralExpression(BooleanValue.TRUE);
        }
        return new BinaryExpression(left, expr.op, right);
    }
    public Expression visitUnaryExpression(UnaryExpression expr) {
        Expression expression = expr.expr.accept(this);
        switch (expr.op.type()) {
            case QUESTION -> {
                if (!(expression instanceof LiteralExpression literal))
                    if (fromType(expression) == VariableType.BOOLEAN)
                        return expression;
                    else
                        return new UnaryExpression(expr.op, expression);
                return new LiteralExpression(literal.value.isTruthy());
            }
            case HASH -> {
                if (!(expression instanceof LiteralExpression literal))
                    if (fromType(expression) == VariableType.NUMBER)
                        return expression;
                    else
                        return new UnaryExpression(expr.op, expression);
                return new LiteralExpression(literal.value.toNumber());
            }
            case MINUS -> {
                if (expression instanceof UnaryExpression unary && unary.op.type() == TokenType.MINUS)
                    return unary.expr;
                else if (expression instanceof LiteralExpression literal)
                    return new LiteralExpression(literal.value.neg());
                return new UnaryExpression(expr.op, expression);
            }
            case BANG -> {
                if (expression instanceof LiteralExpression lit) {
                    return new LiteralExpression(lit.value.inv());
                } else if (expression instanceof UnaryExpression ue && ue.op.type() == TokenType.BANG) {
                    return ue.expr;
                }
            }
        }
        return new UnaryExpression(expr.op, expression);
    }
    public Expression visitFunctionCallExpression(FunctionCallExpression expr) {
        Expression function = expr.function.accept(this);
        ArrayList<Expression> arguments = new ArrayList<>();
        for (Expression argument : expr.args) {
            arguments.add(argument.accept(this));
        }
        return new FunctionCallExpression(function, arguments);
    }
    public Expression visitListExpression(ListExpression expr) {
        ArrayList<Expression> elements = new ArrayList<>();
        for (Expression element : expr.elements) {
            elements.add(element.accept(this));
        }
        ArrayList<Value> literals = new ArrayList<>();
        for (Expression element : elements) {
            if (!(element instanceof LiteralExpression le))
                return new ListExpression(elements);
            literals.add(le.value);
        }
        return new LiteralExpression(new ListValue(literals));
    }
    public Expression visitMemberExpression(MemberExpression expr) {
        return new MemberExpression(expr.expr.accept(this), expr.member);
    }
    public Expression visitLiteralExpression(LiteralExpression expr) {
        return expr;
    }
    public Expression visitIndexExpression(IndexExpression expr) {
        Expression left = expr.expr.accept(this);
        Expression index = expr.index.accept(this);
        if (left instanceof LiteralExpression literal && index instanceof LiteralExpression indexLiteral) {
            return new LiteralExpression(literal.value.index(indexLiteral.value));
        }
        return new IndexExpression(left, index);
    }
    public Expression visitVectorExpression(VectorExpression expr) {
        ArrayList<Expression> elements = new ArrayList<>();
        for (Expression element : expr.elements) {
            elements.add(element.accept(this));
        }
        ArrayList<NumericValue> literals = new ArrayList<>();
        for (Expression element : elements) {
            if (!(element instanceof LiteralExpression le))
                return new VectorExpression(elements);
            if (!(le.value instanceof NumericValue num))
                throw new RuntimeException("Cannot index non-numeric value");
            literals.add(num);
        }
        return new LiteralExpression(new VectorValue(literals.toArray(new NumericValue[]{})));
    }
    public Expression visitLoopExpression(LoopExpression expr) {
        return new LoopExpression(expr.cond.accept(this), expr.body.accept(this));
    }
    public Expression visitBlockExpression(BlockExpression expr) {
        ArrayList<Statement> statements = new ArrayList<>();
        for (Statement statement : expr.statements) {
            Statement optimizedStatement = statement.accept(this);
            statements.add(optimizedStatement);
            if (optimizedStatement instanceof ExpressionStatement est && est.expr instanceof ReturnExpression)
                break;
        }
        return new BlockExpression(statements);
    }
    // we can't make it a literal due to closures
    public Expression visitFunctionDeclarationExpression(FunctionDeclarationExpression expr) {
        return new FunctionDeclarationExpression(expr.args, expr.body.accept(this));
    }
    public Expression visitClassDeclarationExpression(ClassDeclarationExpression expr) {
        var map = new HashMap<String, Expression>();
        for (Map.Entry<String, Expression> entry : expr.members.entrySet()) {
            map.put(entry.getKey(), entry.getValue().accept(this));
        }
        if (expr.superclass == null) {
            return new LiteralExpression(new InterpretedClassValue(map));
        }
        Expression superclass = expr.superclass.accept(this);
        if (superclass instanceof LiteralExpression lit) {
            if (lit.value instanceof InterpretedClassValue icv) {
                map.putAll(icv.fields);
                return new LiteralExpression(new InterpretedClassValue(map));
            } throw new RuntimeException("Cannot inherit from non-class");
        }
        return new ClassDeclarationExpression(map, superclass);
    }
    public Expression visitNativeFunctionCallExpression(NativeFunctionCallExpression expr) {
        ArrayList<Expression> arguments = new ArrayList<>();
        for (Expression argument : expr.args) {
            arguments.add(argument.accept(this));
        }
        // special case: range iterator can be calculated upfront
        switch (expr.name) {
            case "step" -> {
                if (arguments.size() != 2)
                    throw new RuntimeException("step() takes 2 arguments");
                Expression start = arguments.getFirst();
                Expression end = arguments.getLast();
                if (!(start instanceof LiteralExpression rangeLit && end instanceof LiteralExpression numLit))
                    return new NativeFunctionCallExpression(expr.name, arguments);
                Value rnv = rangeLit.value;
                if (!(rnv instanceof RangeValue range))
                    throw new RuntimeException("step() takes range as first argument");
                Value nnv = numLit.value;
                if (!(nnv instanceof NumericValue num))
                    throw new RuntimeException("step() takes number as second argument");
                if (num.number <= 0)
                    throw new InvalidOperationException("step must be positive");
                return new LiteralExpression(new RangeIterator((new StepRange(range, num.number))));
            }
            case "iter" -> {
                if (arguments.size() != 1)
                    throw new RuntimeException("iter() takes 1 argument");
                Expression arg = arguments.getFirst();
                if (arg instanceof LiteralExpression lit) {
                    return new LiteralExpression(new IteratorValue(lit.value.iterator()));
                }
            }
            case "str" -> {
                if (arguments.size() != 1)
                    throw new RuntimeException("str() takes 1 argument");
                Expression arg = arguments.getFirst();
                if (arg instanceof LiteralExpression lit) {
                    return new LiteralExpression(new StringValue(lit.value.toString()));
                }
            }
        }
        return new NativeFunctionCallExpression(expr.name, arguments);
    }
    public Expression visitTernaryExpression(TernaryExpression expr) {
        Expression cond = expr.condition.accept(this);
        if (cond instanceof LiteralExpression lit) {
            if (!(lit.value instanceof BooleanValue bool))
                throw new RuntimeException("Cannot use non-boolean value in ternary");
            if (bool.value)
                return expr.trueExpr.accept(this);
            else
                return expr.falseExpr.accept(this);
        }
        Expression trueExpr = expr.trueExpr.accept(this);
        Expression falseExpr = expr.falseExpr.accept(this);
        if (trueExpr instanceof ReturnExpression re && falseExpr instanceof ReturnExpression fe)
            return new ReturnExpression(new TernaryExpression(cond, re.value, fe.value));
        return new TernaryExpression(cond, trueExpr, falseExpr);
    }
    public Expression visitCatchExpression(CatchExpression expr) {
        Expression left = expr.expr.accept(this);
        // no crashes on a literal
        if (left instanceof LiteralExpression) {
            return left;
        }
        return new CatchExpression(left, expr.fallback.accept(this));
    }
    public Expression visitEnumDeclarationExpression(EnumDeclarationExpression expr) {
        return expr;
    }
    public Expression visitMatchExpression(MatchExpression expr) {
        Expression cond = expr.expr.accept(this);
        VariableType type = fromType(cond);

        ArrayList<MatchExpression.Case> cases = new ArrayList<>();
        for (MatchExpression.Case arm : expr.cases) {
            Expression condition = arm.pattern().accept(this);
            Expression value = arm.value().accept(this);
            boolean add = true;
            if (condition instanceof LiteralExpression lit && cond instanceof LiteralExpression lcond) {
                // equals() uses eq()
                if (lit.value.equals(lcond.value))
                    return value;
                else add = false;
            }
            VariableType t = fromType(condition);
            if (t != VariableType.UNKNOWN && type != VariableType.UNKNOWN) {
                if (t != type)
                    add = false;
            }
            if (add)
                cases.add(new MatchExpression.Case(condition, value));
        }
        if (expr.other != null) {
            Expression other = expr.other.accept(this);
            if (cond instanceof LiteralExpression) {
                // safe to optimize, no side effects
                return other;
            }
            return new MatchExpression(cond, cases, other);
        } else return new MatchExpression(cond, cases, null);
    }
    public Expression visitRangeExpression(RangeExpression expr) {
        Expression left = expr.start.accept(this);
        Expression right = expr.end.accept(this);
        if (left instanceof LiteralExpression l1 && right instanceof LiteralExpression l2) {
            if (!(l1.value instanceof NumericValue n1) || !(l2.value instanceof NumericValue n2))
                throw new RuntimeException("Cannot use non-numeric value in range");
            boolean order = n1.number <= n2.number;
            return new LiteralExpression(new RangeValue(n1.number, n2.number, order));
        }
        return new RangeExpression(left, right);
    }
    public Expression visitDictionaryExpression(DictionaryExpression expr) {
        HashMap<Value, Value> map = new HashMap<>();
        ArrayList<DictionaryExpression.Pair> keys = new ArrayList<>();
        boolean literal = true;
        for (DictionaryExpression.Pair pair : expr.pairs) {
            Expression key = pair.key().accept(this);
            Expression value = pair.value().accept(this);
            keys.add(new DictionaryExpression.Pair(key, value));
            if (key instanceof LiteralExpression lk && value instanceof LiteralExpression lv)
                map.put(lk.value, lv.value);
            else literal = false;
        }
        if (literal)
            return new LiteralExpression(new DictionaryValue(map));
        return new DictionaryExpression(keys);
    }
    public Expression visitSetExpression(SetExpression expr) {
        Set<Expression> elements = new HashSet<>();
        for (Expression element : expr.expr) {
            elements.add(element.accept(this));
        }
        HashSet<Value> literals = new HashSet<>();
        for (Expression element : elements) {
            if (!(element instanceof LiteralExpression le))
                return new SetExpression(elements);
            literals.add(le.value);
        }
        return new LiteralExpression(new SetValue(literals));
    }
    // TODO: add for loop unrolling for short lists & sets
    public Expression visitForExpression(ForExpression expr) {
        return new ForExpression(expr.action.accept(this), expr.list.accept(this), expr.variable);
    }
    VariableType fromType(Expression cond) {
        if (cond instanceof LiteralExpression lit) {
            if (lit.value instanceof NumericValue)
                return VariableType.NUMBER;
            else if (lit.value instanceof BooleanValue)
                return VariableType.BOOLEAN;
            else if (lit.value instanceof StringValue)
                return VariableType.STRING;
            else if (lit.value instanceof InterpretedClassValue)
                return VariableType.CLASS;
            else if (lit.value instanceof ListValue)
                return VariableType.LIST;
            else if (lit.value instanceof SetValue)
                return VariableType.SET;
            else if (lit.value instanceof DictionaryValue)
                return VariableType.DICTIONARY;
            else if (lit.value instanceof RangeValue)
                return VariableType.RANGE;
            else if (lit.value instanceof VectorValue)
                return VariableType.VECTOR;
            else if (lit.value == NullValue.INSTANCE)
                return VariableType.NULL;
            else if (lit.value instanceof RangeIterator)
                return VariableType.STEP_LITERAL;
            else return VariableType.UNKNOWN;
        } else if (cond instanceof NativeFunctionCallExpression nfce) {
            return switch (nfce.name) {
                case "iter" -> VariableType.ITERATOR;
                case "str", "input" -> VariableType.STRING;
                case "openFileReadingHandle" -> VariableType.FILE;
                case "clock" -> VariableType.NUMBER;
                case "print", "println", "ignoreLoopResult" -> VariableType.NULL;
                case "step" -> VariableType.STEP_LITERAL;
                default -> VariableType.UNKNOWN;
            };
        } else if (cond instanceof TernaryExpression te) {
            VariableType tr = fromType(te.trueExpr);
            if (tr == VariableType.UNKNOWN)
                return tr;
            VariableType fr = fromType(te.falseExpr);
            if (fr == VariableType.UNKNOWN)
                return fr;
            return tr == fr ? tr : VariableType.UNKNOWN;
        } else if (cond instanceof ListExpression)
            return VariableType.LIST;
        else if (cond instanceof SetExpression)
            return VariableType.SET;
        else if (cond instanceof DictionaryExpression)
            return VariableType.DICTIONARY;
        else if (cond instanceof ClassDeclarationExpression)
            return VariableType.CLASS;
        else if (cond instanceof RangeExpression)
            return VariableType.RANGE;
        else if (cond instanceof VectorExpression)
            return VariableType.VECTOR;
        else if (cond instanceof AssignmentExpression assignment) {
            return fromType(assignment.value);
        } else if (cond instanceof IndexExpression ie) {
            if (fromType(ie.expr) == VariableType.STRING) {
                return VariableType.STRING;
            }
        } else if (cond instanceof BinaryExpression be) {
            return switch (be.op.type()) {
                case IN, OR, AND, EQEQ, BANG_EQ, GT, LT, GTEQ, LTEQ -> VariableType.BOOLEAN;
                case PIPE -> {
                    if (fromType(be.left) == VariableType.LIST)
                        yield VariableType.LIST;
                    else if (fromType(be.left) == VariableType.SET)
                        yield VariableType.SET;
                    else if (fromType(be.left) == VariableType.DICTIONARY)
                        yield VariableType.DICTIONARY;
                    else yield VariableType.UNKNOWN;
                }
                case SLASH -> {
                    if (fromType(be.left) == VariableType.NUMBER)
                        yield VariableType.NUMBER;
                    else if (fromType(be.left) == VariableType.VECTOR)
                        yield VariableType.VECTOR;
                    else if (fromType(be.left) == VariableType.SET)
                        yield VariableType.SET;
                    else yield VariableType.UNKNOWN;
                }
                default -> VariableType.UNKNOWN;
            };
        } else if (cond instanceof UnaryExpression ue) {
            return switch (ue.op.type()) {
                case MINUS -> {
                    if (fromType(ue.expr) == VariableType.NUMBER)
                        yield VariableType.NUMBER;
                    else yield VariableType.UNKNOWN;
                }
                case BANG -> {
                    if (fromType(ue.expr) == VariableType.BOOLEAN)
                        yield VariableType.BOOLEAN;
                    else yield VariableType.UNKNOWN;
                }
                case QUESTION -> VariableType.BOOLEAN;
                case HASH -> {
                    if (fromType(ue.expr) != VariableType.NUMBER)
                        yield VariableType.NUMBER;
                    else yield VariableType.UNKNOWN;
                }
                default -> VariableType.UNKNOWN;
            };
        } else if (cond instanceof ForExpression || cond instanceof LoopExpression) {
            return VariableType.LIST;
        } else if (cond instanceof FunctionDeclarationExpression) {
            return VariableType.FUNCTION;
        }
        return VariableType.UNKNOWN;
    }
}