package main;

import java.util.ArrayList;

import static main.TokenType.*;

class ScannerException extends RuntimeException {
    public ScannerException(String s) {
        super(s);
    }
}

public class Scanner {
    private String text;
    private int pointer, start, line = 1;
    private ArrayList<Token> tokens;
    public Scanner(String s) {
        this.text = s;
        this.pointer = 0;
        this.start = 0;
        this.tokens = new ArrayList<>();
    }
    public ArrayList<Token> scan() {
        while (!isAtEnd()) {
            Token t = scanOnce();
            if (t == null)
                continue;
            tokens.add(t);
        }
        return tokens;
    }

    private Token scanOnce() {
        skipWhiteSpace();
        if (isAtEnd())
            return makeToken(EOF);
        start = pointer;
        char c = advance();
        switch (c) {
            case '+': return makeToken(match('=') ? PLUS_EQ : PLUS);
            case '-': return makeToken(match('=') ? MINUS_EQ : match('>') ? ARROW : MINUS);
            case '*': return makeToken(match('*') ? match('=') ? EXP_EQ : EXP : match('=') ? STAR_EQ : STAR);
            case '/': {
                if (match('/')) {
                    while (!match('\n') && !isAtEnd())
                        advance();
                    line++;
                    return null;
                } else if (match('*')) {
                    while (!match('*') || !match('/')) {
                        if (match('\n'))
                            line++;
                        advance();
                    }
                    return null;
                } else return makeToken(match('=') ? SLASH_EQ : SLASH);
            }
            case '%': return makeToken(match('=') ? MOD_EQ : MOD);
            case '=': return makeToken(match('=') ? EQEQ : EQ);
            case '<': return makeToken(match('=') ? LTEQ : LT);
            case '>': return makeToken(match('=') ? GTEQ : GT);
            case ':': return makeToken(match(':') ? DOUBLE_COLON : COLON);
            case '?': return makeToken(QUESTION);
            case '!': return makeToken(match('=') ? BANG_EQ : BANG);
            case '#': return makeToken(HASH);
            case '$': return makeToken(DOLLAR);
            case '(': return makeToken(LPAREN);
            case ')': return makeToken(RPAREN);
            case '{': return makeToken(LBRACE);
            case '}': return makeToken(RBRACE);
            case '[': return makeToken(LBRACKET);
            case ']': return makeToken(RBRACKET);
            case '.': if (isNumber(peek())) return number('.');
            else return makeToken(DOT);
            case ',': return makeToken(COMMA);
            case ';': return makeToken(SEMICOLON);
            case '`': return makeToken(BACKTICK);
            case '"', '\'': return string(c);
            default: {
                if (isNumber(c))
                    return number(c);
                else if (isAlpha(c))
                    return identifier();
                throw new ScannerException("Invalid character");
            }
        }
    }
    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }
    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isNumber(c);
    }
    private Token identifier() {
        while (isAlphaNumeric(peek())) {
            advance();
        }
        return makeToken(idenType());
    }
    private TokenType idenType() {
        return switch (text.substring(start, pointer)) {
            case "true" -> TRUE;
            case "false" -> FALSE;
            case "null" -> NULL;
            case "class" -> CLASS;
            case "return" -> RETURN;
            case "new" -> NEW;
            case "or" -> OR;
            case "and" -> AND;
            case "this" -> THIS;
            default -> IDENTIFIER;
        };
    }
    private Token string(char term) {
        while (!match(term)) {
            if (isAtEnd())
                throw new ScannerException("Unterminated string");
            char c = advance();
            if (c == '\n')
                line++;
        }
        return makeToken(STRING);
    }
    private boolean isNumber(char c) {
        return c >= '0' && c <= '9';
    }
    private Token number(char c) {
        boolean dot = false;
        if (c == '.')
            dot = true;
        while (isNumber(peek()) || peek() == '.') {
            if (advance() == '.') {
                if (dot)
                    throw new ScannerException("Invalid number");
                dot = true;
            }
        }
        return makeToken(NUMBER); // makeToken handles the rest
    }
    private boolean match(char c) {
        if (peek() == c) {
            advance();
            return true;
        }
        return false;
    }
    private Token makeToken(TokenType type) {
        return new Token(text.substring(start, pointer), type, line);
    }
    private boolean isAtEnd() {
        return pointer >= text.length();
    }
    private char advance() {
        return text.charAt(pointer++);
    }
    private char peek() {
        if (isAtEnd())
            return '\0';
        return text.charAt(pointer);
    }
    private void skipWhiteSpace() {
        while (true) {
            char c = peek();
            switch (c) {
                case ' ', '\t', '\0' -> {
                    advance();
                }
                case '\n' -> {
                    line++;
                    advance();
                }
                default -> {
                    return;
                }
            }
        }
    }
}
