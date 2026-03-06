package main;

public enum TokenType {
    CLASS, // class declarations
    RETURN, // optional expression, otherwise NULL
    THIS,
    CATCH,
    ENUM,

    IDENTIFIER,

    STRING,
    NUMBER,
    NULL,
    TRUE,
    FALSE,

    LBRACE,
    RBRACE,
    LPAREN,
    RPAREN,
    LBRACKET,
    RBRACKET,

    COMMA, // function args, vectors, lists
    DOT, // class members
    SEMICOLON, // separating expressions

    PLUS,
    MINUS,
    STAR,
    SLASH,
    MOD,

    EQ, // assignment
    COLON, // ternaries
    DOUBLE_COLON, // loops
    QUESTION, // truth eval unary, ternaries

    BANG,
    BANG_EQ,
    EQEQ,
    GT,
    GTEQ,
    LT,
    LTEQ,

    DOLLAR, // native functions

    HASH, // length of vector/list, 1/0 for boolean

    PLUS_EQ,
    MINUS_EQ,
    STAR_EQ,
    SLASH_EQ,
    MOD_EQ,

    ARROW,

    OR,
    AND,

    PIPE,

    EOF
}
