# test language

I tried to test: is it possible to create a language with only expression statements? That is, a language where every single statement consists purely of an expression, and where everything can return a value and be chained infinitely.

It is possible, but only if you cheat.

## types of expressions
Obviously, all expressions present in normal languages exist, such as function calls, operators, and array indexing.
Some other expressions were able to be made pretty easily:
* Function definitions were replaced by anonymous lambdas. For example, what would be `function f(a) {return a;}` in JS would be `f = (a) -> a;` in this language.
* I could do the same with class definitions: `BinaryExpression = class(Expression) {.eval = () -> {}};` would create `BinaryExpression` extending `Expression` with an empty function .eval inside. More on classes later.
* If/else statements can be replaced by the ternary operator `a ? b : c`.
* While loops were trickier, but I decided to use lists. A while loop would run its body (an expression) while the condition remains true, and then add the result to a list. Then it would return the list. Syntax: `(condition)::body`. You can make it not waste memory by using the builting $ignoreWhileResult() function.
* Finally, I added block expressions. These would run a list of statements. To get a result from a block, use a `return` expression.
* Return expressions still feel like cheating. Instead of it returning a value, it'd exit all expressions until it finds a block.
## cool features I thought were neat
### Explicit native functions
Imagine wanting to make your own input() function in python, just to find out it clashes with the standard library one.
In this test language, $ is not a valid identifier symbol and instead denotes native functions:
```
$print(0);
```
### You can add functions together
```
f = (x) -> x;
g = (x) -> 2*x;
h = f + g;
$ print(h(2)); // prints 6
```
You can also do it with multiplication, subtraction, or other numeric operators.

### Classes aren't classes
Classes only serve as blueprints for objects. Every object has its own set of methods, and classes just serve as a blueprint for object creation.
Consider the following:
```
Object = class() {};
o = Object();
o.y = 1;
o._add = (x) -> x+this.y;
```
No trace of Object is present in o. It is its own class with its own methods.
### Vectors
A mathematical vector type!
```
a = (1, 2, 3);
b = (1, 2, 3);
$ print (a+b) // (2, 4, 6)
a = (1, 2);
b = (1, 2, 3);
$ print (a+b) // error
a = (1, 2);
b = (1, 2, 3);
$ print (a.expand(3)+b) // (2, 4, 3)
```
### New operators
* `?` -> converts a value to a boolean. For example, null, "", or 0 would be false, and "abc", 12, and [1, 2] would be true.
* `#` -> converts a value to an integer. For example, null or "0" would be 0, while "12" would be 12 and true would be 1. Lists and vectors give their length.
* `|` -> merges two functions or lists. For functions, for 2 args f and g return function h where h(args) = f(g(args)). For lists: for 2 args l and m return new list containing all elements of l and all of m.
### Catch
Use the catch keyword to catch errors:
```
obj = () -> null-1; // errors
o = obj() catch 1;
$print(o); // prints 1
```
### Operator overloading
You can use functions to overload operators in objects.
List of function names:
* _add = addition
* _sub = subtraction
* _mul = multiplication
* _div = division
* _mod = modulo
* _index = array indexing (can also be by strings or other non-numeric values)
* _call = function call (with arguments)
* _eq = equality
* _neq = inequality
* _gt, _lt, _gte, _lte = comparison
* _neg = unary negation
* _inv = boolean inversion(! operator)
* _bool = boolean conversion(? operator)
* _num = numeric conversion (# operator)
* _str = string conversion (for printing)
* _merge = function merging
* _contains = a in b: b._contains(a)
* _iter = iterator
* _iter_next = custom iterator: advance and give previous value
* _iter_hasnext = custom iterator: is it finished

### Enums

You can create an enum using the enum keyword.
```
ErrorType = enum {
    NO_ERROR,
    FILE_NOT_FOUND,
    READ_FAILED,
    UNKNOWN
};
err = ErrorType.NO_ERROR;
(err == ErrorType.FILE_NOT_FOUND) ? $ print("file not found") : (
    (err == ErrorType.READ_FAILED) ? $ print("read failed") : (
         (err == ErrorType.NO_ERROR) ? $ print("no error") : (
            $ print("unknown error")
         )
    )
); // prints "no error"
```

If you have 2 enums with the same branch (eg. a ReadError.FILE_NOT_FOUND and WriteError.FILE_NOT_FOUND), these branches are not equal.

### Match

A match expression will compare a certain expression to other expressions. If these are equal, it exits the expression and returns the associated value.

You can also produce a fallback using the '_' token.
```
e = 1;
b = match (e) {
    0 => "zero",
    1 => "one",
    (a = 2) => "side effect in condition, two",
    _ => "a lot"
};
$ print(b); // "one"
```

This does not support advanced pattern matching, only simple equality.

### Ranges

Ranges can be created using the .. operator:
```
r = 0..10;
$ print(1 in r);
```

They can be both in ascending order and descending order, although this currently has no effec on the range.
Ranges can currently only be used for number bounds checking using the `in` operator.

### Dictionaries

Dictionaries are is a map of values to values, where a value can correspond to another value. Create a dictionary using the % and { tokens.
```
map = % {1: 2, "e": 'E'};
map["h"] = true;
$ println(map[1]); // 2
$ println(map["e"]); // E
$ println(map["h"]); // true
```

### Sets

Sets can not contain duplicates and are not ordered. Create a set using % and [.
```
set = %[1, 2, 3];
$ println(1 in set); // true
$ println(set); // %[1, 2, 3]
set.add(4);
$ println(#set) // 4
set.add(1);
$ println(#set) // still 4
```

### Iterators

You can iterate over collections using a for loop.

```
list = [1, 2, 3, 4, 5, 6];
square = (l) -> x * x for x in l;
$ print(square(list));
```

You can also iterate over ranges by using the $step() function, which produces an iterable object.
```
range = 1..10;
iterable = $step(range, 1);
$ print(x for x in iterable);
```

You can also define custom iterators and iterables in objects using operator overloading.

## Example program
```
$ print(((tmp1, tmp2, tmp3, c) -> ((until) -> (c <= until)::tmp3 + 0*(c += 1) + (0*(tmp1 = tmp2) + 0*(tmp2 = tmp3) + 0*(tmp3 = tmp1+tmp2))))(0, 0, 1, 1)(200));
```

This prints the first 200 numbers in the fibonacci sequence.