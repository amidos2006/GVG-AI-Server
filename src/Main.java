import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import servers.*;
import tools.Utils;

public class Main {
    public static final float CHECK_RATE_SEC = 5;
    public static final String OUTPUT_PATH="outputs";
    public static final String INPUT_PATH="inputs";
    public static final String LOGS_PATH="logs";
    public static final String GVGAI_PATH="GVG-AI_Competition_Runner";
    public static final String SOURCE_PATH="GVG-AI_Competition";
    
    public static String getDateTime(){
	DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	Date date = new Date();
	return dateFormat.format(date);
    }
    
    public static void logEvent(String logFile, String message) throws Exception{
	logEvent(logFile, message, false);
    }
    
    public static void logEvent(String logFile, String message, boolean includeTime) throws Exception{
	BufferedWriter bw = new BufferedWriter(new FileWriter(logFile, true));
	if(includeTime){
	    bw.write("[" + getDateTime()  + "] " + message + "\n");
	}
	else{
	    bw.write(message + "\n");
	}
	bw.close();
    }
    
    public static void logError(String logFile, Exception e) throws Exception{
	logError(logFile, e, true);
    }
    
    public static void logError(String logFile, Exception e, boolean includeTime) throws Exception{
	logEvent(logFile, "Error: " + e.toString() + " - Message: " + e.getMessage() + 
		" - StackTrace: " + e.getStackTrace() + "\n", includeTime);
    }
    
    public static void main(String[] args) throws Exception{
	int logNumber = (new File(LOGS_PATH)).listFiles().length;
	logEvent(LOGS_PATH + "/log_" + logNumber + ".txt", "Starting server.");
	Server server = null;
	if(args.length == 0){
	    logEvent(LOGS_PATH + "/log_" + logNumber + ".txt", "Error: Invalid server type.");
	    logEvent(LOGS_PATH + "/log_" + logNumber + ".txt", "Server terminated.");
	    return;
	}
	switch(args[0].toLowerCase()){
	case "rulegeneration":
	    logEvent(LOGS_PATH + "/log_" + logNumber + ".txt", "Creating rule generation server.");
	    server = new RuleGenerationServer(GVGAI_PATH, INPUT_PATH, OUTPUT_PATH);
	    break;
	case "levelgeneration":
	    logEvent(LOGS_PATH + "/log_" + logNumber + ".txt", "Creating level generation server.");
	    server = new LevelGenerationServer(GVGAI_PATH, INPUT_PATH, OUTPUT_PATH);
	    break;
	default:
	    logEvent(LOGS_PATH + "/log_" + logNumber + ".txt", "Error: Invalid server type.");
	    logEvent(LOGS_PATH + "/log_" + logNumber + ".txt", "Server terminated.");
	    return;
	}
	try{
    	    while(true){
    		//logEvent(LOGS_PATH + "/log_" + logNumber + ".txt", "Checking for new files.");
    		File[] files = Utils.getNewFiles(INPUT_PATH, OUTPUT_PATH);
    		if(files.length > 0){
    		    logEvent(LOGS_PATH + "/log_" + logNumber + ".txt", files.length + " files were found.");
    		}
    		for(int i=0; i<files.length; i++){
    		    (new File(OUTPUT_PATH + "/" + files[i].getName())).mkdir();
    		    logEvent(LOGS_PATH + "/log_" + logNumber + ".txt", "Making a fresh copy of GVG-AI.");
    		    try{
    			Utils.newCopy(SOURCE_PATH, GVGAI_PATH);
    		    }
    		    catch(Exception e){
    			logError(LOGS_PATH + "/log_" + logNumber + ".txt", e);
    			logEvent(OUTPUT_PATH + "/" + files[i].getName() + "/log.txt", 
    				"Failed - During Extraction of the file");
    			logError(OUTPUT_PATH + "/" + files[i].getName() + "/log.txt", e);
    			continue;
    		    }
    		    
    		    logEvent(LOGS_PATH + "/log_" + logNumber + ".txt", "Extracting \"" + files[i].getName() + "\"");
    		    String packageName = "";
    		    try{
    		        packageName = server.extract(files[i].getName());
    		    }
    		    catch(Exception e){
    			logError(LOGS_PATH + "/log_" + logNumber + ".txt", e);
    			logEvent(OUTPUT_PATH + "/" + files[i].getName() + "/log.txt", 
    				"Failed - During extracting the files.");
    			logError(OUTPUT_PATH + "/" + files[i].getName() + "/log.txt", e);
    			continue;
    		    }
    		    
    		    logEvent(LOGS_PATH + "/log_" + logNumber + ".txt", "Compiling \"" + files[i].getName() + "\"");
    		    try{
    			server.compile(files[i].getName());
    		    }
    		    catch(Exception e){
			logError(LOGS_PATH + "/log_" + logNumber + ".txt", e);
    			logEvent(OUTPUT_PATH + "/" + files[i].getName() + "/log.txt", 
    				"Failed, During compiling the code.", false);
    			logError(OUTPUT_PATH + "/" + files[i].getName() + "/log.txt", e, false);
    			String buildText = "";
    			BufferedReader r = new BufferedReader(new FileReader(OUTPUT_PATH + "/" + files[i].getName() + "/build.txt"));
    			String line = "";
    			while ((line = r.readLine()) != null) {
    			    buildText += line + "\n";
    			}
    			r.close();
    			logEvent(OUTPUT_PATH + "/" + files[i].getName() + "/log.txt", "Ant output error:\n" + buildText, false);
			continue;
		    }
    		    
    		    logEvent(LOGS_PATH + "/log_" + logNumber + ".txt", "Running \"" + files[i].getName() + "\"");
    		    try{
    			Utils.copySpritesExamples(SOURCE_PATH, GVGAI_PATH);
    			server.run(files[i].getName(), packageName);
    		    }
    		    catch(Exception e){
			logError(LOGS_PATH + "/log_" + logNumber + ".txt", e);
    			logEvent(OUTPUT_PATH + "/" + files[i].getName() + "/log.txt", 
    				"Failed - During running the code.");
    			logError(OUTPUT_PATH + "/" + files[i].getName() + "/log.txt", e);
			continue;
		    }
    		}
    		Thread.sleep((long)(CHECK_RATE_SEC * 1000));
    	    }
	}
	catch(Exception e){
	    logError(LOGS_PATH + "/log_" + logNumber + ".txt", e);
	}
	logEvent(LOGS_PATH + "/log_" + logNumber + ".txt", "Server terminated.");
    }
}
