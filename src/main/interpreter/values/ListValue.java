package main.interpreter.values;

import main.interpreter.Executor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public final class ListValue extends Value {
    private ArrayList<Value> elements;
    public final Executor executor;
    private HashMap<String, CompiledFunctionValue> members = new HashMap<>();
    public ListValue(ArrayList<Value> elements, Executor executor) {
        this.executor = executor;
        this.elements = elements;
        members.put("append", new CompiledFunctionValue(executor) {
            public Value call(List<Value> args) {
                ListValue.this.elements.addAll(args);
                return ListValue.this;
            }
        });
        members.put("length", new CompiledFunctionValue(executor) {
            public Value call(List<Value> args) {
                return new NumericValue(elements.size(), executor);
            }
        });
        members.put("remove", new CompiledFunctionValue(executor) {
                    @Override
                    public Value call(List<Value> args) {
                        if (args.size() == 1 && args.get(0) instanceof NumericValue nv) {
                            if (nv.number < 0)
                                throw new InvalidOperationException("Can't remove a negative index from a list");
                            if (nv.number >= elements.size())
                                throw new InvalidOperationException("Can't remove a number larger than the length of the list");
                            if (nv.number % 1 != 0)
                                throw new InvalidOperationException("Can't remove a non-integer number from a list");
                            ListValue.this.elements.remove((int) nv.number);
                            return ListValue.this;
                        }
                        ListValue.this.elements.removeAll(args);
                        return ListValue.this;
                    }
                }
        );
    }
    public Value add(Value v) {
        if (v instanceof ListValue lv) {
            ArrayList<Value> newElements = new ArrayList<>(elements);
            newElements.addAll(lv.elements);
            return new ListValue(newElements, executor);
        }
        throw new InvalidOperationException("Can't use '+' on lists");
    }
    public Value index(Value v) {
        if (!(v instanceof NumericValue nv))
            throw new InvalidOperationException("Can't index a list by a non-number");
        if (nv.number < 0)
            throw new InvalidOperationException("Can't index a list by a negative number");
        if (nv.number >= elements.size())
            throw new InvalidOperationException("Can't index a list by a number larger than "+elements.size());
        if (nv.number % 1 != 0)
            throw new InvalidOperationException("Can't index a list by a non-integer number");
        return elements.get((int) nv.number);
    }
    public Value sub(Value v) {
        throw new InvalidOperationException("Can't use '-' on lists");
    }
    public Value mul(Value v) {
        throw new InvalidOperationException("Can't use '*' on lists");
    }
    public Value div(Value v) {
        throw new InvalidOperationException("Can't use '/' on lists");
    }
    public Value mod(Value v) {
        throw new InvalidOperationException("Can't use '%' on lists");
    }
    public Value getMember(String mem) {
        if (members.containsKey(mem))
            return members.get(mem);
        throw new InvalidOperationException("Can't get member value "+mem+" from a list");
    }
    public Value call(List<Value> args) {
        throw new InvalidOperationException("Can't call a list");
    }
    public BooleanValue eq(Value v) {
        if (v instanceof ListValue lv) {
            if (lv.elements.size() != elements.size())
                return new BooleanValue(false, executor);
            for (int i = 0; i < lv.elements.size(); i++) {
                BooleanValue val = elements.get(i).eq(lv.elements.get(i));
                if (!val.value)
                    return new BooleanValue(false, executor);
            }
            return new BooleanValue(true, executor);
        }
        return new BooleanValue(false, executor);
    }
    public Value neg() {
        throw new InvalidOperationException("Can't use '-' on lists");
    }
    public Value inv() {
        throw new InvalidOperationException("Can't use '!' on lists");
    }
    public Value toNumber() {
        return new NumericValue(elements.size(), executor);
    }
    public BooleanValue isTruthy() {
        return new BooleanValue(!elements.isEmpty(), executor);
    }
    public BooleanValue neq(Value v) {
        if (v instanceof ListValue lv) {
            if (lv.elements.size() != elements.size())
                return new BooleanValue(true, executor);
            for (int i = 0; i < lv.elements.size(); i++) {
                BooleanValue val = elements.get(i).neq(lv.elements.get(i));
                if (val.value)
                    return new BooleanValue(true, executor);
            }
            return new BooleanValue(false, executor);
        }
        return new BooleanValue(true, executor);
    }
    public BooleanValue lt(Value v) {
        throw new InvalidOperationException("Can't use '<' on lists");
    }
    public BooleanValue lte(Value v) {
        throw new InvalidOperationException("Can't use '<=' on lists");
    }
    public BooleanValue gt(Value v) {
        throw new InvalidOperationException("Can't use '>' on lists");
    }
    public BooleanValue gte(Value v) {
        throw new InvalidOperationException("Can't use '>=' on lists");
    }
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < elements.size(); i++) {
            sb.append(elements.get(i).toString());
            if (i != elements.size() - 1)
                sb.append(", ");
        }
        sb.append("]");
        return sb.toString();
    }
}
