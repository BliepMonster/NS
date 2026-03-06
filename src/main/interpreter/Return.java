package main.interpreter;

import main.interpreter.values.builtins.Value;

public class Return extends RuntimeException {
    public final Value value;
    public Return(Value value) {
        this.value = value;
    }
}
