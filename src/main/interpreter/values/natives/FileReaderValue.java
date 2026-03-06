package main.interpreter.values.natives;

import main.interpreter.values.builtins.BuiltinObjectValue;
import main.interpreter.values.builtins.CompiledFunctionValue;
import main.interpreter.values.builtins.StringValue;
import main.interpreter.values.builtins.Value;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;

public class FileReaderValue extends BuiltinObjectValue<Path> {
    public static abstract class MethodProducer {
        public abstract CompiledFunctionValue produce(FileReaderValue self);
    }
    private static final HashMap<String, MethodProducer> methods = new HashMap<>();
    static {
        methods.put("readAll", new MethodProducer() {
            @Override
            public CompiledFunctionValue produce(FileReaderValue self) {
                return new CompiledFunctionValue() {
                    @Override
                    public Value call(List<Value> args) {
                        if (!args.isEmpty()) {
                            throw new RuntimeException("readAll() takes no arguments");
                        }
                        try {
                            List<String> lines = Files.readAllLines(self.obj, StandardCharsets.UTF_8);
                            StringBuilder sb = new StringBuilder();
                            for (int i = 0; i < lines.size(); i++ ) {
                                sb.append(lines.get(i));
                                if (i != lines.size()-1)
                                    sb.append(System.lineSeparator());
                            }
                            return new StringValue(sb.toString());
                        } catch (IOException e) {
                            throw new RuntimeException("Could not read file: "+e.getMessage());
                        }

                    }
                };
            }
        });
    }
    public FileReaderValue(Path value) {
        super(value);
    }
    public Value getMember(String s) {
        if (!methods.containsKey(s)) {
            throw new RuntimeException("No such method: " + s);
        }
        return methods.get(s).produce(this);
    }
}
