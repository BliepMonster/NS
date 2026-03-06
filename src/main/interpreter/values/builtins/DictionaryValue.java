package main.interpreter.values.builtins;

import main.interpreter.Executor;
import main.interpreter.values.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class DictionaryValue extends Value {
    private final HashMap<Value, Value> map;
    private final Executor executor;
    private final HashMap<String, CompiledFunctionValue> memberFunctions = new HashMap<>();
    public DictionaryValue(HashMap<Value, Value> map, Executor executor) {
        this.executor = executor;
        this.map = map;
        memberFunctions.put("containsKey", new CompiledFunctionValue(executor) {
            @Override
            public Value call(List<Value> args) {
                if (args.size() != 1)
                    throw new InvalidOperationException("containsKey() takes exactly one argument");
                return BooleanValue.fromBoolean(map.containsKey(args.getFirst()));
            }
        });
        memberFunctions.put("containsValue", new CompiledFunctionValue(executor) {
            @Override
            public Value call(List<Value> args) {
                if (args.size() != 1)
                    throw new InvalidOperationException("containsKey() takes exactly one argument");
                return BooleanValue.fromBoolean(map.containsValue(args.getFirst()));
            }
        });
    }
    public Value add(Value v) {
        throw new InvalidOperationException("Cannot add a dictionary to a value");
    }
    public Value sub(Value v) {
        throw new InvalidOperationException("Cannot subtract a dictionary from a value");
    }
    public Value mul(Value v) {
        throw new InvalidOperationException("Cannot multiply a dictionary by a value");
    }
    public Value div(Value v) {
        throw new InvalidOperationException("Cannot divide a dictionary by a value");
    }
    public Value mod(Value v) {
        throw new InvalidOperationException("Cannot mod a dictionary by a value");
    }
    public Value getMember(String s) {
        if (!memberFunctions.containsKey(s))
            throw new InvalidOperationException("Dictionary does not have member "+s);
        return memberFunctions.get(s);
    }
    public Value index(Value v) {
        if (!map.containsKey(v))
            return NullValue.INSTANCE;
        return map.get(v);
    }
    public Value call(List<Value> args) {
        throw new InvalidOperationException("Cannot call a dictionary");
    }
    public BooleanValue eq(Value v) {
        if (!(v instanceof DictionaryValue dv))
            return BooleanValue.fromBoolean(false);
        return BooleanValue.fromBoolean(map.equals(dv.map));
    }
    public Value neg() {
        throw new InvalidOperationException("Cannot negate a dictionary");
    }
    public Value inv() {
        throw new InvalidOperationException("Cannot invert a dictionary");
    }
    public BooleanValue isTruthy() {
        return BooleanValue.fromBoolean(!map.isEmpty());
    }
    public BooleanValue neq(Value v) {
        if (!(v instanceof DictionaryValue dv))
            return BooleanValue.fromBoolean(true);
        return BooleanValue.fromBoolean(!map.equals(dv.map));
    }
    public BooleanValue lt(Value v) {
        throw new InvalidOperationException("Cannot compare a dictionary to a number");
    }
    public BooleanValue lte(Value v) {
        throw new InvalidOperationException("Cannot compare a dictionary to a number");
    }
    public BooleanValue gt(Value v) {
        throw new InvalidOperationException("Cannot compare a dictionary to a number");
    }
    public BooleanValue gte(Value v) {
        throw new InvalidOperationException("Cannot compare a dictionary to a number");
    }
    public Value toNumber() {
        return new NumericValue(map.size(), executor);
    }
    public String toString() {
        StringBuilder sb = new StringBuilder("{");
        for (Map.Entry<Value, Value> entry : map.entrySet()) {
            sb.append(entry.getKey().toString()).append(": ").append(entry.getValue().toString()).append(", ");
        }
        sb.deleteCharAt(sb.length()-2);
        sb.deleteCharAt(sb.length()-1);
        sb.append("}");
        return sb.toString();
    }
    public Value setMember(String s, Value v) {
        throw new InvalidOperationException("Cannot set member value "+s+" in a dictionary");
    }
    public Value setIndex(Value v, Value w) {
        map.put(v, w);
        return w;
    }
    public Value merge(Value v) {
        if (!(v instanceof DictionaryValue dv))
            throw new InvalidOperationException("Cannot merge a dictionary with a value");
        HashMap<Value, Value> newMap = new HashMap<>(map);
        newMap.putAll(dv.map);
        return new DictionaryValue(newMap, executor);
    }
    public Value contains(Value v) {
        return BooleanValue.fromBoolean(map.containsKey(v) || map.containsValue(v));
    }
    public int hashCode() {
        return map.hashCode();
    }
}
