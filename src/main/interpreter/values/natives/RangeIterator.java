package main.interpreter.values.natives;

import main.interpreter.values.InvalidOperationException;
import main.interpreter.values.builtins.BuiltinObjectValue;
import main.interpreter.values.builtins.Value;

import java.util.Iterator;

public class RangeIterator extends BuiltinObjectValue<StepRange> {
    public Value getMember(String s) {
        throw new InvalidOperationException("Range step iterators have no members");
    }
    public RangeIterator(StepRange sr) {
        super(sr);
    }
    @Override
    public Iterator<Value> iterator() {
        return this.obj;
    }
    public int size() {
        return obj.length();
    }
}
