package main.interpreter;

import main.interpreter.values.builtins.Value;

import java.util.HashMap;

public class Scope {
    public final Scope parent;
    private final HashMap<String, Value> variables = new HashMap<>();
    public Scope(Scope parent) {
        this.parent = parent;
    }
    public Scope() {
        this(null);
    }
    public void assign(String name, Value val) {
        if (variables.containsKey(name)) {
            variables.put(name, val);
        } else if (parent != null && parent.contains(name)) {
            parent.assign(name, val);
        } else {
            variables.put(name, val);
        }
    }
    public boolean contains(String name) {
        return variables.containsKey(name) || (parent != null && parent.contains(name));
    }
    public Value lookup(String name) {
        if (variables.containsKey(name)) {
            return variables.get(name);
        } else if (parent != null) {
            return parent.lookup(name);
        } else {
            throw new RuntimeException("Variable not found: " + name);
        }
    }
    private Value thisValue;

    public Value getThis() {
        return thisValue != null ? thisValue : (parent != null ? parent.getThis() : null);
    }

    public void bindThis(Value v) {
        thisValue = v;
    }
    public void assignLocal(String s, Value v) {
        variables.put(s, v);
    }
}
