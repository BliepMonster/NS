package main.interpreter;

import main.Statement;
import main.expr.Expression;
import main.interpreter.values.InterpretedFunctionValue;
import main.interpreter.values.Value;

import java.util.List;

public interface Executor {
    Value evaluate(Expression expr);
    void execute(Statement stmt);
    // fn is the function to call
    // args is the list of arguments
    Value callFunction(InterpretedFunctionValue fn, List<Value> args);
}
