package main;

import main.interpreter.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) throws IOException {
        if (args.length != 1)
            System.exit(-1);
        String s;
        try (FileInputStream fs = new FileInputStream(args[0])) {
            s = new String(fs.readAllBytes());
        }
        ArrayList<Token> tokens = new Scanner(s).scan();
        ArrayList<Statement> statements = new Optimizer(new Parser().parse(tokens)).optimize();
        new Interpreter(statements).interpret();
    }
}
