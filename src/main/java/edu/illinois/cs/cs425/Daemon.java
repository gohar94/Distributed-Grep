package edu.illinois.cs.cs425;

/* 
 * This is the program that is executed by the user - it is the starting point of the entire system's backend
 */

public class Daemon {
    public static void main(String[] args) throws Exception {
        Qdaemon qdaemon = new Qdaemon(args);
    }
}