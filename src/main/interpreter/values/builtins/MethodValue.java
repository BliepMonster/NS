package main.interpreter.values.builtins;

import main.interpreter.values.InvalidOperationException;

import java.util.List;

public final class MethodValue extends InterpretedFunctionValue {
    private final Value context;
    public MethodValue(InterpretedFunctionValue fn, Value context) {
        super(fn.parameters, fn.body, fn.closure);
        this.context = context;
    }
    @Override
    public Value call(List<Value> args) {
        if (args.size() != parameters.size()) {
            throw new InvalidOperationException("Expected "+parameters.size()+" arguments, got "+args.size());
        }
        return ExecutorHolder.EXECUTOR.callFunction(this, args, context);
    }
}
