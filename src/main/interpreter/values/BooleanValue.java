package main.interpreter.values;

import main.interpreter.Executor;

public final class BooleanValue extends Value {
    public final boolean value;
    public final Executor executor;
    private BooleanValue(boolean value, Executor executor) {
        this.value = value;
        this.executor = executor;
    }
    public static BooleanValue TRUE;
    public static BooleanValue FALSE;
    public static void init(Executor executor) {
        TRUE = new BooleanValue(true, executor);
        FALSE = new BooleanValue(false, executor);
    }
    public static BooleanValue fromBoolean(boolean value) {
        return value ? TRUE : FALSE;
    }
    public Value add(Value v) {
        if (v instanceof StringValue sv) {
            return new StringValue(value + sv.getValue(), executor);
        }
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
    public Value asNumber() {
        return new NumericValue(value ? 1 : 0, executor);
    }
    public BooleanValue eq(Value v) {
        return new BooleanValue(v instanceof BooleanValue bv && value == bv.value, executor);
    }
    public Value neg() {
        throw new InvalidOperationException("Cannot negate a boolean");
    }
    public Value inv() {
        return new BooleanValue(!value, executor);
    }
    public BooleanValue isTruthy() {
        return this;
    }
    public BooleanValue neq(Value v) {
        if (!(v instanceof BooleanValue bv))
            return new BooleanValue(true, executor);
        return new BooleanValue(value != bv.value, executor);
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
        return new NumericValue(value ? 1 : 0, executor);
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
        return Boolean.hashCode(value);
    }
}
