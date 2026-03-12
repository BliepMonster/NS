package main.interpreter.values.builtins;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

public final class BigNumberValue extends Value {
    public static final BigNumberValue ZERO = new BigNumberValue(BigDecimal.ZERO);
    public static final BigNumberValue ONE = new BigNumberValue(BigDecimal.ONE);
    public final BigDecimal num;
    private BigNumberValue(BigDecimal num) {
        this.num = num;
    }
    public static BigNumberValue of(BigDecimal n) {
        if (n.equals(BigDecimal.ZERO)) return ZERO;
        else if (n.equals(BigDecimal.ONE)) return ONE;
        return new BigNumberValue(n);
    }
    public Value add(Value v) {
        if (v instanceof NumericValue nv) {
            return BigNumberValue.of(num.add(BigDecimal.valueOf(nv.number)));
        } else if (v instanceof BigNumberValue bv) {
            return BigNumberValue.of(num.add(bv.num));
        }
        throw new UnsupportedOperationException("Cannot add "+v.getClass().getSimpleName()+" to a BigNumberValue");
    }
    public Value sub(Value v) {
        if (v instanceof NumericValue nv) {
            return BigNumberValue.of(num.subtract(BigDecimal.valueOf(nv.number)));
        } else if (v instanceof BigNumberValue bv) {
            return BigNumberValue.of(num.subtract(bv.num));
        }
        throw new UnsupportedOperationException("Cannot subtract "+v.getClass().getSimpleName()+" from a BigNumberValue");
    }
    public Value mul(Value v) {
        if (v instanceof NumericValue nv) {
            return BigNumberValue.of(num.multiply(BigDecimal.valueOf(nv.number)));
        } else if (v instanceof BigNumberValue bv) {
            return BigNumberValue.of(num.multiply(bv.num));
        }
        throw new UnsupportedOperationException("Cannot multiply "+v.getClass().getSimpleName()+" to a BigNumberValue");
    }
    public Value div(Value v) {
        if (v instanceof NumericValue nv) {
            return BigNumberValue.of(num.divide(BigDecimal.valueOf(nv.number)));
        } else if (v instanceof BigNumberValue bv) {
            return BigNumberValue.of(num.divide(bv.num));
        }
        throw new UnsupportedOperationException("Cannot divide "+v.getClass().getSimpleName()+" by a BigNumberValue");
    }
    public Value mod(Value v) {
        if (v instanceof NumericValue nv) {
            return BigNumberValue.of(num.remainder(BigDecimal.valueOf(nv.number)));
        } else if (v instanceof BigNumberValue bv) {
            return BigNumberValue.of(num.remainder(bv.num));
        }
        throw new UnsupportedOperationException("Cannot mod "+v.getClass().getSimpleName()+" by a BigNumberValue");
    }
    public Value getMember(String mem) {
        throw new UnsupportedOperationException("Cannot get member value "+mem+" from a BigNumberValue");
    }
    public Value index(Value v) {
        throw new UnsupportedOperationException("Cannot index a BigNumberValue");
    }
    public Value call(List<Value> args) {
        throw new UnsupportedOperationException("Cannot call a BigNumberValue");
    }
    // bignum(0) != 0
    public BooleanValue eq(Value v) {
        if (v instanceof BigNumberValue bv) {
            return BooleanValue.fromBoolean(num.compareTo(bv.num) == 0);
        }
        return BooleanValue.FALSE;
    }
    public BooleanValue neq(Value v) {
        if (v instanceof BigNumberValue bv) {
            return BooleanValue.fromBoolean(num.compareTo(bv.num) != 0);
        }
        return BooleanValue.TRUE;
    }
    public Value neg() {
        return BigNumberValue.of(num.negate());
    }
    public Value inv() {
        throw new UnsupportedOperationException("Cannot invert a BigNumberValue");
    }
    public BooleanValue isTruthy() {
        return BooleanValue.fromBoolean(num.compareTo(BigDecimal.ZERO) != 0);
    }
    public BooleanValue lt(Value v) {
        if (v instanceof NumericValue nv) {
            return BooleanValue.fromBoolean(num.compareTo(BigDecimal.valueOf(nv.number)) < 0);
        } else if (v instanceof BigNumberValue bv) {
            return BooleanValue.fromBoolean(num.compareTo(bv.num) < 0);
        }
        throw new UnsupportedOperationException("Cannot compare a BigNumberValue to a "+v.getClass().getSimpleName());
    }
    public BooleanValue lte(Value v) {
        if (v instanceof NumericValue nv) {
            return BooleanValue.fromBoolean(num.compareTo(BigDecimal.valueOf(nv.number)) <= 0);
        } else if (v instanceof BigNumberValue bv) {
            return BooleanValue.fromBoolean(num.compareTo(bv.num) <= 0);
        }
        throw new UnsupportedOperationException("Cannot compare a BigNumberValue to a "+v.getClass().getSimpleName());
    }
    public BooleanValue gt(Value v) {
        if (v instanceof NumericValue nv) {
            return BooleanValue.fromBoolean(num.compareTo(BigDecimal.valueOf(nv.number)) > 0);
        } else if (v instanceof BigNumberValue bv) {
            return BooleanValue.fromBoolean(num.compareTo(bv.num) > 0);
        }
        throw new UnsupportedOperationException("Cannot compare a BigNumberValue to a "+v.getClass().getSimpleName());
    }
    public BooleanValue gte(Value v) {
        if (v instanceof NumericValue nv) {
            return BooleanValue.fromBoolean(num.compareTo(BigDecimal.valueOf(nv.number)) >= 0);
        } else if (v instanceof BigNumberValue bv) {
            return BooleanValue.fromBoolean(num.compareTo(bv.num) >= 0);
        }
        throw new UnsupportedOperationException("Cannot compare a BigNumberValue to a "+v.getClass().getSimpleName());
    }
    public Value toNumber() {
        return NumericValue.of(num.doubleValue());
    }
    private static final DecimalFormat SCI =
            new DecimalFormat("0.#####E0", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
    public String toString() {
        return SCI.format(num);
    }
    public Value setMember(String s, Value v) {
        throw new UnsupportedOperationException("Cannot set member value "+s+" in a BigNumberValue");
    }
    public Value setIndex(Value v, Value w) {
        throw new UnsupportedOperationException("Cannot set index in a BigNumberValue");
    }
    public Value merge(Value v) {
        throw new UnsupportedOperationException("Cannot merge a BigNumberValue with a value");
    }
}
