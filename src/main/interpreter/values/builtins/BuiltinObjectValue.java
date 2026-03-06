package main.interpreter.values.builtins;

import main.interpreter.values.InvalidOperationException;

import java.util.List;

public abstract non-sealed class BuiltinObjectValue<T> extends Value {
    protected T obj;
    public BuiltinObjectValue(T obj) {
        this.obj = obj;
    }
    public Value add(Value v) {
        throw new InvalidOperationException("Cannot add a value to a builtin object");
    }
    public Value sub(Value v) {
        throw new InvalidOperationException("Cannot subtract a value from a builtin object");
    }
    public Value mul(Value v) {
        throw new InvalidOperationException("Cannot multiply a value by a builtin object");
    }
    public Value div(Value v) {
        throw new InvalidOperationException("Cannot divide a value by a builtin object");
    }
    public Value mod(Value v) {
        throw new InvalidOperationException("Cannot mod a value by a builtin object");
    }
    public Value index(Value v) {
        throw new InvalidOperationException("Cannot index a builtin object");
    }

    @Override
    public Value toNumber() {
        throw new InvalidOperationException("Cannot convert a builtin object to a number");
    }
    public Value call(List<Value> args) {
        throw new InvalidOperationException("Cannot call a builtin object");
    }
    public Value setMember(String s, Value v) {
        throw new InvalidOperationException("Cannot set member value "+s+" in a builtin object");
    }
    public Value neg() {
        throw new InvalidOperationException("Cannot negate a builtin object");
    }
    public Value inv() {
        throw new InvalidOperationException("Cannot invert a builtin object");
    }
    public Value merge(Value v) {
        throw new InvalidOperationException("Cannot merge a builtin object with a value");
    }
    public BooleanValue isTruthy() {
        return BooleanValue.TRUE;
    }
    public BooleanValue gt(Value v) {
        throw new InvalidOperationException("Cannot compare a builtin object to a number");
    }
    public BooleanValue lt(Value v) {
        throw new InvalidOperationException("Cannot compare a builtin object to a number");
    }
    public BooleanValue gte(Value v) {
        throw new InvalidOperationException("Cannot compare a builtin object to a number");
    }
    public BooleanValue lte(Value v) {
        throw new InvalidOperationException("Cannot compare a builtin object to a number");
    }
    public Value setIndex(Value v, Value w) {
        throw new InvalidOperationException("Cannot set index in a builtin object");
    }
    public int hashCode() {
        return obj.hashCode();
    }
    public BooleanValue eq(Value other) {
        if (other instanceof BuiltinObjectValue<?> bov)
            return BooleanValue.fromBoolean(obj.equals(bov.obj));
        return BooleanValue.FALSE;
    }
    public BooleanValue neq(Value other) {
        if (other instanceof BuiltinObjectValue<?> bov)
            return BooleanValue.fromBoolean(!obj.equals(bov.obj));
        return BooleanValue.TRUE;
    }
    public String toString() {
        return "<native object>";
    }
}
