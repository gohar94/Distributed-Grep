package edu.illinois.cs.cs425;

import java.io.*;
import java.util.*;
import java.net.*;
import org.json.JSONObject;
import org.json.JSONArray;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.concurrent.atomic.AtomicReference;
import java.text.SimpleDateFormat;

/* 
 * This thread takes messages from the query input program (equivalent of grep) and sends the query to all nodes in the system
 */

public class UserHandler extends Thread {
    
    ServerSocket listener;
    String pattern = null;    //send to each remote machine;
    String output_path = null;// send to own local server;
    String default_hostlist = "hostlist.txt";
    String directory = null;
    boolean finished;
    BufferedReader input = null;
    PrintWriter feedback = null;
    private final AtomicReference<Thread> currentThread = new AtomicReference<Thread>();
    private final static Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    public UserHandler(int portNumber) {
        finished = false;
        try {
            listener = new ServerSocket(portNumber);   
        } catch (Exception e) {
            logger.severe(e.toString());
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
            logger.info("User Handler started listening on " + listener.getInetAddress() + ":" + listener.getLocalPort());
            while (!Thread.currentThread().isInterrupted()) {
                Socket socket;
                socket = listener.accept();
                try {
                    
                    logger.info("Connected to " + socket.getRemoteSocketAddress());
                    // Wait for inputs and process them, no other connection can be made until then
                    // This means that only one query input program (called Qgrep) can be used on one node at a time
                    input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String message;

                    message = input.readLine();
                    JSONObject jsonArguments = new JSONObject(message);
                    logger.info(jsonArguments.toString());
                    ArrayList<String> hosts = new ArrayList<String>();

                    if (jsonArguments.has("p") && jsonArguments.has("h") && jsonArguments.has("d")) {
                        pattern = jsonArguments.getString("p");
                        output_path = "results.txt";

                        JSONArray hostsArray = jsonArguments.getJSONArray("h");
                        for (int i = 0; i < hostsArray.length(); i++) {
                            String temp = hostsArray.getString(i);
                            hosts.add(temp);
                        }

                        directory = jsonArguments.getString("d");
                    }

                    // Use CountDownLatch to wait all threads complete
                    logger.info("Command read completed");
                    logger.info("Number of hosts is " + hosts.size());
                    
                    CountDownLatch doneSignal = new CountDownLatch(hosts.size());
                    logger.info("Set CountDownLatch successfully");
                    
                    String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
                    timeStamp += ".out"; // To be used as folder name later

                    File file = new File(timeStamp);
                    if (!file.exists()) {
                        if (file.mkdir()) {
                            logger.info("Directory created - " + timeStamp);
                        }
                    }

                    for (int i = 0; i < hosts.size(); i++){
                        QueryHandler queryhandler = new QueryHandler(directory, timeStamp, hosts.get(i).split(":")[0],Integer.parseInt(hosts.get(i).split(":")[1]),pattern,doneSignal); 
                        queryhandler.start();
                    }

                    doneSignal.await(100, TimeUnit.SECONDS); // Wait for all threads to end
                    logger.info("Query collect from all remote host!");

                    // Merge all the results from each node
                    if (output_path != null) {
                        Runtime runtime = Runtime.getRuntime();
                        String catCommand = "cat " + timeStamp + "/Host*.txt > " + timeStamp + "/" + output_path;
                        String[] command = {"/bin/sh", "-c", catCommand};
                        logger.info(catCommand);
                        Process process = runtime.exec(command);
                        process.waitFor();
                    }
                    
                    // Send feedback to user program (Qgrep)
                    feedback = new PrintWriter(socket.getOutputStream(),true);
                    feedback.println(timeStamp);
                } catch (Exception e) {
                    logger.severe(e.toString());
                } finally {
                    try {
                        logger.info("Closing socket for " + socket.getRemoteSocketAddress());
                        socket.close();
                        feedback.close();
                        input.close();
                    } catch (Exception e) {
                        logger.severe(e.toString());
                    }
                }
            }
        } catch (Exception e) {
            logger.severe(e.toString());
        }
    }
}
