package edu.illinois.cs.cs425;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.net.*;
import org.json.JSONObject;

/* 
 * This thread is for receiving/responding to messages (like queries) from the node that just connected to the daemon
 */ 

public class ServerHandler extends Thread {

    Socket socket;
    BufferedReader input;
    PrintWriter output;
    BufferedReader processInput;
    private final static Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    
    public ServerHandler(Socket _socket) {
        socket = _socket;
        input = null;
        output = null;
        processInput = null;
    }

    public void run() {
        try {
            logger.info("Spawned thread for " + socket.getRemoteSocketAddress());

            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new PrintWriter(socket.getOutputStream(), true);
            String message;

            message = input.readLine();
            logger.info("Incoming message is " + message);

            // Find out which operation is being requested by the other node
            //String opCode = message.split("/")[0];
            //logger.info("OpCode is " + opCode);
            JSONObject jsonMessage = new JSONObject(message);

            if (jsonMessage.has("query") && jsonMessage.has("directory")) {
                String query = jsonMessage.getString("query");
                String directory = jsonMessage.getString("directory");
                logger.info("Querying for " + query);
                
                // TODO use the query string as input for grep
                Runtime runtime = Runtime.getRuntime();
                // TODO need to escape special chars here
                String grepCommand = "grep " + query + " " + directory + "/*.*"; // /inputs/*.*
                String[] commands = {"/bin/sh", "-c", grepCommand};
                logger.info("Executing grep command on this node - " + grepCommand);

                try {
                    Process process = runtime.exec(commands);
                    logger.info("Grep executed");
                    
                    // Printing the output of grep
                    processInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    JSONObject jsonResponse = new JSONObject();
                    
                    String result = "";
                    String temp = "";
                    int x;
                    
                    logger.info("Result of grep ");
                    
                    int lineCount = 0;
                    while ((x = processInput.read()) != -1) {
                        if ((char)x == '\n') {
                            lineCount++;
                        }
                        output.print((char)x);
                    }

                    output.flush();
                    // output.println("Lines of output = " + lineCount);

                    logger.info("Finished result of grep");
                    
                    // Printing the error messages of grep
                    processInput = new BufferedReader(new InputStreamReader(process.getErrorStream()));

                    logger.info("Error result of grep");
                    
                    String error = "";

                    while ((temp = processInput.readLine()) != null) {
                        output.println(temp);
                    }

                    logger.info("Finished error result of grep");

                    int exitCode = process.waitFor();
                    logger.info("Grep finished with exit code " + exitCode);
                } catch (Exception e) {
                    logger.info("Problem executing grep on this node");
                    logger.severe(e.toString());
                } 
            }

            logger.warning("Connection dropped from " + socket.getRemoteSocketAddress());
        } catch (Exception e) {
            logger.info("Something went wrong in Server Handler thread");
            logger.severe(e.toString());
        } finally {
            try {
                logger.info("Closing socket for " + socket.getRemoteSocketAddress());
                socket.close();
                processInput.close();
                input.close();
                output.close();
                logger.info("Closed socket for " + socket.getRemoteSocketAddress());
            } catch (Exception e) {
                logger.info("Could not close socket for " + socket.getRemoteSocketAddress());
                logger.severe(e.toString());
            }
        }
    }

}
