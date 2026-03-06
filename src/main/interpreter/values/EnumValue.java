package main.interpreter.values;

import main.interpreter.Executor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public final class EnumValue extends Value {
    private HashMap<String, EnumHandleValue> handles;
    private Executor executor;
    public EnumValue(ArrayList<String> handles, Executor executor) {
        this.handles = new HashMap<>();
        for (String handle : handles) {
            this.handles.put(handle, new EnumHandleValue(handle, this, executor));
        }
        this.executor = executor;
    }
    public Value add(Value v) {
        throw new InvalidOperationException("Cannot add an enum to a value");
    }
    public Value sub(Value v) {
        throw new InvalidOperationException("Cannot subtract an enum from a value");
    }
    public Value mul(Value v) {
        throw new InvalidOperationException("Cannot multiply an enum by a value");
    }
    public Value div(Value v) {
        throw new InvalidOperationException("Cannot divide an enum by a value");
    }
    public Value mod(Value v) {
        throw new InvalidOperationException("Cannot mod an enum by a value");
    }
    public Value getMember(String mem) {
        if (!handles.containsKey(mem))
            throw new InvalidOperationException("Enum does not have member "+mem);
        return handles.get(mem);
    }
    public Value index(Value v) {
        throw new InvalidOperationException("Cannot index an enum");
    }
    public Value call(List<Value> args) {
        throw new InvalidOperationException("Cannot call an enum");
    }
    public BooleanValue eq(Value v) {
        return BooleanValue.fromBoolean(equals(v));
    }
    public Value neg() {
        throw new InvalidOperationException("Cannot negate an enum");
    }
    public Value inv() {
        throw new InvalidOperationException("Cannot invert an enum");
    }
    public BooleanValue isTruthy() {
        return BooleanValue.fromBoolean(true);
    }
    public BooleanValue neq(Value v) {
        return BooleanValue.fromBoolean(!equals(v));
    }
    public BooleanValue gt(Value v) {
        throw new InvalidOperationException("Cannot compare an enum to a number");
    }
    public BooleanValue lt(Value v) {
        throw new InvalidOperationException("Cannot compare an enum to a number");
    }
    public BooleanValue lte(Value v) {
        throw new InvalidOperationException("Cannot compare an enum to a number");
    }
    public BooleanValue gte(Value v) {
        throw new InvalidOperationException("Cannot compare an enum to a number");
    }
    public Value toNumber() {
        throw new InvalidOperationException("Cannot convert an enum to a number");
    }
    public String toString() {
        return "<enum>";
    }
    public Value setMember(String s, Value v) {
        throw new InvalidOperationException("Cannot set member value "+s+" in an enum");
    }
    public Value setIndex(Value v, Value w) {
        throw new InvalidOperationException("Cannot set index in an enum");
    }
    public Value merge(Value v) {
        if (!(v instanceof EnumValue ev))
            throw new InvalidOperationException("Cannot merge an enum with a value");
        HashMap<String, EnumHandleValue> newHandles = new HashMap<>(handles);
        newHandles.putAll(ev.handles);
        newHandles.putAll(this.handles);
        return new EnumValue(new ArrayList<>(newHandles.keySet()), executor);
    }
}
