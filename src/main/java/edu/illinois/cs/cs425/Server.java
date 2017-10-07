package edu.illinois.cs.cs425;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.concurrent.atomic.AtomicReference;
import java.net.*;

/*
 * This class is for listening to incoming connections from other nodes
 */

public class Server extends Thread {
    
    ServerSocket listener;
    boolean finished;
    private final static Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private final AtomicReference<Thread> currentThread = new AtomicReference<Thread>();

    public Server(int portNumber) {
        finished = false;
        try {
            listener = new ServerSocket(portNumber);
        } catch (Exception e) {
            logger.info("Server could not be started");
            logger.severe(e.toString());
            return;
        }
    }

    public void stopMe() {
        logger.info("Stopping");
        finished = true;
        currentThread.get().interrupt();
        try {
            listener.close();
        } catch (Exception e) {
            logger.severe(e.toString());
        }        
    }

    public void run() {
        try {
            if (listener == null) {
                logger.severe("Server is not listening - killing this thread");
                return;
            }

            logger.info("Server started listening on " + listener.getInetAddress() + ":" + listener.getLocalPort());
            
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Socket socket = listener.accept();
                    logger.info("Connected to " + socket.getRemoteSocketAddress());
                    
                    logger.info("Spawning thread to handle incoming request to Server");
                    ServerHandler handler = new ServerHandler(socket);
                    handler.start();
                } catch (Exception e) {
                    logger.info("Problem accepting incoming connection on Server");
                    logger.severe(e.toString());
                }
            }
        } catch (Exception e) {
            logger.info("Problem in Server thread - killing this thread");
            logger.severe(e.toString());
        }
    }
}
