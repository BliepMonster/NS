package main.interpreter.values.builtins;

import main.interpreter.values.InvalidOperationException;

public final class BooleanValue extends Value {
    public final boolean value;
    private BooleanValue(boolean value) {
        this.value = value;
    }
    public static final BooleanValue TRUE = new BooleanValue(true);
    public static final BooleanValue FALSE = new BooleanValue(false);
    public static BooleanValue fromBoolean(boolean value) {
        return value ? TRUE : FALSE;
    }
    public Value add(Value v) {
        throw new InvalidOperationException("Cannot add boolean values");
    }
    public Value sub(Value v) {
        throw new InvalidOperationException("Cannot subtract boolean values");
    }
    public Value mul(Value v) {
        throw new InvalidOperationException("Cannot multiply boolean values");
    }
    public Value div(Value v) {
        throw new InvalidOperationException("Cannot divide boolean values");
    }
    public Value mod(Value v) {
        throw new InvalidOperationException("Cannot mod boolean values");
    }
    public Value getMember(String mem) {
        throw new InvalidOperationException("Cannot get member value "+mem+" from a boolean");
    }
    public Value index(Value v) {
        throw new InvalidOperationException("Cannot index a boolean");
    }
    public Value call(java.util.List<Value> args) {
        throw new InvalidOperationException("Cannot call a boolean");
    }
    public BooleanValue eq(Value v) {
        return fromBoolean(v instanceof BooleanValue bv && value == bv.value);
    }
    public Value neg() {
        throw new InvalidOperationException("Cannot negate a boolean");
    }
    public Value inv() {
        return fromBoolean(!value);
    }
    public BooleanValue isTruthy() {
        return this;
    }
    public BooleanValue neq(Value v) {
        if (!(v instanceof BooleanValue bv))
            return fromBoolean(true);
        return fromBoolean(value != bv.value);
    }
    public BooleanValue lt(Value v) {
        throw new InvalidOperationException("Cannot compare a boolean to a number");
    }
    public BooleanValue lte(Value v) {
        throw new InvalidOperationException("Cannot compare a boolean to a number");
    }
    public BooleanValue gt(Value v) {
        throw new InvalidOperationException("Cannot compare a boolean to a number");
    }
    public BooleanValue gte(Value v) {
        throw new InvalidOperationException("Cannot compare a boolean to a number");
    }
    public Value toNumber() {
        return value ? NumericValue.ONE : NumericValue.ZERO;
    }
    public String toString() {
        return value ? "true" : "false";
    }
    public Value setMember(String s, Value v) {
        throw new InvalidOperationException("Cannot set member value "+s+" in a boolean");
    }
    public Value setIndex(Value v, Value w) {
        throw new InvalidOperationException("Cannot set index in a boolean");
    }
    public Value merge(Value v) {
        throw new InvalidOperationException("Cannot merge a boolean with a value");
    }
    public int hashCode() {
        return value ? 1231 : 1237;
    }
}
