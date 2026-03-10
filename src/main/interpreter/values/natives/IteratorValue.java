package main.interpreter.values.natives;

import main.interpreter.values.InvalidOperationException;
import main.interpreter.values.builtins.BuiltinObjectValue;
import main.interpreter.values.builtins.Value;

import java.util.Iterator;

public class IteratorValue extends BuiltinObjectValue<Iterator<Value>> {
    public IteratorValue(Iterator<Value> value) {
        super(value);
    }
    public Value getMember(String s) {
        throw new InvalidOperationException("Native object does not have members");
    }
    public Value next() {
        return obj.next();
    }
    public boolean hasNext() {
        return obj.hasNext();
    }
}
