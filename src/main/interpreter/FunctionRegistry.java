package main.interpreter;

import main.interpreter.values.InvalidOperationException;
import main.interpreter.values.builtins.*;
import main.interpreter.values.natives.FileReaderValue;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;

public class FunctionRegistry {
    public static final HashMap<String, CompiledFunctionValue> functions = new HashMap<>();
    static {
        functions.put("print", new CompiledFunctionValue() {
            @Override
            public Value call(List<Value> args) {
                for (Value v : args) {
                    System.out.print(v.toString());
                    System.out.print(" ");
                }
                return NullValue.INSTANCE;
            }
        });
        functions.put("println", new CompiledFunctionValue() {
            @Override
            public Value call(List<Value> args) {
                for (Value v : args) {
                    System.out.print(v.toString());
                    System.out.print(" ");
                }
                System.out.println();
                return NullValue.INSTANCE;
            }
        });
        functions.put("input", new CompiledFunctionValue() {
            @Override
            public Value call(List<Value> args) {
                if (!args.isEmpty()) {
                    throw new InvalidOperationException("input() takes no arguments");
                }
                System.out.print("> ");
                String input = System.console().readLine();
                return new StringValue(input);
            }
        });
        functions.put("str", new CompiledFunctionValue() {
            @Override
            public Value call(List<Value> args) {
                if (args.size() != 1) {
                    throw new InvalidOperationException("str() takes exactly one argument");
                }
                return new StringValue(args.getFirst().toString());
            }
        });
        functions.put("clock", new CompiledFunctionValue() {
            @Override
            public Value call(List<Value> args) {
                if (!args.isEmpty()) {
                    throw new InvalidOperationException("clock() takes no arguments");
                }
                return NumericValue.of(System.currentTimeMillis());
            }
        });
        functions.put("openFileReadingHandle", new CompiledFunctionValue() {
            @Override
            public Value call(List<Value> args) {
                if (args.size() != 1) {
                    throw new InvalidOperationException("openFile() takes exactly one argument");
                }
                return new FileReaderValue(Paths.get(args.getFirst().toString()));
            }
        });
    }
}
