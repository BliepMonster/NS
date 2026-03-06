package main.interpreter.values.builtins;

import main.interpreter.values.InvalidOperationException;

import java.util.List;

public final class StringValue extends Value {
    private final String value;
    public StringValue(String value) {
        this.value = value;
    }
    public Value add(Value v) {
        return new StringValue(value+v.toString());
    }
    public String getValue() {
        return value;
    }
    public String toString() {
        return value;
    }
    public Value sub(Value v) {
        throw new InvalidOperationException("Cannot subtract a string");
    }
    public Value mul(Value v) {
        if (!(v instanceof NumericValue nv))
            throw new InvalidOperationException("Cannot multiply a string by a non-number");
        if (nv.number < 0)
            throw new InvalidOperationException("Cannot multiply a string by a negative number");
        if (nv.number > Integer.MAX_VALUE)
            throw new InvalidOperationException("Cannot multiply a string by a number larger than "+Integer.MAX_VALUE);
        if (nv.number % 1 != 0)
            throw new InvalidOperationException("Cannot multiply a string by a non-integer number");
        return new StringValue(value.repeat((int) nv.number));
    }
    public Value div(Value v) {
        throw new InvalidOperationException("Cannot divide a string");
    }
    public Value mod(Value v) {
        throw new InvalidOperationException("Cannot mod a string");
    }
    public Value getMember(String s) {
        throw new InvalidOperationException("Cannot get member value "+s+" from a string");
    }
    public Value index(Value v) {
        if (!(v instanceof NumericValue nv))
            throw new InvalidOperationException("Cannot index a string by a non-number");
        if (nv.number < 0)
            throw new InvalidOperationException("Cannot index a string by a negative number");
        int i = (int) nv.number;
        if (i != nv.number)
            throw new InvalidOperationException("Cannot index a string by a non-integer number");
        return new StringValue(value.substring(i, i+1));
    }
    public Value call(List<Value> args) {
        throw new InvalidOperationException("Cannot call a string");
    }
    public BooleanValue eq(Value v) {
        return BooleanValue.fromBoolean(v instanceof StringValue sv && value.equals(sv.value));
    }
    public Value neg() {
        throw new InvalidOperationException("Cannot negate a string");
    }
    public Value inv() {
        throw new InvalidOperationException("Cannot invert a string");
    }
    public BooleanValue isTruthy() {
        return BooleanValue.fromBoolean(!value.isEmpty());
    }
    public BooleanValue neq(Value v) {
        if (!(v instanceof StringValue sv))
            return BooleanValue.fromBoolean(true);
        return BooleanValue.fromBoolean(!value.equals(sv.value));
    }
    public BooleanValue lt(Value v) {
        throw new InvalidOperationException("Cannot compare a string to a number");
    }
    public BooleanValue lte(Value v) {
        throw new InvalidOperationException("Cannot compare a string to a number");
    }
    public BooleanValue gt(Value v) {
        throw new InvalidOperationException("Cannot compare a string to a number");
    }
    public BooleanValue gte(Value v) {
        throw new InvalidOperationException("Cannot compare a string to a number");
    }
    public Value toNumber() {
        try {
            return NumericValue.of(Double.parseDouble(value));
        } catch (NumberFormatException e) {
            throw new InvalidOperationException("Cannot convert string to number");
        }
    }
    public Value setMember(String s, Value v) {
        throw new InvalidOperationException("Cannot set member value "+s+" in a string");
    }
    public Value setIndex(Value v, Value w) {
        throw new InvalidOperationException("Strings are immutable, cannot set index");
    }
    public Value merge(Value v) {
        throw new InvalidOperationException("Cannot merge a string with a value");
    }
    public int hashCode() {
        return value.hashCode();
    }
}
