package main;

import main.expr.*;

import java.util.ArrayList;
import java.util.HashMap;

import static main.TokenType.*;

class ParserException extends RuntimeException {
    public ParserException(int line, String s) {
        super("Error at line "+line+": "+s);
    }
}

public class Parser {
    ArrayList<Token> tokens;
    int index;
    public ArrayList<Statement> parse(ArrayList<Token> tokens) {
        this.tokens = tokens;
        this.index = 0;
        ArrayList<Statement> stmts = new ArrayList<>();
        while (!isAtEnd()) {
            stmts.add(statement());
        }
        return stmts;
    }
    public Statement statement() {
        Expression expr = expression();
        consume(SEMICOLON, "Expected semicolon.");
        return new ExpressionStatement(expr);
    }
    boolean isAtEnd() {
        return index >= tokens.size();
    }
    Token advance() {
        return tokens.get(index++);
    }
    void consume(TokenType t, String error) {
        Token tt = advance();
        if (tt.type() == t)
            return;
        throw new ParserException(tt.line(), error);
    }
    //TODO
    Expression expression() {
        return assignment();
    }
    Expression assignment() {
        Expression expr = ternary();
        if (match(EQ)) {
            Token t = previous();
            if (!isAssignable(expr))
                throw new ParserException(peek().line(), "Invalid assignment target.");
            Expression right = assignment();
            return new AssignmentExpression(expr, right, t);
        }
        return expr;
    }
    boolean isAssignable(Expression expr) {
        return expr instanceof VariableLookupExpression || expr instanceof MemberExpression || expr instanceof IndexExpression;
    }
    boolean match(TokenType... types) {
        Token t = peek();
        for (TokenType type : types) {
            if (t.type() == type) {
                advance();
                return true;
            }
        }
        return false;
    }
    Token peek() {
        return tokens.get(index);
    }
    Expression ternary() {
        Expression expr = or();
        if (match(QUESTION)) {
            Expression trueExpr = expression();
            consume(COLON, "Expect ':' after '?'");
            Expression falseExpr = expression();
            return new TernaryExpression(expr, trueExpr, falseExpr);
        }
        return expr;
    }
    Expression or() {
        Expression expr = and();
        while (match(OR)) {
            expr = new BinaryExpression(expr, previous(), and());
        }
        return expr;
    }
    Expression and() {
        Expression expr = equality();
        while (match(AND)) {
            expr = new BinaryExpression(expr, previous(), equality());
        }
        return expr;
    }
    Expression equality() {
        Expression expr = comparison();
        if (match(EQEQ, BANG_EQ)) {
            return new BinaryExpression(expr, previous(), equality());
        }
        return expr;
    }
    Expression comparison() {
        Expression expr = term();
        if (match(LT, GT, LTEQ, GTEQ)) {
            return new BinaryExpression(expr, previous(), comparison());
        }
        return expr;
    }
    Expression term() {
        Expression expr = factor();
        while (match(PLUS, MINUS)) {
            expr = new BinaryExpression(expr, previous(), factor());
        }
        return expr;
    }
    Expression factor() {
        Expression expr = unary();
        while (match(STAR, SLASH, MOD)) {
            expr = new BinaryExpression(expr, previous(), unary());
        }
        return expr;
    }
    Expression unary() {
        if (match(BANG, QUESTION, HASH, MINUS)) {
            return new UnaryExpression(previous(), unary());
        }
        return call();
    }
    private Expression finishCall(Expression callee) {
        ArrayList<Expression> arguments = new ArrayList<>();
        if (!check(RPAREN)) {
            do {
                if (arguments.size() >= 255) {
                    throw new ParserException(peek().line(), "Can't have more than 255 arguments.");
                }
                arguments.add(expression());
            } while (match(COMMA));
        }
        consume(RPAREN, "Expect ')' after arguments.");

        return new FunctionCallExpression(callee, arguments);
    }
    Expression call() {
        Expression expr = primary();

        while (true) {
            if (match(LPAREN)) {
                expr = finishCall(expr);
            } else if (match(DOT)) {
                consume(IDENTIFIER,
                        "Expect property name after '.'.");
                Token name = previous();
                expr = new MemberExpression(expr, name.text());
            } else if (match(LBRACKET)) {
                Expression index = expression();
                consume(RBRACKET, "Expect ']' after index.");
                expr = new IndexExpression(expr, index);
            } else {
                break;
            }
        }
        return expr;
    }
    Expression primary() {
        Token t = advance();
        return switch (t.type()) {
            case THIS -> new ThisExpression();
            case TRUE -> new LiteralExpression(true);
            case FALSE -> new LiteralExpression(false);
            case NULL -> new LiteralExpression(null);
            case NUMBER -> new LiteralExpression(Double.parseDouble(t.text()));
            case STRING -> new LiteralExpression(t.text().substring(1, t.text().length() - 1));
            case IDENTIFIER -> new VariableLookupExpression(t.text());
            case LPAREN -> paren();
            case CLASS -> classDeclaration();
            case LBRACE -> block();
            case RETURN -> returnExpression();
            case LBRACKET -> list();
            case DOLLAR -> nativeCall();
            default -> throw new ParserException(t.line(), "Expect expression.");
        };
    }
    Token previous() {
        return tokens.get(index - 1);
    }
    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type() == type;
    }
    Expression paren() {
        TokenType t = peekAfterStack(LPAREN, RPAREN, 1).type();
        if (t == DOUBLE_COLON) {
            return loop();
        } else if (t == ARROW) {
            return functionDeclaration();
        } else {
            Expression expr = expression();
            if (check(COMMA)) {
                return vector(expr);
            }
            consume(RPAREN, "Expect ')' after expression.");
            return expr;
        }
    }
    Expression functionDeclaration() {
        ArrayList<Token> params = new ArrayList<>();
        if (!check(RPAREN)) {
            do {
                if (params.size() >= 255) {
                    throw new ParserException(peek().line(), "Can't have more than 255 arguments.");
                }
                params.add(advance());
            } while (match(COMMA));
        }
        consume(RPAREN, "Expect ')' after arguments.");
        consume(ARROW, "Expected '->'");
        Expression body = expression();
        return new FunctionDeclarationExpression(params, body);
    }
    Expression vector(Expression expr1) {
        ArrayList<Expression> exprs = new ArrayList<>();
        exprs.add(expr1);
        while (match(COMMA)) {
            exprs.add(expression());
        } consume(RPAREN, "Expect ')' after vector.");
        return new VectorExpression(exprs);
    }
    Token peekAfterStack(TokenType acc, TokenType dec, int start) {
        int i = index;
        while (true) {
            if (i >= tokens.size())
                throw new ParserException(previous().line(), "Unterminated expression.");
            Token t = tokens.get(i++);
            if (t.type() == acc) {
                start++;
            } else if (t.type() == dec) {
                start--;
                if (start == 0) {
                    if (i >= tokens.size())
                        throw new ParserException(previous().line(), "Unterminated expression.");
                    return tokens.get(i);
                }
            }
        }
    }
    Expression loop() {
        Expression condition = expression();
        consume(RPAREN, "Expect ')' after condition.");
        consume(DOUBLE_COLON, "Expect '::' in loops.");
        var body = expression();
        return new LoopExpression(condition, body);
    }
    Expression block() {
        ArrayList<Statement> stmts = new ArrayList<>();
        while (!check(RBRACE) && !isAtEnd()) {
            stmts.add(statement());
        }
        consume(RBRACE, "Expect '}' after block.");
        return new BlockExpression(stmts);
    }
    Expression classDeclaration() {
        consume(LPAREN, "Expect '(' after class name.");
        Expression superclass = null;
        if (!check(RPAREN))
            superclass = expression();
        consume(RPAREN, "Expect ')' after superclass name.");
        consume(LBRACE, "Expect '{' before class body.");
        HashMap<String, Expression> fields = new HashMap<>();
        if (!check(RBRACE)) {
            do {
                if (fields.size() >= 255) {
                    throw new ParserException(peek().line(), "Can't have more than 255 fields.");
                }
                consume(DOT, "Expect '.'");
                String f = advance().text();
                consume(EQ, "Expect '='");
                // allows methods, named fields, etc
                // initialized on construction
                fields.put(f, expression());
            } while (match(COMMA));
        }
        consume(RBRACE, "Expect '}' after class body.");
        return new ClassDeclarationExpression(fields, superclass);
    }
    // the only expression that does not return a value and instantly folds all expressions and exits the block or function
    // all functions return something
    Expression returnExpression() {
        Expression value = expression();
        return new ReturnExpression(value);
    }
    Expression nativeCall() {
        Token t = advance();
        String name = t.text();
        if (t.type() != IDENTIFIER)
            throw new ParserException(t.line(), "Expect native function name.");
        consume(LPAREN, "Expect '(' after function name.");
        ArrayList<Expression> arguments = new ArrayList<>();
        if (!check(RPAREN)) {
            do {
                if (arguments.size() >= 255) {
                    throw new ParserException(peek().line(), "Can't have more than 255 arguments.");
                }
                arguments.add(expression());
            } while (match(COMMA));
        }
        consume(RPAREN, "Expect ')' after arguments.");
        return new NativeFunctionCallExpression(name, arguments);
    }
    Expression list() {
        ArrayList<Expression> exprs = new ArrayList<>();
        if (!check(RBRACKET)) {
            do {
                exprs.add(expression());
            } while (match(COMMA));
        }
        consume(RBRACKET, "Expect ']' after list.");
        return new ListExpression(exprs);
    }
}
