package main.interpreter.values.builtins;

import main.interpreter.Executor;
import main.interpreter.values.InvalidOperationException;

import java.util.List;

public final class NullValue extends Value {
    public static NullValue INSTANCE = null;
    public final Executor executor;
    public static void init(Executor executor) {
        INSTANCE = new NullValue(executor);
    }
    private NullValue(Executor executor) {
        this.executor = executor;
    }
    public Value add(Value v) {
        throw new InvalidOperationException("Cannot add null to a value");
    }
    public Value sub(Value v) {
        throw new InvalidOperationException("Cannot subtract null from a value");
    }
    public String toString() {
        return "null";
    }
    public Value mul(Value v) {
        throw new InvalidOperationException("Cannot multiply null by a value");
    }
    public Value div(Value v) {
        throw new InvalidOperationException("Cannot divide null by a value");
    }
    public Value mod(Value v) {
        throw new InvalidOperationException("Cannot mod null by a value");
    }
    public Value getMember(String mem) {
        throw new InvalidOperationException("Cannot get member value "+mem+" from a null");
    }
    public Value index(Value v) {
        throw new InvalidOperationException("Cannot index a null");
    }
    public Value call(List<Value> args) {
        throw new InvalidOperationException("Cannot call a null");
    }
    public BooleanValue eq(Value v) {
        return BooleanValue.fromBoolean(v instanceof NullValue);
    }
    public BooleanValue neq(Value v) {
        return BooleanValue.fromBoolean(!(v instanceof NullValue));
    }
    public Value neg() {
        throw new InvalidOperationException("Cannot negate a null");
    }
    public Value inv() {
        throw new InvalidOperationException("Cannot invert a null");
    }
    public BooleanValue isTruthy() {
        return BooleanValue.fromBoolean(false);
    }
    public BooleanValue lt(Value v) {
        throw new InvalidOperationException("Cannot compare a null to a number");
    }
    public BooleanValue lte(Value v) {
        throw new InvalidOperationException("Cannot compare a null to a number");
    }
    public BooleanValue gt(Value v) {
        throw new InvalidOperationException("Cannot compare a null to a number");
    }
    public BooleanValue gte(Value v) {
        throw new InvalidOperationException("Cannot compare a null to a number");
    }
    public Value toNumber() {
        return new NumericValue(0, executor);
    }
    public Value setMember(String s, Value v) {
        throw new InvalidOperationException("Cannot set member value "+s+" in a null");
    }
    public Value setIndex(Value v, Value w) {
        throw new InvalidOperationException("Cannot set index in a null");
    }
    public Value merge(Value v) {
        throw new InvalidOperationException("Cannot merge a null with a value");
    }
    public int hashCode() {
        return 0;
    }
}
