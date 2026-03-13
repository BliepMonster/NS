package bytecode;

public class Instructions {
    public static final byte OP_CONST = 0; // const <index>
    public static final byte OP_CONST_LONG = 1; // const <index index index>
    public static final byte OP_ADD = 2;
    public static final byte OP_SUB = 3;
    public static final byte OP_MUL = 4;
    public static final byte OP_DIV = 5;
    public static final byte OP_MOD = 6;
    public static final byte OP_NEG = 7;
    public static final byte OP_NOT = 8;
    public static final byte OP_EQ = 9;
    public static final byte OP_GT = 10;
    public static final byte OP_LT = 11;
    public static final byte OP_GTE = 12;
    public static final byte OP_LTE = 13;
    public static final byte OP_BOOL = 14;
    public static final byte OP_NBOOL = 15;
    public static final byte OP_ADDNUM = 16;
    public static final byte OP_SUBNUM = 17;
    public static final byte OP_MULNUM = 18;
    public static final byte OP_DIVNUM = 19;
    public static final byte OP_MODNUM = 20;
    public static final byte OP_NEGNUM = 21;
    public static final byte OP_BOOLNUM = 22;
    public static final byte OP_NBOOLNUM = 23;
    public static final byte OP_NUMCONV = 24;
    public static final byte OP_STR = 25; // $str function
    public static final byte OP_ITER = 26;
    public static final byte OP_CALL = 27; // <num_args> stack > (top) arg1 > arg2 > ... > function
    public static final byte OP_LIST = 28; // <num_args>
    public static final byte OP_DICT = 29; // <num_pairs> stack ordered > (top) key > value > key > value > ...
    public static final byte OP_VEC = 30; // <num_args>
    public static final byte OP_SET = 31;
    public static final byte OP_ASSIGN = 32; // stack: > (top) name > value
    public static final byte OP_SET_MEMBER = 33; // stack: > (top) assignee > member name > value
    public static final byte OP_GET_MEMBER = 34; // stack: > (top) mem > expr
    public static final byte OP_GET_INDEX = 35;
    public static final byte OP_LAST = 36; // $last function
    public static final byte OP_FIRST = 37; // $first function
    public static final byte OP_LEN = 38; // $len function
    public static final byte OP_NATIVE_CALL = 39; // <num_args>
    public static final byte OP_JMP = 40; // <index index index>
    public static final byte OP_JIF = 41; // <index index index>
    public static final byte OP_POP = 42;
    public static final byte OP_DUP = 43;
    public static final byte OP_SWAP = 44;
    public static final byte OP_PUSH_TRY_ADDRESS = 45; // push next catch expression address stack
    public static final byte OP_POP_TRY_ADDRESS = 46;
    public static final byte OP_LADD = 47; // add to list: iterators, loops
    public static final byte OP_EMPTY_LIST = 48;
    public static final byte OP_EMPTY_DICT = 49;
    public static final byte OP_EMPTY_SET = 50;
    public static final byte OP_PUSH0 = 51;
    public static final byte OP_PUSH1 = 52;
    public static final byte OP_TRUE = 53;
    public static final byte OP_FALSE = 54;
    public static final byte OP_NULL = 55;
    public static final byte OP_JIT = 56; // <index index index>
    public static final byte OP_INIT_CLASS = 57; // <num_fields> stack: > (top) super (or null) > chunk > chunk > ...
    public static final byte OP_GET_THIS = 58; // function calls auto bind "this"
    public static final byte OP_RANGE = 59;
    public static final byte OP_GET_VARIABLE = 60;
    public static final byte OP_ITER_HASNEXT = 61;
    public static final byte OP_ITER_NEXT = 62;
    public static final byte OP_SAVE_STACK_SIZE = 63; // return, catch
    public static final byte OP_RESTORE_STACK_SIZE = 64;
    public static final byte OP_ASSIGN_LOCAL = 65; // scope.assignLocal used in eg iterators
    public static final byte OP_DEEPEN_SCOPE = 66; // scope = new Scope(scope)
    public static final byte OP_DECREASE_SCOPE = 67; // scope = scope.parent
    public static final byte OP_REPLACE = 68; // pop(); const<index>
    public static final byte OP_REPLACE_LONG = 69; // pop(); const<index index index>
    public static final byte OP_CLOCK = 70; // $clock function
    public static final byte OP_BIGNUM = 71; // $bignum function
    public static final byte OP_INPUT = 72; // $input function
    public static final byte OP_PRINT = 73; // $print function with 1 arg
    public static final byte OP_PRINTLN = 74; // $println function with 1 arg
    public static final byte OP_MERGE = 75;
    public static final byte OP_NEQ = 76;
    public static final byte OP_IN = 77;
    public static final byte OP_SET_INDEX = 78; // stack: > (top) assignee > index > value
}

/*
return <expr>:
restore stack size
[expr]
jmp block
 */