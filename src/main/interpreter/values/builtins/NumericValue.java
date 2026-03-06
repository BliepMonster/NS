package main.interpreter.values.builtins;

import main.interpreter.values.InvalidOperationException;

import java.util.List;

public final class NumericValue extends Value {
    public final double number;
    public static final NumericValue ZERO = new NumericValue(0), ONE = new NumericValue(1);
    private NumericValue(double number) {
        this.number = number;
    }
    public Value add(Value v) {
        if (v instanceof NumericValue nv) {
            return NumericValue.of(number + nv.number);
        } else throw new InvalidOperationException("Cannot add " + v.getClass().getSimpleName() + " to a number (number on left side)");
    }
    public Value sub(Value v) {
        if (!(v instanceof NumericValue nv))
            throw new InvalidOperationException("Cannot subtract a" + v.getClass().getSimpleName()+ "from a number");
        return NumericValue.of(number - nv.number);
    }
    public Value mul(Value v) {
        if (!(v instanceof NumericValue nv))
            throw new InvalidOperationException("Cannot multiply a " + v.getClass().getSimpleName()+ " to a number (number on left side)");
        return NumericValue.of(number * nv.number);
    }
    public Value div(Value v) {
        if (!(v instanceof NumericValue nv))
            throw new InvalidOperationException("Cannot divide a " + v.getClass().getSimpleName()+ " by a number (number on left side)");
        if (nv.number == 0)
            throw new InvalidOperationException("Cannot divide by 0");
        return NumericValue.of(number / nv.number);
    }
    public Value mod(Value v) {
        if (!(v instanceof NumericValue nv))
            throw new InvalidOperationException("Cannot mod a " + v.getClass().getSimpleName()+ " by a number (number on left side)");
        if (nv.number == 0)
            throw new InvalidOperationException("Cannot modulo by 0");
        return NumericValue.of(number % nv.number);
    }
    public Value getMember(String mem) {
        throw new InvalidOperationException("Cannot get member value "+mem+" from a number");
    }
    public Value index(Value val) {
        throw new InvalidOperationException("Cannot index a number");
    }
    public Value call(List<Value> args) {
        throw new InvalidOperationException("Cannot call a number");
    }
    public BooleanValue eq(Value v) {
        return BooleanValue.fromBoolean(v instanceof NumericValue nv && number == nv.number);
    }
    public Value neg() {
        return NumericValue.of(-number);
    }
    public Value inv() {
        throw new InvalidOperationException("Cannot invert a number");
    }
    public BooleanValue isTruthy() {
        return BooleanValue.fromBoolean(number != 0);
    }
    public BooleanValue neq(Value v) {
        if (!(v instanceof NumericValue nv))
            return BooleanValue.fromBoolean(true);
        return BooleanValue.fromBoolean(number != nv.number);
    }
    public BooleanValue lt(Value v) {
        if (!(v instanceof NumericValue nv))
            throw new InvalidOperationException("Cannot compare a number to a " + v.getClass().getSimpleName());
        return BooleanValue.fromBoolean(number < nv.number);
    }
    public BooleanValue lte(Value v) {
        if (!(v instanceof NumericValue nv))
            throw new InvalidOperationException("Cannot compare a number to a " + v.getClass().getSimpleName());
        return BooleanValue.fromBoolean(number <= nv.number);
    }
    public BooleanValue gt(Value v) {
        if (!(v instanceof NumericValue nv))
            throw new InvalidOperationException("Cannot compare a number to a " + v.getClass().getSimpleName());
        return BooleanValue.fromBoolean(number > nv.number);
    }
    public BooleanValue gte(Value v) {
        if (!(v instanceof NumericValue nv))
            throw new InvalidOperationException("Cannot compare a number to a " + v.getClass().getSimpleName());
        return BooleanValue.fromBoolean(number >= nv.number);
    }
    public Value toNumber() {
        return this;
    }
    public String toString() {
        long l = (long) number;
        if (number == l)
            return Long.toString(l);
        return Double.toString(this.number);
    }
    public Value setMember(String s, Value v) {
        throw new InvalidOperationException("Cannot set member value "+s+" in a number");
    }
    public Value setIndex(Value v, Value w) {
        throw new InvalidOperationException("Cannot set index in a number");
    }
    public Value merge(Value v) {
        throw new InvalidOperationException("Cannot merge a number with a value");
    }
    public int hashCode() {
        return Double.hashCode(number);
    }
    public static NumericValue of(double n) {
        if (n == 0) return ZERO;
        if (n == 1) return ONE;
        return new NumericValue(n);
    }
}
