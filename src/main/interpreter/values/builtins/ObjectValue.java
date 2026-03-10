package main.interpreter.values.builtins;

import main.expr.Expression;
import main.interpreter.values.InvalidOperationException;

import java.util.*;

public final class ObjectValue extends Value {
    private final HashMap<String, Value> fields;
    public ObjectValue(InterpretedClassValue cv) {
        this.fields = new HashMap<>();
        for (Map.Entry<String, Expression> expr : cv.fields.entrySet()) {
            Value v = ExecutorHolder.EXECUTOR.evaluate(expr.getValue());
            if (v instanceof InterpretedFunctionValue iv) {
                v = iv.toMethod(this);
            }
            fields.put(expr.getKey(), v);
        }
    }
    public Value add(Value v) {
        if (!fields.containsKey("_add"))
            throw new InvalidOperationException("Cannot add an object");
        return fields.get("_add").call(List.of(v));
    }
    public Value sub(Value v) {
        if (!fields.containsKey("_sub"))
            throw new InvalidOperationException("Cannot subtract an object");
        return fields.get("_sub").call(List.of(v));
    }
    public Value mul(Value v) {
        if (!fields.containsKey("_mul"))
            throw new InvalidOperationException("Cannot multiply an object");
        return fields.get("_mul").call(List.of(v));
    }
    public Value div(Value v) {
        if (!fields.containsKey("_div"))
            throw new InvalidOperationException("Cannot divide an object");
        return fields.get("_div").call(List.of(v));
    }
    public Value mod(Value v) {
        if (!fields.containsKey("_mod"))
            throw new InvalidOperationException("Cannot modulo an object");
        return fields.get("_mod").call(List.of(v));
    }
    public Value getMember(String s) {
        if (fields.get(s) == null)
            throw new InvalidOperationException("Cannot get member value "+s+" from an object");
        return fields.get(s);
    }
    public Value index(Value v) {
        if (!fields.containsKey("_index"))
            throw new InvalidOperationException("Cannot index an object");
        return fields.get("_index").call(List.of(v));
    }
    public Value call(List<Value> v) {
        if (!fields.containsKey("_call"))
            throw new InvalidOperationException("Cannot call an object");
        return fields.get("_call").call(v);
    }
    public BooleanValue eq(Value v) {
        if (!fields.containsKey("_eq")) {
            return BooleanValue.fromBoolean(this == v);
        }
        Value val = fields.get("_eq").call(List.of(v));
        if (!(val instanceof BooleanValue bv)) {
            throw new InvalidOperationException("Cannot compare an object to a non-boolean value");
        }
        return bv;
    }
    public BooleanValue neq(Value v) {
        if (!fields.containsKey("_neq")) {
            return BooleanValue.fromBoolean(this == v);
        }
        Value val = fields.get("_neq").call(List.of(v));
        if (!(val instanceof BooleanValue bv))
            throw new InvalidOperationException("Cannot compare an object to a non-boolean value");
        return bv;
    }
    public BooleanValue gt(Value v) {
        if (!fields.containsKey("_gt")) {
            throw new InvalidOperationException("Cannot compare an object to a number");
        }
        Value val = fields.get("_gt").call(List.of(v));
        if (!(val instanceof BooleanValue bv))
            throw new InvalidOperationException("Cannot compare an object to a non-boolean value");
        return bv;
    }
    public BooleanValue gte(Value v) {
        if (!fields.containsKey("_gte")) {
            throw new InvalidOperationException("Cannot compare an object to a number");
        }
        Value val = fields.get("_gte").call(List.of(v));
        if (!(val instanceof BooleanValue bv))
            throw new InvalidOperationException("Cannot compare an object to a non-boolean value");
        return bv;
    }
    public BooleanValue lt(Value v) {
        if (!fields.containsKey("_lt")) {
            throw new InvalidOperationException("Cannot compare an object to a number");
        }
        Value val = fields.get("_lt").call(List.of(v));
        if (!(val instanceof BooleanValue bv))
            throw new InvalidOperationException("Cannot compare an object to a non-boolean value");
        return bv;
    }
    public BooleanValue lte(Value v) {
        if (!fields.containsKey("_lte")) {
            throw new InvalidOperationException("Cannot compare an object to a number");
        }
        Value val = fields.get("_lte").call(List.of(v));
        if (!(val instanceof BooleanValue bv))
            throw new InvalidOperationException("Cannot compare an object to a non-boolean value");
        return bv;
    }
    public Value neg() {
        if (!fields.containsKey("_neg"))
            throw new InvalidOperationException("Cannot negate an object");
        return fields.get("_neg").call(List.of());
    }
    public Value inv() {
        if (!fields.containsKey("_inv"))
            throw new InvalidOperationException("Cannot invert an object");
        return fields.get("_inv").call(List.of());
    }
    public BooleanValue isTruthy() {
        if (!fields.containsKey("_bool"))
            return BooleanValue.fromBoolean(true); // it is not null
        Value val = fields.get("_bool").call(List.of());
        if (!(val instanceof BooleanValue bv))
            throw new InvalidOperationException("Cannot check if an object is truthy");
        return bv;
    }
    public Value toNumber() {
        if (!fields.containsKey("_num"))
            throw new InvalidOperationException("Cannot convert an object to a number");
        return fields.get("_num").call(List.of());
    }
    public String toString() {
        if (!fields.containsKey("_str"))
            return "<object>";
        Value val = fields.get("_str").call(List.of());
        if (!(val instanceof StringValue sv))
            throw new InvalidOperationException("Cannot convert an object to a string");
        return sv.getValue();
    }
    public Value setMember(String s, Value v) {
        return fields.put(s, v);
    }
    public Value setIndex(Value v, Value w) {
        throw new InvalidOperationException("Cannot set index in an object");
    }
    public Value merge(Value v) {
        if (fields.containsKey("_merge")) {
            return fields.get("_merge").call(List.of(v));
        }
        throw new InvalidOperationException("No merge operator found for object");
    }
    public Value contains(Value v) {
        if (!fields.containsKey("_contains"))
            throw new InvalidOperationException("Cannot check if an object contains a value");
        return fields.get("_contains").call(List.of(v));
    }
    public int hashCode() {
        return fields.hashCode();
    }
    public boolean hasNext() {
        if (!fields.containsKey("_iter_hasnext"))
            throw new InvalidOperationException("Object does not override iterator function _iter_hasnext");
        var result = fields.get("_iter_hasnext").call(new ArrayList<>());
        if (!(result instanceof BooleanValue bv))
            throw new InvalidOperationException("Iterator function hasnext must return boolean");
        return bv.value;
    }
    public Value next() {
        if (!fields.containsKey("_iter_next"))
            throw new InvalidOperationException("Object does not override iterator function _iter_next");
        return fields.get("_iter_next").call(new ArrayList<>());
    }
    public Iterator<Value> iterator() {
        if (!fields.containsKey("_iter"))
            throw new InvalidOperationException("Object does not override iterable function _iter");
        var iter = fields.get("_iter").call(new ArrayList<>());
        return iter;
    }
}
