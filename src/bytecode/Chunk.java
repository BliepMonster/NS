package bytecode;

import main.interpreter.values.builtins.Value;

public class Chunk {
    public final Value[] values;
    public final byte[] instructions;
    public Chunk(Value[] values, byte[] instructions) {
        this.values = values;
        this.instructions = instructions;
    }
}
