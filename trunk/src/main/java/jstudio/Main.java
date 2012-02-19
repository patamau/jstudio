package jstudio;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import jstudio.util.FilteredStream;
import jstudio.util.Options;

public class Main {
	
	private static final Logger logger = Logger.getLogger(Main.class);
	
	private static final String 
		DEBUG_OPT = "debug",
		LOG4J_CONFIG_FILE = "log4j.cfg",
		FILTERED_FILE = "jstudio.err";
		
	private static void printSystemProperties(){
		printSystemProperty("os.name");
		printSystemProperty("os.version");
		printSystemProperty("os.arch");
		printSystemProperty("java.version");
		printSystemProperty("java.vendor");
		printSystemProperty("java.class.path");
		printSystemProperty("java.library.path");
	}
		
	private static void printSystemProperty(String key){
		logger.debug(key+" "+System.getProperty(key));
	}

	public static void main(String args[]){
		//configure log4j
		PropertyConfigurator.configure(LOG4J_CONFIG_FILE);

		Options opts = new Options(args);
		boolean console = false;
		if(opts.isSet(DEBUG_OPT)){
			//force debug and enable console
			Logger.getRootLogger().setLevel(Level.DEBUG);
			Logger.getLogger("org.hibernate").setLevel(Level.INFO);
			console = true;
		}
		
		//redirect all standard output and error to file 
		//so that console can be closed
		FilteredStream stream = new FilteredStream(
				new ByteArrayOutputStream(),
				FILTERED_FILE,
				console);
		PrintStream filteredPrintStream  =  new PrintStream(stream);
		System.setErr(filteredPrintStream);
		System.setOut(filteredPrintStream);
		if(logger.isDebugEnabled()){
			printSystemProperties();
		}
		
		final JStudio jstudio = new JStudio();
		//redirect exceptions to jstudio
		/*
		Thread.setDefaultUncaughtExceptionHandler(jstudio);
		Runtime.getRuntime().addShutdownHook(new Thread(){
			public void run(){
				//jstudio.finalize();
			}
		});
		*/
		
		//start up
		try{
			jstudio.initialize();
		}catch(Exception e){
			e.printStackTrace();
			System.exit(1);
		}
		//waiting for threads to finish
		//finalization is called by the shutdown hook
	}
}
