package edu.illinois.cs.cs425;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.net.*;
import org.json.JSONObject;
import org.json.JSONArray;
import org.apache.commons.cli.GnuParser; 
import org.apache.commons.cli.CommandLine; 
import org.apache.commons.cli.CommandLineParser; 
import org.apache.commons.cli.HelpFormatter; 
import org.apache.commons.cli.Option; 
import org.apache.commons.cli.Options; 
import org.apache.commons.cli.ParseException; 


/* 
 * This class is responsible for taking user's input, sanitizing it and passes it to the daemon listening on the client node (same node)
 */

public class Qgrep {

	private final static Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    public Qgrep() {}

	public static String execute(String[] args) throws Exception {
        String response = "";
		try {
			//convet input string array to string
			StringBuffer sb = new StringBuffer();
			for (int i = 0;i < args.length; i++) {
				sb.append(args[i]);
				sb.append(" ");
			}
			
			String s = sb.toString();

            final Options options = new Options();
            final CommandLineParser parser = new GnuParser();

            Option p = new Option("p", "pattern", true, "Pattern you want to find in files");
            p.setArgs(Option.UNLIMITED_VALUES);
            options.addOption(p);

            Option h = new Option("h", "hosts", false, "Hosts in a hostfile or a list of hosts");
            h.setArgs(Option.UNLIMITED_VALUES);
            options.addOption(h);

            Option d = new Option("d", "directory", true, "Directory to execute grep in");
            options.addOption(d);
            
            final CommandLine commandLine = parser.parse(options, args);
            JSONObject jsonArguments = new JSONObject();

            if (commandLine.hasOption("p")) {
                logger.info("Option -p found");
                String[] values = commandLine.getOptionValues("p");
                String valuesStr = "";
                for (String value : values) {
                    logger.info(value);
                    valuesStr += value;
                    valuesStr += " ";
                }
                valuesStr = valuesStr.substring(0, valuesStr.length()-1);
                jsonArguments.put("p", valuesStr);
            }

            if (commandLine.hasOption("d")) {
                logger.info("Option -d found");
                String directory = commandLine.getOptionValue("d");
                logger.info(directory);
                jsonArguments.put("d", directory);
            }

            ArrayList<String> hosts = new ArrayList<String>();

            if (commandLine.hasOption("h")) {
                logger.info("Option -h found");
                String[] values = commandLine.getOptionValues("h");
                boolean usingFile = false;
                if (values.length == 1) {
                    if (values[0].contains(".txt")) {
                        usingFile = true;
                        String host_filepath = values[0];
                        FileInputStream host_fileInput = new FileInputStream(host_filepath);
                        BufferedReader host_fileInput_BR = new BufferedReader(new InputStreamReader(host_fileInput));
                        String host_tmp;
                        while((host_tmp = host_fileInput_BR.readLine()) != null) {
                            logger.info(host_tmp);
                            hosts.add(host_tmp);
                        }
                    }
                }
                if (!usingFile) {
                    for (String value : values) {
                        logger.info(value);
                        hosts.add(value);
                    }
                }
            } else {
                logger.info("No -h option found, adding default host");
                hosts.add("0.0.0.0:6666"); // If no host is specified then just run grep on localhost
            }

            JSONArray hArray = new JSONArray();
            for (String host : hosts) {
                hArray.put(host);
            }
            jsonArguments.put("h", hArray);

            try {
                Socket client = new Socket("0.0.0.0",6667);
                client.setSoTimeout(120000); // Timeout from userhandler (in milliseconds)
                logger.info("Connected to "+client.getRemoteSocketAddress());
                
                PrintWriter pw = new PrintWriter(client.getOutputStream(),true);
                pw.println(jsonArguments.toString());
                logger.info("Grep task begin");
                
                // Wait for UserHandler to complete
                BufferedReader input = new BufferedReader(new InputStreamReader(client.getInputStream()));
                response = input.readLine();
                logger.info(response);
		
		String filename =response +"/Host*.txt";
	//	logger.info(filename);
		Thread.sleep(20);
                //LineNumberReader reader = new LineNumberReader(new FileReader(filename));
          	//int count= 0;
		//String lineReader = "";
		//while((lineReader = reader.readLine())!=null){}
		//count = reader.getLineNumber();
		//logger.info("Total grep: "+Integer.toString(count)+" lines\n");
	        Runtime runtime = Runtime.getRuntime();
		String countCommand = "wc -l "+filename;
		String[] commands = {"/bin/sh", "-c",countCommand };
		Process process = runtime.exec(commands);
		BufferedReader processInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
		System.out.println("	****************");
		System.out.println("	*Lines count:");
		String count;
		while((count=processInput.readLine())!=null){
			
			System.out.println("	*"+count);
		}
		Runtime runtime2 = Runtime.getRuntime();
		filename = response +"/results.txt";
                countCommand = "wc -l "+filename;
		String[] commands2 = {"/bin/sh", "-c",countCommand };
                Process process2 = runtime2.exec(commands2);
                BufferedReader processInput2 = new BufferedReader(new InputStreamReader(process2.getInputStream()));
                while((count=processInput2.readLine())!=null){

                        System.out.println("	*  "+count);
                }

		System.out.println("	****************");
		client.close();
            } catch (Exception socket_error) {
                logger.severe(socket_error.toString());
            }
		} catch (Exception main_error) {
			System.out.println(main_error);
		} finally {
            return response;
        }
	}
}
