package main.interpreter.values;

import main.expr.Expression;
import main.interpreter.Executor;

import java.util.HashMap;
import java.util.List;

/// less of a class, more a template for building objects; objects do not hold classes
public final class InterpretedClassValue extends Value {
    public final HashMap<String, Expression> fields;
    public final Executor executor;
    public InterpretedClassValue(HashMap<String, Expression> fields, Executor executor) {
        this.executor = executor;
        this.fields = fields;
    }
    public Value add(Value v) {
        throw new InvalidOperationException("Cannot add a class to a value");
    }
    public Value sub(Value v) {
        throw new InvalidOperationException("Cannot subtract a class from a value");
    }
    public Value mul(Value v) {
        throw new InvalidOperationException("Cannot multiply a class by a value");
    }
    public Value div(Value v) {
        throw new InvalidOperationException("Cannot divide a class by a value");
    }
    public Value mod(Value v) {
        throw new InvalidOperationException("Cannot mod a class by a value");
    }
    /// members are for templates for objects, not for direct indexing
    public Value getMember(String mem) {
        throw new InvalidOperationException("Cannot get member value "+mem+" from a class");
    }
    public Value index(Value v) {
        throw new InvalidOperationException("Cannot index a class");
    }
    public Value call(List<Value> args) {
        return new ObjectValue(this, executor);
    }
    public BooleanValue eq(Value v) {
        return BooleanValue.fromBoolean(equals(v));
    }
    public Value neg() {
        throw new InvalidOperationException("Cannot negate a class");
    }
    public Value inv() {
        throw new InvalidOperationException("Cannot invert a class");
    }
    public BooleanValue isTruthy() {
        return BooleanValue.fromBoolean(true);
    }
    public BooleanValue neq(Value v) {
        return BooleanValue.fromBoolean(!equals(v));
    }
    public BooleanValue gt(Value v) {
        throw new InvalidOperationException("Cannot compare a class to a number");
    }
    public BooleanValue lt(Value v) {
        throw new InvalidOperationException("Cannot compare a class to a number");
    }
    public BooleanValue lte(Value v) {
        throw new InvalidOperationException("Cannot compare a class to a number");
    }
    public BooleanValue gte(Value v) {
        throw new InvalidOperationException("Cannot compare a class to a number");
    }
    public Value toNumber() {
        throw new InvalidOperationException("Cannot convert a class to a number");
    }
    public String toString() {
        return "<class>";
    }
    public Value setMember(String s , Value v) {
        throw new InvalidOperationException("Cannot set member value "+s+" in a class");
    }
    public Value setIndex(Value v, Value w) {
        throw new InvalidOperationException("Cannot set index in a class");
    }
}
