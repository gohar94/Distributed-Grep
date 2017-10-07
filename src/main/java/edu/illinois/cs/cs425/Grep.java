package edu.illinois.cs.cs425;

/* 
 * This is the program that is executed by the user - it is the starting point of the entire system's frontend
 */

public class Grep {
    public static void main(String[] args) throws Exception {
        Qgrep qgrep = new Qgrep();
        qgrep.execute(args);
    }
}