package main.interpreter.values.builtins;

import main.interpreter.values.InvalidOperationException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public final class SetValue extends Value {
    private final HashSet<Value> set;
    public static abstract class FunctionBuilder {
        public abstract CompiledFunctionValue build(SetValue set);
    }
    public static final HashMap<String, FunctionBuilder> FUNCTIONS = new HashMap<>();
    static {
        FUNCTIONS.put("add", new FunctionBuilder() {
            @Override
            public CompiledFunctionValue build(SetValue set) {
                return new CompiledFunctionValue() {
                    @Override
                    public Value call(List<Value> args) {
                        set.set.addAll(args);
                        return set;
                    }
                };
            }
        });
        FUNCTIONS.put("remove", new FunctionBuilder() {
            @Override
            public CompiledFunctionValue build(SetValue set) {
                return new CompiledFunctionValue() {
                    @Override
                    public Value call(List<Value> args) {
                        args.forEach(set.set::remove);
                        return set;
                    }
                };
            }
        });
    }
    public SetValue(HashSet<Value> set) {
        this.set = set;
    }
    public Value contains(Value v) {
        return BooleanValue.fromBoolean(set.contains(v));
    }
    public Value add(Value v) {
        throw new InvalidOperationException("Cannot use + on a set");
    }
    public Value sub(Value v) {
        throw new InvalidOperationException("Cannot use - on a set");
    }
    public Value mul(Value v) {
        throw new InvalidOperationException("Cannot use * on a set");
    }
    public Value div(Value v) {
        if (!(v instanceof SetValue sv))
            throw new InvalidOperationException("Cannot divide a set by a non-set");
        HashSet<Value> newSet = new HashSet<>(set);
        newSet.removeAll(sv.set);
        return new SetValue(newSet);
    }
    public Value mod(Value v) {
        throw new InvalidOperationException("Cannot use % on a set");
    }
    public Value getMember(String s) {
        FunctionBuilder fb = FUNCTIONS.get(s);
        if (fb == null)
            throw new InvalidOperationException("Unknown set method: " + s);
        return fb.build(this);
    }
    public Value index(Value v) {
        throw new InvalidOperationException("Cannot index a set");
    }
    public Value call(List<Value> args) {
        throw new InvalidOperationException("Cannot call a set");
    }
    public BooleanValue eq(Value v) {
        if (!(v instanceof SetValue sv))
            return BooleanValue.FALSE;
        return BooleanValue.fromBoolean(set.equals(sv.set));
    }
    public BooleanValue neq(Value v) {
        if (!(v instanceof SetValue sv))
            return BooleanValue.TRUE;
        return BooleanValue.fromBoolean(!set.equals(sv.set));
    }
    public Value neg() {
        throw new InvalidOperationException("Cannot negate a set");
    }
    public Value inv() {
        throw new InvalidOperationException("Cannot invert a set");
    }
    public BooleanValue isTruthy() {
        return BooleanValue.fromBoolean(!set.isEmpty());
    }
    public BooleanValue gt(Value v) {
        throw new InvalidOperationException("Cannot compare a set to a number");
    }
    public BooleanValue lt(Value v) {
        throw new InvalidOperationException("Cannot compare a set to a number");
    }
    public BooleanValue lte(Value v) {
        throw new InvalidOperationException("Cannot compare a set to a number");
    }
    public BooleanValue gte(Value v) {
        throw new InvalidOperationException("Cannot compare a set to a number");
    }
    public Value toNumber() {
        return NumericValue.of(this.set.size());
    }
    public String toString() {
        StringBuilder sb = new StringBuilder("%[");
        for (Value v : set) {
            sb.append(v.toString());
            sb.append(", ");
        }
        if (!set.isEmpty()) sb.delete(sb.length()-2, sb.length());
        return sb.append("]").toString();
    }
    public Value setMember(String s, Value v) {
        throw new InvalidOperationException("Cannot set member value "+s+" in a set");
    }
    public Value setIndex(Value v, Value w) {
        throw new InvalidOperationException("Cannot set index in a set");
    }
    public Value merge(Value v) {
        if (!(v instanceof SetValue sv))
            throw new InvalidOperationException("Cannot merge a set with a value");
        HashSet<Value> newSet = new HashSet<>(set);
        newSet.addAll(sv.set);
        return new SetValue(newSet);
    }
    public int hashCode() {
        return set.hashCode();
    }

    @Override
    public Iterator<Value> iterator() {
        return this.set.iterator();
    }
}
