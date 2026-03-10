package main.interpreter.values.builtins;

import main.interpreter.values.InvalidOperationException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public final class ListValue extends Value {
    final List<Value> elements;
    private final HashMap<String, CompiledFunctionValue> members = new HashMap<>();
    public ListValue(List<Value> elements) {
        this.elements = elements;
        members.put("append", new CompiledFunctionValue() {
            public Value call(List<Value> args) {
                ListValue.this.elements.addAll(args);
                return ListValue.this;
            }
        });
        members.put("length", new CompiledFunctionValue() {
            public Value call(List<Value> args) {
                return NumericValue.of(elements.size());
            }
        });
        members.put("remove", new CompiledFunctionValue() {
                    @Override
                    public Value call(List<Value> args) {
                        if (args.size() == 1 && args.getFirst() instanceof NumericValue nv) {
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
            return new ListValue(newElements);
        }
        throw new InvalidOperationException("Can't use '+' on lists");
    }
    public Value index(Value v) {
        if (!(v instanceof NumericValue nv))
            if (v instanceof RangeValue rv)
                return indexRange(rv);
            else
                throw new InvalidOperationException("Can't index a list by a non-number");
        if (nv.number >= elements.size())
            throw new InvalidOperationException("Can't index a list by a number larger than "+elements.size());
        int i = (int) nv.number;
        if (nv.number != i)
            throw new InvalidOperationException("Can't index a list by a non-integer number");
        return elements.get(i);
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
                return BooleanValue.fromBoolean(false);
            for (int i = 0; i < lv.elements.size(); i++) {
                BooleanValue val = elements.get(i).eq(lv.elements.get(i));
                if (!val.value)
                    return BooleanValue.fromBoolean(false);
            }
            return BooleanValue.fromBoolean(true);
        }
        return BooleanValue.fromBoolean(false);
    }
    public Value neg() {
        throw new InvalidOperationException("Can't use '-' on lists");
    }
    public Value inv() {
        throw new InvalidOperationException("Can't use '!' on lists");
    }
    public Value toNumber() {
        return NumericValue.of(elements.size());
    }
    public BooleanValue isTruthy() {
        return BooleanValue.fromBoolean(!elements.isEmpty());
    }
    public BooleanValue neq(Value v) {
        if (v instanceof ListValue lv) {
            if (lv.elements.size() != elements.size())
                return BooleanValue.fromBoolean(true);
            for (int i = 0; i < lv.elements.size(); i++) {
                BooleanValue val = elements.get(i).neq(lv.elements.get(i));
                if (val.value)
                    return BooleanValue.fromBoolean(true);
            }
            return BooleanValue.fromBoolean(false);
        }
        return BooleanValue.fromBoolean(true);
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
    public Value setMember(String s , Value v) {
        throw new InvalidOperationException("Can't set member value "+s+" in a list");
    }
    public Value setIndex(Value v, Value w) {
        if (!(v instanceof NumericValue nv))
            throw new InvalidOperationException("Can't set index in a list by a non-number");
        if (nv.number < 0)
            throw new InvalidOperationException("Can't set index in a list by a negative number");
        if (nv.number >= elements.size())
            throw new InvalidOperationException("Can't set index in a list by a number larger than "+elements.size());
        this.elements.set((int) nv.number, w);
        return w;
    }
    public Value merge(Value v) {
        if (v instanceof ListValue lv) {
            ArrayList<Value> newElements = new ArrayList<>(elements);
            newElements.addAll(lv.elements);
            return new ListValue(newElements);
        }
        throw new InvalidOperationException("Can't merge a list with a non-list");
    }
    public Value contains(Value v) {
        for (Value e : elements) {
            if (e.eq(v).value)
                return BooleanValue.fromBoolean(true);
        }
        return BooleanValue.fromBoolean(false);
    }
    public int hashCode() {
        int hashcode = 1;
        for (Value e : elements) {
            hashcode = 31 * hashcode + e.hashCode();
        }
        return hashcode;
    }
    public Value last() {
        if (elements.isEmpty())
            throw new InvalidOperationException("Cannot get last element of an empty list");
        return elements.getLast();
    }
    public Value first() {
        if (elements.isEmpty())
            throw new InvalidOperationException("Cannot get first element of an empty list");
        return elements.getFirst();
    }
    Value indexRange(RangeValue rv) {
        if (rv.order == RangeValue.ASCENDING) {
            return new ListValue(elements.subList((int) rv.l, (int) rv.r+1));
        }
        return new ListValue(elements.subList((int) rv.r, (int) rv.l+1));
    }

    @Override
    public Iterator<Value> iterator() {
        return this.elements.iterator();
    }
}
