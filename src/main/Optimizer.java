package main;

import main.expr.*;
import main.interpreter.values.InvalidOperationException;
import main.interpreter.values.builtins.*;
import main.interpreter.values.natives.*;
import utils.ContainsChecker;
import utils.Replacer;
import utils.SideEffectAnalyzer;
import utils.VariableLister;

import java.util.*;


/**
 * The Optimizer class will try to get code to its most optimal form. <br><br>
 * It will not remove or introduce side effects, and it will not add bugs.
 * However, it assumes code works. Some optimizations may hide or obfuscate bugs, or even accidentally remove them completely.
 * <br><br>Take for example:
 * <br>{@code
 * x = 0+obj;
 * }<br>
 * Here, it will try to turn it into {@code x = obj}. If obj is not a number, these are not equivalent. However, if obj is not a number, the code would've crashed anyway.
 */
public class Optimizer implements StatementVisitor<Statement>, ExpressionVisitor<Expression> {
    private final ArrayList<Statement> statements;
    private static final SideEffectAnalyzer analyzer = new SideEffectAnalyzer();
    public enum VariableType {
        NUMBER, BOOLEAN, STRING, ITERATOR, LIST, SET,
        DICTIONARY, CLASS, VECTOR, RANGE, NULL, FUNCTION,
        STEP_LITERAL, ENUM, BIG_NUMBER,
        UNKNOWN
    }

