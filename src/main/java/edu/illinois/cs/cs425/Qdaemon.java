package edu.illinois.cs.cs425;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.net.*;

/* 
 * This class handles two modules; the User Handler (which takes inputs from the query input program and passes them to all known nodes) and the Server (which responds to queries received from any node)
 */

public class Qdaemon {

    private final static Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    public Server server;
    public UserHandler userHandler;

    public Qdaemon(String[] args) throws Exception {
        logger.setLevel(Level.INFO);

        if (args.length != 1) {
            System.out.println("You need to give an argument for the port number");
            System.out.println("Usage: java Daemon <port>");
            System.out.println("Example: java Daemon 6666");
            return;
        }  

        int portNumber = -1;

        try {
            portNumber = Integer.parseInt(args[0]); 
        } catch(Exception e) {
            logger.info("Invalid port number entered");
            logger.severe(e.toString());
            return;
        }

        try {
            server = new Server(portNumber);
            server.start();
        } catch (Exception e) {
            logger.info("Server could not be started");
            logger.severe(e.toString());
            return;
        }

        userHandler = new UserHandler(portNumber+1);
        userHandler.start();

        // joinMe();
    }

    public void joinMe() {
        try {
            server.join();
            userHandler.join();
        } catch (Exception e) {
            logger.info("Unable to join either Server thread or User Handler thread with Daemon's Main thread");
            logger.severe(e.toString());
        }
    }

    public void stopMe() {
        logger.info("Stopping");
        server.stopMe();
        try {
            server.join();
        } catch (Exception e) {
            logger.info("Unable to join Server thread with Daemon's Main thread");
            logger.severe(e.toString());
        }
        userHandler.stopMe();
        try {
            userHandler.join();
        } catch (Exception e) {
            logger.info("Unable to join User Handler thread with Daemon's Main thread");
            logger.severe(e.toString());
        }
    }
}
