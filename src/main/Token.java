package main;

public record Token(String text, TokenType type, int line) {
    @Override
    public String toString() {
        return "{"+type+", "+text+", "+line+"}";
    }
}
