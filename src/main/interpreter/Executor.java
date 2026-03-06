package main.interpreter;

import main.Statement;
import main.expr.Expression;
import main.interpreter.values.builtins.InterpretedFunctionValue;
import main.interpreter.values.builtins.Value;

import java.util.List;

public interface Executor {
    Value evaluate(Expression expr);
    void execute(Statement stmt);
    // fn is the function to call
    // args is the list of arguments
    // context is This or NULL
    Value callFunction(InterpretedFunctionValue fn, List<Value> args, Value context);
}
