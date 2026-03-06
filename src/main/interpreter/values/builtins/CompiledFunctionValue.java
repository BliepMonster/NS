package main.interpreter.values.builtins;

import java.util.List;

public abstract non-sealed class CompiledFunctionValue extends Value {
    public Value add(Value v) {
        if (!(v instanceof CompiledFunctionValue)) {
            if (v instanceof InterpretedFunctionValue iv) {
                return new CompiledFunctionValue() {
                    public Value call(List<Value> args) {
                        return CompiledFunctionValue.this.call(args).add(iv.call(args));
                    }
                };
            }
            return new CompiledFunctionValue() {
                public Value call(List<Value> args) {
                    return CompiledFunctionValue.this.call(args).add(v);
                }
            };
        } else {
            return new CompiledFunctionValue() {
                public Value call(List<Value> args) {
                    return CompiledFunctionValue.this.call(args).add(v.call(args));
                }
            };
        }
    }
    public String toString() {
        return "<generated function>";
    }
    public Value sub(Value v) {
        if (!(v instanceof CompiledFunctionValue)) {
            if (v instanceof InterpretedFunctionValue iv) {
                return new CompiledFunctionValue() {
                    public Value call(List<Value> args) {
                        return CompiledFunctionValue.this.call(args).sub(iv.call(args));
                    }
                };
            }
            return new CompiledFunctionValue() {
                public Value call(List<Value> args) {
                    return CompiledFunctionValue.this.call(args).sub(v);
                }
            };
        } else {
            return new CompiledFunctionValue() {
                public Value call(List<Value> args) {
                    return CompiledFunctionValue.this.call(args).sub(v.call(args));
                }
            };
        }
    }
    public Value mul(Value v) {
        if (!(v instanceof CompiledFunctionValue)) {
            if (v instanceof InterpretedFunctionValue iv) {
                return new CompiledFunctionValue() {
                    public Value call(List<Value> args) {
                        return CompiledFunctionValue.this.call(args).mul(iv.call(args));
                    }
                };
            }
            return new CompiledFunctionValue() {
                public Value call(List<Value> args) {
                    return CompiledFunctionValue.this.call(args).mul(v);
                }
            };
        } else {
            return new CompiledFunctionValue() {
                public Value call(List<Value> args) {
                    return CompiledFunctionValue.this.call(args).mul(v.call(args));
                }
            };
        }
    }
    public Value div(Value v) {
        if (!(v instanceof CompiledFunctionValue)) {
            if (v instanceof InterpretedFunctionValue iv) {
                return new CompiledFunctionValue() {
                    public Value call(List<Value> args) {
                        return CompiledFunctionValue.this.call(args).div(iv.call(args));
                    }
                };
            }
            return new CompiledFunctionValue() {
                public Value call(List<Value> args) {
                    return CompiledFunctionValue.this.call(args).div(v);
                }
            };
        } else {
            return new CompiledFunctionValue() {
                public Value call(List<Value> args) {
                    return CompiledFunctionValue.this.call(args).div(v.call(args));
                }
            };
        }
    }
    public Value mod(Value v) {
        if (!(v instanceof CompiledFunctionValue)) {
            if (v instanceof InterpretedFunctionValue iv) {
                return new CompiledFunctionValue() {
                    public Value call(List<Value> args) {
                        return CompiledFunctionValue.this.call(args).mod(iv.call(args));
                    }
                };
            }
            return new CompiledFunctionValue() {
                public Value call(List<Value> args) {
                    return CompiledFunctionValue.this.call(args).mod(v);
                }
            };
        } else {
            return new CompiledFunctionValue() {
                public Value call(List<Value> args) {
                    return CompiledFunctionValue.this.call(args).mod(v.call(args));
                }
            };
        }
    }
    public Value getMember(String s) {
        throw new UnsupportedOperationException("Cannot get member value "+s+" from a function");
    }
    public Value index(Value v) {
        throw new UnsupportedOperationException("Cannot index a function");
    }
    public abstract Value call(List<Value> args);
    public BooleanValue eq(Value v) {
        return BooleanValue.fromBoolean(equals(v));
    }
    public BooleanValue neq(Value v) {
        return BooleanValue.fromBoolean(!equals(v));
    }
    public BooleanValue gt(Value v) {
        throw new UnsupportedOperationException("Cannot compare a function to a number");
    }
    public BooleanValue gte(Value v) {
        throw new UnsupportedOperationException("Cannot compare a function to a number");
    }
    public BooleanValue lt(Value v) {
        throw new UnsupportedOperationException("Cannot compare a function to a number");
    }
    public BooleanValue lte(Value v) {
        throw new UnsupportedOperationException("Cannot compare a function to a number");
    }
    public Value neg() {
        return new CompiledFunctionValue() {
            public Value call(List<Value> args) {
                return CompiledFunctionValue.this.call(args).neg();
            }
        };
    }
    public Value inv() {
        return new CompiledFunctionValue() {
            public Value call(List<Value> args) {
                return CompiledFunctionValue.this.call(args).inv();
            }
        };
    }
    public BooleanValue isTruthy() {
        return BooleanValue.fromBoolean(true);
    }
    public Value toNumber() {
        throw new UnsupportedOperationException("Cannot convert a function to a number");
    }
    public Value setMember(String s, Value v) {
        throw new UnsupportedOperationException("Cannot set member value "+s+" in a function");
    }
    public Value setIndex(Value v, Value w) {
        throw new UnsupportedOperationException("Cannot set index in a function");
    }
    public Value merge(Value v) {
        if (!(v instanceof CompiledFunctionValue)) {
            if (v instanceof InterpretedFunctionValue iv) {
                return new CompiledFunctionValue() {
                    public Value call(List<Value> args) {
                        return CompiledFunctionValue.this.call((List.of(iv.call(args))));
                    }
                };
            }
            return new CompiledFunctionValue() {
                public Value call(List<Value> args) {
                    return CompiledFunctionValue.this.call(List.of(v));
                }
            };
        } else {
            return new CompiledFunctionValue() {
                public Value call(List<Value> args) {
                    return CompiledFunctionValue.this.call(List.of(v.call(args)));
                }
            };
        }
    }
}
