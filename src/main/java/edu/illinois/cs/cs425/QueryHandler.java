package edu.illinois.cs.cs425;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.concurrent.CountDownLatch;
import java.net.*;
import org.json.JSONObject;

/* 
 * This thread is for sending the query to one node and getting the response back from it
 */

public class QueryHandler extends Thread {

    String remoteIp;
    int remotePort;
    String query;
    CountDownLatch doneSignal;
    String filepath;
    String timeStamp;
    String directory;
    private final static Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    public QueryHandler(String _directory, String _timeStamp, String _remoteIp, int _remotePort, String _query, CountDownLatch _doneSignal) {
        // TODO ensure these are valid IP and port - maybe do it when reading from IP table and from argument - make standard sanitizing function
        remoteIp = _remoteIp;
        remotePort = _remotePort;
        query = _query;
        doneSignal = _doneSignal;
        timeStamp = _timeStamp;
        directory = _directory;
        // TODO what if query has special characters? Will file still be created?
        filepath = timeStamp+"/"+"Host-"+remoteIp+":"+remotePort+".txt";
    }

    public void run() {
        Socket socket = new Socket();

        try {
            logger.info("Spawned thread for fetching results from " + remoteIp + ":" + remotePort);
            socket = new Socket(remoteIp, remotePort);
            
            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
            
            JSONObject jsonQuery = new JSONObject();
            jsonQuery.put("query", query);
            jsonQuery.put("directory", directory);

            output.println(jsonQuery.toString());
            logger.info("Sent query " + query);
            
            int x;
            
            File outfile = new File(filepath);
            
            if (!outfile.exists()) {
                outfile.createNewFile();    
            }
            
            PrintWriter bf = new PrintWriter(outfile);
            String result = "";
            
            logger.info("Receiving response for query " + query);
            
            while ((x = input.read()) != -1) {
                // logger.info(message); // This can take too long to print!
                bf.print((char)x);
            }
            
            logger.info("Finished receiving response for query " + query);
            
            // TODO can we do this in finally block? And close all other printwriter and streams in all the code!
            bf.close();
        } catch (Exception e) {
            logger.info("Something went wrong in Query Handler thread");
            logger.severe(e.toString());
        } finally {
            try {
                logger.info("Closing socket for " + socket.getRemoteSocketAddress());
                socket.close();
                logger.info("Closed socket for " + socket.getRemoteSocketAddress());
                doneSignal.countDown();
            } catch (Exception e) {
                logger.info("Could not close socket for " + socket.getRemoteSocketAddress());
                logger.severe(e.toString());
            }
        }
    }
}
