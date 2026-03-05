package main.interpreter;

import main.interpreter.values.Value;

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
        } else if (contains(name)) {
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
    // to be overridden by class contexts
    public Value getThis() {
        return lookup("this");
    }
    public void bindThis(Value v) {
        variables.put("this", v);
    }
    public void assignLocal(String s, Value v) {
        variables.put(s, v);
    }
}
