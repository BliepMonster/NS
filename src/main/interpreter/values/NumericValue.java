package main.interpreter.values;

import main.interpreter.Executor;

import java.util.List;

public final class NumericValue extends Value {
    public final double number;
    public final Executor executor;
    public NumericValue(double number, Executor executor) {
        this.number = number;
        this.executor = executor;
    }
    public Value asNumber() {
        return this;
    }
    public Value add(Value v) {
        if (v instanceof StringValue sv) {
            return new StringValue(number + sv.value, executor);
        } else if (v instanceof NumericValue nv) {
            return new NumericValue(number + nv.number, executor);
        } else throw new InvalidOperationException("Cannot add " + v.getClass().getSimpleName() + " to a number (number on left side)");
    }
    public Value sub(Value v) {
        if (!(v instanceof NumericValue nv))
            throw new InvalidOperationException("Cannot subtract a" + v.getClass().getSimpleName()+ "from a number");
        return new NumericValue(number - nv.number, executor);
    }
    public Value mul(Value v) {
        if (!(v instanceof NumericValue nv))
            throw new InvalidOperationException("Cannot multiply a " + v.getClass().getSimpleName()+ " to a number (number on left side)");
        return new NumericValue(number * nv.number, executor);
    }
    public Value div(Value v) {
        if (!(v instanceof NumericValue nv))
            throw new InvalidOperationException("Cannot divide a " + v.getClass().getSimpleName()+ " by a number (number on left side)");
        return new NumericValue(number / nv.number, executor);
    }
    public Value mod(Value v) {
        if (!(v instanceof NumericValue nv))
            throw new InvalidOperationException("Cannot mod a " + v.getClass().getSimpleName()+ " by a number (number on left side)");
        return new NumericValue(number % nv.number, executor);
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
        return new BooleanValue(v instanceof NumericValue nv && number == nv.number, executor);
    }
    public Value neg() {
        return new NumericValue(-number, executor);
    }
    public Value inv() {
        throw new InvalidOperationException("Cannot invert a number");
    }
    public BooleanValue isTruthy() {
        return new BooleanValue(number != 0, executor);
    }
    public BooleanValue neq(Value v) {
        if (!(v instanceof NumericValue nv))
            return new BooleanValue(true, executor);
        return new BooleanValue(number != nv.number, executor);
    }
    public BooleanValue lt(Value v) {
        if (!(v instanceof NumericValue nv))
            throw new InvalidOperationException("Cannot compare a number to a " + v.getClass().getSimpleName());
        return new BooleanValue(number < nv.number, executor);
    }
    public BooleanValue lte(Value v) {
        if (!(v instanceof NumericValue nv))
            throw new InvalidOperationException("Cannot compare a number to a " + v.getClass().getSimpleName());
        return new BooleanValue(number <= nv.number, executor);
    }
    public BooleanValue gt(Value v) {
        if (!(v instanceof NumericValue nv))
            throw new InvalidOperationException("Cannot compare a number to a " + v.getClass().getSimpleName());
        return new BooleanValue(number > nv.number, executor);
    }
    public BooleanValue gte(Value v) {
        if (!(v instanceof NumericValue nv))
            throw new InvalidOperationException("Cannot compare a number to a " + v.getClass().getSimpleName());
        return new BooleanValue(number >= nv.number, executor);
    }
    public Value toNumber() {
        return this;
    }
    public String toString() {
        return Double.toString(this.number);
    }
}
