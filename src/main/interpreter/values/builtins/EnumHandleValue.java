package main.interpreter.values.builtins;

import main.interpreter.values.InvalidOperationException;

import java.util.List;

public final class EnumHandleValue extends Value {
    public final String name;
    public final EnumValue owner;
    public EnumHandleValue(String name, EnumValue owner) {
        this.name = name;
        this.owner = owner;
    }
    public Value add(Value v) {
        throw new InvalidOperationException("Cannot add an enum handle to a value");
    }
    public Value sub(Value v) {
        throw new InvalidOperationException("Cannot subtract an enum handle from a value");
    }
    public Value mul(Value v) {
        throw new InvalidOperationException("Cannot multiply an enum handle by a value");
    }
    public Value div(Value v) {
        throw new InvalidOperationException("Cannot divide an enum handle by a value");
    }
    public Value mod(Value v) {
        throw new InvalidOperationException("Cannot mod an enum handle by a value");
    }
    public Value getMember(String s) {
        throw new InvalidOperationException("Cannot get member value "+s+" from an enum handle");
    }
    public Value index(Value v) {
        throw new InvalidOperationException("Cannot index an enum handle");
    }
    public Value call(List<Value> args) {
        throw new InvalidOperationException("Cannot call an enum handle");
    }
    public BooleanValue eq(Value v) {
        if (!(v instanceof EnumHandleValue ev))
            return BooleanValue.fromBoolean(false);
        boolean b = owner.equals(ev.owner);
        return BooleanValue.fromBoolean(b && name.equals(ev.name));
    }
    public BooleanValue neq(Value v) {
        if (!(v instanceof EnumHandleValue ev))
            return BooleanValue.fromBoolean(true);
        boolean b = !owner.equals(ev.owner);
        return BooleanValue.fromBoolean(b || !name.equals(ev.name));
    }
    public Value neg() {
        throw new InvalidOperationException("Cannot negate an enum handle");
    }
    public Value inv() {
        throw new InvalidOperationException("Cannot invert an enum handle");
    }
    public BooleanValue isTruthy() {
        return BooleanValue.fromBoolean(true);
    }
    public BooleanValue gt(Value v) {
        throw new InvalidOperationException("Cannot compare an enum handle to a number");
    }
    public BooleanValue lt(Value v) {
        throw new InvalidOperationException("Cannot compare an enum handle to a number");
    }
    public BooleanValue gte(Value v) {
        throw new InvalidOperationException("Cannot compare an enum handle to a number");
    }
    public BooleanValue lte(Value v) {
        throw new InvalidOperationException("Cannot compare an enum handle to a number");
    }
    public Value toNumber() {
        throw new InvalidOperationException("Cannot convert an enum handle to a number");
    }
    public String toString() {
        return name;
    }
    public Value setMember(String s, Value v) {
        throw new InvalidOperationException("Cannot set member value "+s+" in an enum handle");
    }
    public Value setIndex(Value v, Value w) {
        throw new InvalidOperationException("Cannot set index in an enum handle");
    }
    public Value merge(Value v) {
        throw new InvalidOperationException("Cannot merge an enum handle with a value");
    }
    public int hashCode() {
        return name.hashCode();
    }
}
