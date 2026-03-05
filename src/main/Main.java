package main;

import main.interpreter.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) throws IOException {
        if (args.length != 1)
            System.exit(-1);
        String s = new String(new FileInputStream(args[0]).readAllBytes());
        ArrayList<Token> tokens = new Scanner(s).scan();
        ArrayList<Statement> statements = new Parser().parse(tokens);
        new Interpreter(statements).interpret();
    }
}
