package main.interpreter.values.builtins;

import main.interpreter.values.InvalidOperationException;

import java.util.HashMap;
import java.util.List;

public final class VectorValue extends Value {
    public final NumericValue[] elements;
    public final HashMap<String, CompiledFunctionValue> members = new HashMap<>();
    public VectorValue(NumericValue[] elements) {
        this.elements = elements;
        members.put("expand", new CompiledFunctionValue() {
            @Override
            public Value call(List<Value> args) {
                if (args.size() != 1)
                    throw new InvalidOperationException("expand() takes exactly one argument");
                Value v = args.getFirst();
                if (!(v instanceof NumericValue nv))
                    throw new InvalidOperationException("expand() argument must be numeric");
                if (nv.number < 0)
                    throw new InvalidOperationException("expand() argument must be a positive number");
                if (nv.number % 1 != 0)
                    throw new InvalidOperationException("expand() argument must be an integer");
                if (nv.number < VectorValue.this.elements.length)
                    throw new InvalidOperationException("expand() argument must be larger than the current length of the vector");
                NumericValue[] newElements = new NumericValue[(int) nv.number];
                System.arraycopy(elements, 0, newElements, 0, elements.length);
                for (int i = elements.length; i < newElements.length; i++) {
                    newElements[i] = NumericValue.ZERO;
                }
                return new VectorValue(newElements);
            }
        });
    }
    public Value index(Value index) {
        if (!(index instanceof NumericValue n))
            throw new InvalidOperationException("Index must be numeric");
        if (n.number < 0 || n.number >= elements.length)
            throw new InvalidOperationException("Index out of bounds");
        if (n.number % 1 != 0)
            throw new InvalidOperationException("Index must be an integer");
        return elements[(int) n.number];
    }
    public Value add(Value v) {
        if (!(v instanceof VectorValue vv))
            throw new InvalidOperationException("Cannot add a vector to a non-vector");
        if (elements.length != vv.elements.length)
            throw new InvalidOperationException("Cannot add vectors of different length");
        NumericValue[] newElements = new NumericValue[elements.length];
        for (int i = 0; i < newElements.length; i++) {
            newElements[i] = (NumericValue) elements[i].add(vv.elements[i]);
        }
        return new VectorValue(newElements);
    }
    public Value sub(Value v) {
        if (!(v instanceof VectorValue vv))
            throw new InvalidOperationException("Cannot subtract a vector from a non-vector");
        if (elements.length != vv.elements.length)
            throw new InvalidOperationException("Cannot add vectors of different length");
        NumericValue[] newElements = new NumericValue[elements.length];
        for (int i = 0; i < newElements.length; i++) {
            newElements[i] = (NumericValue) elements[i].sub(vv.elements[i]);
        }
        return new VectorValue(newElements);
    }
    public Value mul(Value v) {
        if (!(v instanceof NumericValue n))
            throw new InvalidOperationException("Cannot multiply a vector by a non-number");
        NumericValue[] newElements = new NumericValue[elements.length];
        for (int i = 0; i < newElements.length; i++) {
            newElements[i] = (NumericValue) elements[i].mul(n);
        }
        return new VectorValue(newElements);
    }
    public Value div(Value v) {
        if (!(v instanceof NumericValue n))
            throw new InvalidOperationException("Cannot divide a vector by a non-number");
        NumericValue[] newElements = new NumericValue[elements.length];
        for (int i = 0; i < newElements.length; i++) {
            newElements[i] = (NumericValue) elements[i].div(n);
        }
        return new VectorValue(newElements);
    }
    public Value mod(Value v) {
        if (!(v instanceof NumericValue n))
            throw new InvalidOperationException("Cannot mod a vector by a non-number");
        NumericValue[] newElements = new NumericValue[elements.length];
        for (int i = 0; i < newElements.length; i++) {
            newElements[i] = (NumericValue) elements[i].mod(n);
        }
        return new VectorValue(newElements);
    }
    public Value getMember(String s) {
        if (!members.containsKey(s))
            throw new InvalidOperationException("Cannot get member value "+s+" from a vector");
        return members.get(s);
    }
    public Value call(List<Value> args) {
        throw new InvalidOperationException("Cannot call a vector");
    }
    public BooleanValue eq(Value v) {
        if (!(v instanceof VectorValue vv))
            return BooleanValue.fromBoolean(false);
        if (elements.length != vv.elements.length)
            return BooleanValue.fromBoolean(false);
        for (int i = 0; i < elements.length; i++) {
            NumericValue n1 = elements[i];
            NumericValue n2 = vv.elements[i];
            if (n1.neq(n2).value)
                return BooleanValue.fromBoolean(false);
        }
        return BooleanValue.fromBoolean(true);
    }
    public Value neg() {
        NumericValue[] newElements = new NumericValue[elements.length];
        for (int i = 0; i < newElements.length; i++) {
            newElements[i] = (NumericValue) elements[i].neg();
        }
        return new VectorValue(newElements);
    }
    public Value inv() {
        throw new InvalidOperationException("Cannot invert a vector");
    }
    public BooleanValue isTruthy() {
        return BooleanValue.fromBoolean(elements.length != 0);
    }
    public BooleanValue neq(Value v) {
        if (!(v instanceof VectorValue vv))
            return BooleanValue.fromBoolean(true);
        if (elements.length != vv.elements.length)
            return BooleanValue.fromBoolean(true);
        for (int i = 0; i < elements.length; i++) {
            NumericValue n1 = elements[i];
            NumericValue n2 = vv.elements[i];
            if (n1.neq(n2).value)
                return BooleanValue.fromBoolean(true);
        }
        return BooleanValue.fromBoolean(false);
    }
    public BooleanValue lt(Value v) {
        throw new InvalidOperationException("Cannot compare a vector to a number");
    }
    public BooleanValue lte(Value v) {
        throw new InvalidOperationException("Cannot compare a vector to a number");
    }
    public BooleanValue gt(Value v) {
        throw new InvalidOperationException("Cannot compare a vector to a number");
    }
    public BooleanValue gte(Value v) {
        throw new InvalidOperationException("Cannot compare a vector to a number");
    }
    public Value toNumber() {
        return NumericValue.of(elements.length);
    }
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('(');
        for (int i = 0; i < elements.length; i++) {
            sb.append(elements[i].toString());
            if (i < elements.length - 1)
                sb.append(", ");
        }
        sb.append(')');
        return sb.toString();
    }
    public Value setMember(String s , Value v) {
        throw new InvalidOperationException("Cannot set member of vector");
    }
    public Value setIndex(Value v, Value w) {
        if (!(v instanceof NumericValue n))
            throw new InvalidOperationException("Index must be numeric");
        if (n.number < 0 || n.number >= elements.length)
            throw new InvalidOperationException("Index out of bounds");
        if (n.number % 1 != 0)
            throw new InvalidOperationException("Index must be an integer");
        if (!(w instanceof NumericValue nw))
            throw new InvalidOperationException("Cannot set index of vector to non-numeric");
        elements[(int) n.number] = nw;
        return w;
    }
    public Value merge(Value v) {
        throw new InvalidOperationException("Cannot merge a vector with an object");
    }
    public int hashCode() {
        int hashcode = 1;
        for (NumericValue n : elements) {
            hashcode = 31 * hashcode + n.hashCode();
        }
        return hashcode;
    }
}