    public Optimizer(ArrayList<Statement> statements) {
        this.statements = statements;
    }
    public ArrayList<Statement> optimize() {
        ArrayList<Statement> optimizedStatements = new ArrayList<>();
        for (Statement statement : statements) {
            Statement optimizedStatement = statement.accept(this);
            if (optimizedStatement != null)
                optimizedStatements.add(optimizedStatement);
        }
        return optimizedStatements;
    }
    public Statement visitExpressionStatement(ExpressionStatement stmt) {
        Expression expr = stmt.expr.accept(this);
        if (!expr.accept(analyzer))
            return null;
        return new ExpressionStatement(expr);
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
    // operator overloading prevents us from optimizing the right
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
                case PLUS -> {
                    if (lv.value instanceof NumericValue lvnum) {
                        if (lvnum.number == 0)
                            yield right;
                    }
                    else if (lv.value instanceof StringValue lvstr) {
                        if (lvstr.getValue().isEmpty())
                            // replace by str() call
                            yield (new NativeFunctionCallExpression("str", new ArrayList<>(List.of(right)))).accept(this);
                    }
                    yield new BinaryExpression(lv, expr.op, right);
                }
                case STAR -> {
                    if (lv.value instanceof NumericValue lvnum) {
                        if (lvnum.number == 1)
                            yield right;
                        else if (lvnum.number == 0 && right.accept(analyzer))
                            yield lv;
                    }
                    yield new BinaryExpression(lv, expr.op, right);
                }
                default ->  new BinaryExpression(left, expr.op, right);
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
                default -> new BinaryExpression(left, expr.op, rv);
            };
        } if (expr.op.type() == TokenType.EQEQ) {
            VariableType ltype = getType(left);
            if (ltype == VariableType.UNKNOWN)
                return new BinaryExpression(left, expr.op, right);
            VariableType rtype = getType(right);
            if (rtype == VariableType.UNKNOWN)
                return new BinaryExpression(left, expr.op, right);
            if (ltype != rtype)
                return new LiteralExpression(BooleanValue.FALSE);
        } if (expr.op.type() == TokenType.BANG_EQ) {
            VariableType ltype = getType(left);
            if (ltype == VariableType.UNKNOWN)
                return new BinaryExpression(left, expr.op, right);
            VariableType rtype = getType(right);
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
                    if (getType(expression) == VariableType.BOOLEAN)
                        return expression;
                    else
                        return new UnaryExpression(expr.op, expression);
                return new LiteralExpression(literal.value.isTruthy());
            }
            case HASH -> {
                if (!(expression instanceof LiteralExpression literal))
                    if (getType(expression) == VariableType.NUMBER)
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
        Expression owner = expr.expr.accept(this);
        if (owner instanceof LiteralExpression lit) {
            return new LiteralExpression(lit.value.getMember(expr.member));
        }
        return new MemberExpression(owner, expr.member);
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
        Expression cond = expr.cond.accept(this);
        if (cond instanceof LiteralExpression lit) {
            if (!(lit.value instanceof BooleanValue bv))
                throw new RuntimeException("Cannot use non-boolean value in loop");
            if (!bv.value)
                return new LiteralExpression(new ListValue(new ArrayList<>()));
            // otherwise: infinite loop -> normal case: true + expr.optimize
        }
        return new LoopExpression(cond, expr.body.accept(this));
    }
    public Expression visitBlockExpression(BlockExpression expr) {
        ArrayList<Statement> statements = new ArrayList<>();
        for (Statement statement : expr.statements) {
            Statement optimizedStatement = statement.accept(this);
            if (optimizedStatement == null)
                continue;
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
                } else if (getType(arg) == VariableType.STRING)
                    return arg;
            }
            case "print" -> {
                ArrayList<Value> vals = new ArrayList<>();
                for (Expression argument : arguments) {
                    if (!(argument instanceof LiteralExpression lit))
                        return new NativeFunctionCallExpression(expr.name, arguments);
                    vals.add(lit.value);
                }
                StringBuilder sb = new StringBuilder();
                for (Value val : vals) {
                    sb.append(val.toString());
                    sb.append(" ");
                }
                return new NativeFunctionCallExpression("print", List.of(new LiteralExpression(new StringValue(sb.toString()))));
            }
            case "println" -> {
                ArrayList<Value> vals = new ArrayList<>();
                for (Expression argument : arguments) {
                    if (!(argument instanceof LiteralExpression lit))
                        return new NativeFunctionCallExpression(expr.name, arguments);
                    vals.add(lit.value);
                }
                StringBuilder sb = new StringBuilder();
                for (Value val : vals) {
                    sb.append(val.toString());
                    sb.append(" ");
                }
                sb.append('\n');
                return new NativeFunctionCallExpression("print", List.of(new LiteralExpression(new StringValue(sb.toString()))));
            }
            case "len" -> {
                if (arguments.size() != 1)
                    throw new RuntimeException("len() takes 1 argument");
                Expression arg = arguments.getFirst();
                if (arg instanceof LiteralExpression lit) {
                    return new LiteralExpression(lit.value.length());
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
        return new LiteralExpression(new EnumValue(expr.members));
    }
    public Expression visitMatchExpression(MatchExpression expr) {
        Expression cond = expr.expr.accept(this);
        VariableType type = getType(cond);

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
            VariableType t = getType(condition);
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
        List<Expression> elements = new ArrayList<>();
        for (Expression element : expr.expr) {
            elements.add(element.accept(this));
        }
        List<Value> literals = new ArrayList<>();
        for (Expression element : elements) {
            if (!(element instanceof LiteralExpression le))
                return new SetExpression(elements);
            literals.add(le.value);
        }
        return new LiteralExpression(new SetValue(new HashSet<>(literals)));
    }
    public Expression visitForExpression(ForExpression expr) {
        Expression iterator = expr.list.accept(this);
        Expression action = expr.action.accept(this);
        if (iterator instanceof LiteralExpression lit) {
            switch (lit.value) {
                case RangeIterator range -> {
                    if (!action.accept(new ContainsChecker(expr.variable.text()))) {
                        return new RepeatExpression(action, range.size()).accept(this);
                    }
                }
                case ListValue list -> {
                    if (!action.accept(new ContainsChecker(expr.variable.text()))) {
                        // we can be sure list.length() returns an integer
                        return new RepeatExpression(action, (int) ((NumericValue) list.length()).number).accept(this);
                    }
                    if (action.accept(analyzer))
                        return new ForExpression(action, iterator, expr.variable);
                    HashSet<String> vars = action.accept(new VariableLister());
                    if (vars.size() != 1) {
                        return new ForExpression(action, iterator, expr.variable);
                    }
                    String var = vars.iterator().next();
                    if (!var.equals(expr.variable.text())) {
                        // should've normally been caught by the contains checker
                        return new ForExpression(action, iterator, expr.variable);
                    }
                    ArrayList<Expression> bodies = new ArrayList<>();
                    for (Value value : list) {
                        Expression literal = new LiteralExpression(value);
                        Expression body = action.accept(new Replacer(literal, var));
                        bodies.add(body);
                    }
                    return new ListExpression(bodies).accept(this);
                }
                case SetValue set -> {
                    if (!action.accept(new ContainsChecker(expr.variable.text()))) {
                        // we can be sure set.length() returns an integer
                        return new RepeatExpression(action, (int) ((NumericValue) set.length()).number).accept(this);
                    }
                    if (action.accept(analyzer))
                        return new ForExpression(action, iterator, expr.variable);
                    HashSet<String> vars = action.accept(new VariableLister());
                    if (vars.size() != 1) {
                        return new ForExpression(action, iterator, expr.variable);
                    }
                    String var = vars.iterator().next();
                    if (!var.equals(expr.variable.text())) {
                        // should've normally been caught by the contains checker
                        return new ForExpression(action, iterator, expr.variable);
                    }
                    ArrayList<Expression> bodies = new ArrayList<>();
                    for (Value value : set) {
                        Expression literal = new LiteralExpression(value);
                        Expression body = action.accept(new Replacer(literal, var));
                        bodies.add(body);
                    }
                    return new SetExpression(bodies).accept(this);
                }
                case null, default -> throw new RuntimeException("Cannot use non-list/set/range iterator in for loop");
            }
        }
        return new ForExpression(action , iterator, expr.variable);
    }
    public static VariableType getType(Expression cond) {
        if (cond.accept(analyzer))
            return VariableType.UNKNOWN;
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
            else if (lit.value instanceof BigNumberValue)
                return VariableType.BIG_NUMBER;
            else if (lit.value instanceof RangeValue)
                return VariableType.RANGE;
            else if (lit.value instanceof VectorValue)
                return VariableType.VECTOR;
            else if (lit.value == NullValue.INSTANCE)
                return VariableType.NULL;
            else if (lit.value instanceof EnumValue)
                return VariableType.ENUM;
            else if (lit.value instanceof RangeIterator)
                return VariableType.STEP_LITERAL;
            else return VariableType.UNKNOWN;
        } else if (cond instanceof NativeFunctionCallExpression nfce) {
            return switch (nfce.name) {
                case "iter" -> VariableType.ITERATOR;
                case "str" -> VariableType.STRING;
                case "clock" -> VariableType.NUMBER;
                case "ignoreLoopResult" -> VariableType.NULL;
                case "step" -> VariableType.STEP_LITERAL;
                case "bignum" -> VariableType.BIG_NUMBER;
                default -> VariableType.UNKNOWN;
            };
        } else if (cond instanceof TernaryExpression te) {
            VariableType tr = getType(te.trueExpr);
            if (tr == VariableType.UNKNOWN)
                return tr;
            VariableType fr = getType(te.falseExpr);
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
        else if (cond instanceof EnumDeclarationExpression)
            return VariableType.ENUM;
        else if (cond instanceof AssignmentExpression assignment) {
            return getType(assignment.value);
        } else if (cond instanceof IndexExpression ie) {
            if (getType(ie.expr) == VariableType.STRING) {
                return VariableType.STRING;
            }
        } else if (cond instanceof BinaryExpression be) {
            return switch (be.op.type()) {
                case IN, OR, AND, EQEQ, BANG_EQ, GT, LT, GTEQ, LTEQ -> VariableType.BOOLEAN;
                case PIPE -> {
                    if (getType(be.left) == VariableType.LIST)
                        yield VariableType.LIST;
                    else if (getType(be.left) == VariableType.SET)
                        yield VariableType.SET;
                    else if (getType(be.left) == VariableType.DICTIONARY)
                        yield VariableType.DICTIONARY;
                    else yield VariableType.UNKNOWN;
                }
                case PLUS, MOD, STAR, MINUS -> {
                    if (getType(be.left) == VariableType.NUMBER)
                        yield VariableType.NUMBER;
                    else if (getType(be.left) == VariableType.BIG_NUMBER)
                        yield VariableType.BIG_NUMBER;
                    else if (getType(be.left) == VariableType.VECTOR)
                        yield VariableType.VECTOR;
                    else if (getType(be.left) == VariableType.FUNCTION)
                        yield VariableType.FUNCTION;
                    yield VariableType.UNKNOWN;
                }
                case SLASH -> {
                    if (getType(be.left) == VariableType.NUMBER)
                        yield VariableType.NUMBER;
                    else if (getType(be.left) == VariableType.BIG_NUMBER)
                        yield VariableType.BIG_NUMBER;
                    else if (getType(be.left) == VariableType.VECTOR)
                        yield VariableType.VECTOR;
                    else if (getType(be.left) == VariableType.SET)
                        yield VariableType.SET;
                    else if (getType(be.left) == VariableType.FUNCTION)
                        yield VariableType.FUNCTION;
                    else yield VariableType.UNKNOWN;
                }
                default -> VariableType.UNKNOWN;
            };
        } else if (cond instanceof UnaryExpression ue) {
            return switch (ue.op.type()) {
                case MINUS -> {
                    if (getType(ue.expr) == VariableType.NUMBER)
                        yield VariableType.NUMBER;
                    else if (getType(ue.expr) == VariableType.BIG_NUMBER)
                        yield VariableType.BIG_NUMBER;
                    else yield VariableType.UNKNOWN;
                }
                case BANG -> {
                    if (getType(ue.expr) == VariableType.BOOLEAN)
                        yield VariableType.BOOLEAN;
                    else yield VariableType.UNKNOWN;
                }
                case QUESTION -> VariableType.BOOLEAN;
                case HASH -> {
                    if (getType(ue.expr) != VariableType.UNKNOWN)
                        yield VariableType.NUMBER;
                    else yield VariableType.UNKNOWN;
                }
                default -> VariableType.UNKNOWN;
            };
        } else if (cond instanceof ForExpression || cond instanceof LoopExpression || cond instanceof RepeatExpression) {
            return VariableType.LIST;
        } else if (cond instanceof FunctionDeclarationExpression) {
            return VariableType.FUNCTION;
        } else if (cond instanceof CatchExpression ce) {
            VariableType t = getType(ce.expr);
            if (t == getType(ce.fallback))
                return t;
            return VariableType.UNKNOWN;
        } else if (cond instanceof MatchExpression me) {
            VariableType t = null;
            for (MatchExpression.Case match : me.cases) {
                if (t == null) {
                    t = getType(match.pattern());
                } else if (t != getType(match.pattern())) {
                    return VariableType.UNKNOWN;
                }
            }
            if (me.other != null) {
                VariableType ot = getType(me.other);
                if (t == ot)
                    return t;
                return VariableType.UNKNOWN;
            }
            return t;
        }
        return VariableType.UNKNOWN;
    }
    public Expression visitRepeatExpression(RepeatExpression expr) {
        if (expr.repeat == 0)
            return new LiteralExpression(new ListValue(new ArrayList<>()));
        else if (expr.repeat <= 5) {
            ArrayList<Expression> expressions = new ArrayList<>();
            Expression optimizedExpr = expr.expr.accept(this);
            for (int i = 0; i < expr.repeat; i++) {
                expressions.add(optimizedExpr);
            }
            return new ListExpression(expressions).accept(this);
        } else if (expr.expr instanceof LiteralExpression le) {
            ArrayList<Value> values = new ArrayList<>();
            for (int i = 0; i < expr.repeat; i++) {
                values.add(le.value);
            }
            return new LiteralExpression(new ListValue(values));
        }
        return new RepeatExpression(expr.expr.accept(this), expr.repeat);
    }
}