package main.interpreter.values.builtins;

import main.interpreter.values.InvalidOperationException;

import java.util.List;

public final class RangeValue extends Value {
    public final double l, r;
    public final boolean order;
    public static final boolean ASCENDING = true,
                                DESCENDING = false;
    public RangeValue(double min, double max, boolean order) {
        this.l = min;
        this.r = max;
        this.order = order;
    }
    public Value add(Value v) {
        throw new InvalidOperationException("Cannot add a range to a value");
    }
    public Value sub(Value v) {
        throw new InvalidOperationException("Cannot subtract a range from a value");
    }
    public Value mul(Value v) {
        throw new InvalidOperationException("Cannot multiply a range by a value");
    }
    public Value div(Value v) {
        throw new InvalidOperationException("Cannot divide a range by a value");
    }
    public Value mod(Value v) {
        throw new InvalidOperationException("Cannot mod a range by a value");
    }
    public Value getMember(String mem) {
        // TODO
        throw new InvalidOperationException("Cannot get member value "+mem+" from a range");
    }
    public Value index(Value v) {
        throw new InvalidOperationException("Cannot index a range");
    }
    public Value call(List<Value> args) {
        throw new InvalidOperationException("Cannot call a range");
    }
    public BooleanValue eq(Value v) {
        if (!(v instanceof RangeValue rv))
            return BooleanValue.fromBoolean(false);
        return BooleanValue.fromBoolean(rv.l == l && rv.r == r && rv.order == order);
    }
    public Value neg() {
        throw new InvalidOperationException("Cannot negate a range");
    }
    public Value inv() {
        throw new InvalidOperationException("Cannot invert a range");
    }
    public BooleanValue isTruthy() {
        return BooleanValue.fromBoolean(true);
    }
    public BooleanValue neq(Value v) {
        return BooleanValue.fromBoolean(!eq(v).value);
    }
    public BooleanValue lt(Value v) {
        throw new InvalidOperationException("Cannot compare a range to a number");
    }
    public BooleanValue lte(Value v) {
        throw new InvalidOperationException("Cannot compare a range to a number");
    }
    public BooleanValue gt(Value v) {
        throw new InvalidOperationException("Cannot compare a range to a number");
    }
    public BooleanValue gte(Value v) {
        throw new InvalidOperationException("Cannot compare a range to a number");
    }
    public Value toNumber() {
        if (order == ASCENDING)
            return NumericValue.of(r-l);
        return NumericValue.of(l-r);
    }
    public String toString() {
        return "["+l+","+r+"]";
    }
    public Value setMember(String id, Value v) {
        throw new InvalidOperationException("Cannot set member value "+id+" in a range");
    }
    public Value setIndex(Value v, Value w) {
        throw new InvalidOperationException("Cannot set index in a range");
    }
    public Value merge(Value v) {
        throw new InvalidOperationException("Cannot merge a range with a value");
    }
    public BooleanValue contains(Value v) {
        if (!(v instanceof NumericValue nv))
            throw new InvalidOperationException("Cannot check if a range contains a non-numeric value");
        if (order == ASCENDING)
            return BooleanValue.fromBoolean(nv.number >= l && nv.number <= r);
        return BooleanValue.fromBoolean(nv.number <= l && nv.number >= r);
    }
    public int hashCode() {
        return (int) (l+r);
    }
}
