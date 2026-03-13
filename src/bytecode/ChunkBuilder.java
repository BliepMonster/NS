package bytecode;

import main.interpreter.values.builtins.Value;
import org.jetbrains.annotations.*;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import static bytecode.Instructions.*;

public final class ChunkBuilder {
    private static sealed abstract class Instruction permits Operation, ValueAccess, Jump, Label {}
    private static final class Operation extends Instruction {
        public final byte opcode;
        public final int[] operands;
        public Operation(byte opcode, int... operands) {
            this.opcode = opcode;
            this.operands = operands;
        }
    }
    public static final class Label extends Instruction {}
    public static final class Jump extends Instruction {
        public final byte opcode;
        Label target;
        /// if label is null, PATCH IT LATER!
        public Jump(byte opcode, @Nullable Label operand) {
            this.opcode = opcode;
            this.target = operand;
        }
        public void patch(Label operand) {
            this.target = operand;
        }
    }
    private static final class ValueAccess extends Instruction {
        public final Value value;
        public ValueAccess(@NotNull Value index) {
            this.value = index;
        }
    }
    private final ArrayList<Instruction> operations;
    public ChunkBuilder() {
        operations = new ArrayList<>();
    }
    @Contract("_ -> this")
    public @NotNull ChunkBuilder pushValue(@NotNull Value v) {
        operations.add(new ValueAccess(v));
        return this;
    }
    @Contract("_, _ -> this")
    public @NotNull ChunkBuilder pushOperation(byte opcode, int... operands) {
        operations.add(new Operation(opcode, operands));
        return this;
    }
    @Contract(value = "_ -> new", pure = true)
    private byte @NotNull[] encode(int i) {
        if (i < 256) {
            return new byte[] {(byte) i};
        }
        return new byte[] {(byte) (i >> 16), (byte) (i >> 8), (byte) i};
    }
    @Contract(value = "_ -> new", pure = true)
    private byte @NotNull[] encodeLarge(int i) {
        return new byte[] {(byte) (i >> 16), (byte) (i >> 8), (byte) i};
    }
    static sealed abstract class NewInstruction permits RegularInstruction, UnresolvedJumpInstruction {
        abstract int size();
    }
    static final class RegularInstruction extends NewInstruction {
        final byte opcode;
        public RegularInstruction(byte opcode) {
            this.opcode = opcode;
        }
        @Override
        public int size() {
            return 1;
        }
    }
    static final class UnresolvedJumpInstruction extends NewInstruction {
        public final Label target;
        public final byte opcode;
        public UnresolvedJumpInstruction(byte opcode, Label target) {
            this.target = target;
            this.opcode = opcode;
        }
        @Override
        public int size() {
            return 4; // 3-byte operand
        }
    }
    public Label label() {
        Label l = new Label();
        operations.add(l);
        return l;
    }
    record ValueArray(ArrayList<Value> values) {
        public int add(Value value) {
            // what if the value is mutable, like a list or set?
            // we need duplicates for this to work, which is why
            // the value array may be too large
            // TODO: strings & numbers & bignums & ranges & classes are immutable; don't copy them
            int index = values.size();
            values.add(value);
            return index;
        }

        public @NotNull Value @NotNull [] build() {
            return values.toArray(new Value[0]);
        }
    }

    @Contract(" -> new")
    public @NotNull Chunk build() {
        HashMap<Label, Integer> labels = new HashMap<>();
        ValueArray values = new ValueArray(new ArrayList<>());
        ArrayList<NewInstruction> instr = new ArrayList<>();
        for (Instruction i : operations) {
            if (i instanceof ValueAccess v) {
                int index = values.add(v.value);
                if (index < 256) {
                    instr.add(new RegularInstruction(OP_CONST));
                    instr.add(new RegularInstruction((byte) index));
                } else {
                    instr.add(new RegularInstruction(OP_CONST_LONG));
                    byte[] b = encode(index);
                    for (byte by : b) {
                        instr.add(new RegularInstruction(by));
                    }
                }
            } else if (i instanceof Operation o) {
                instr.add(new RegularInstruction(o.opcode));
                for (int operand : o.operands) {
                    byte[] b = encode(operand);
                    for (byte bi : b) {
                        instr.add(new RegularInstruction(bi));
                    }
                }
            } else if (i instanceof Jump j) {
                if (j.target == null) {
                    throw new RuntimeException("jump with no target");
                }
                if (!labels.containsKey(j.target)) {
                    instr.add(new UnresolvedJumpInstruction(j.opcode, j.target));
                } else {
                    instr.add(new RegularInstruction(j.opcode));
                    byte[] b = encodeLarge(labels.get(j.target));
                    for (byte bi : b) {
                        instr.add(new RegularInstruction(bi));
                    }
                }
            } else if (i instanceof Label l) {
                labels.put(l, instr.size());
            }
        }
        return new Chunk(values.build(), resolve(instr, labels).toByteArray());
    }
    @Contract(value = "_ -> new")
    public @NotNull Jump jump(byte opcode) {
        Jump j = new Jump(opcode, null);
        operations.add(j);
        return j;
    }
    @Contract(pure = true)
    public int size() {
        return operations.size();
    }
    static byte[] generateJump(byte opcode, int target) {
        byte[] bytes = new byte[4];
        bytes[0] = opcode;
        bytes[1] = (byte) (target >> 16);
        bytes[2] = (byte) (target >> 8);
        bytes[3] = (byte) target;
        return bytes;
    }
    static ByteArrayOutputStream resolve(@NotNull ArrayList<NewInstruction> compiled, @NotNull HashMap<Label, Integer> labels) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (NewInstruction instr : compiled) {
            if (instr instanceof UnresolvedJumpInstruction uj) {
                Label target = uj.target;
                if (!labels.containsKey(target))
                    throw new RuntimeException("unresolved jump");
                out.writeBytes(generateJump(uj.opcode, labels.get(target)));
            } else if (instr instanceof RegularInstruction r) {
                out.write(r.opcode);
            } else throw new RuntimeException("unknown instruction type: should be impossible");
        }
        return out;
    }
}
