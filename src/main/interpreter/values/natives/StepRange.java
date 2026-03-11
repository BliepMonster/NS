package main.interpreter.values.natives;

import main.interpreter.values.builtins.NumericValue;
import main.interpreter.values.builtins.RangeValue;
import main.interpreter.values.builtins.Value;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class StepRange implements Iterator<Value> {
    private final RangeValue range;
    private final double step;
    private double current;
    public StepRange(RangeValue range, double step) {
        current = range.l;
        this.step = range.order == RangeValue.ASCENDING ? step : -step;
        this.range = range;
    }
    public boolean hasNext() {
        return current <= range.r;
    }
    public Value next() {
        if (!hasNext())
            throw new NoSuchElementException();
        var r = current;
        current += step;
        return NumericValue.of(r);
    }
    public int length() {
        StepRange copy = new StepRange(range, step);
        int count = 0;
        while (copy.hasNext()) {
            copy.next();
            count++;
        }
        return count;
    }
}
