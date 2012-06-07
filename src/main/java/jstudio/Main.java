package jstudio;

import java.io.IOException;

import javax.swing.JOptionPane;

import jstudio.util.Options;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

public class Main {
	
	private static Logger logger = Logger.getLogger(Main.class);
	
	private static final String 
		DEBUG_OPT = "debug",
		LOG_FILE = "jstudio.log",
		LOG_PATTERN = "%-4r [%t] %-5p %c %x - %m%n";
		
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
		//parse arguments
		final Options opts = new Options(args);
		//configure logging
		Logger root = Logger.getRootLogger();
		if(opts.isSet(DEBUG_OPT)){
			//force debug and enable console
			root.addAppender(new ConsoleAppender(new PatternLayout(LOG_PATTERN)));
			root.setLevel(Level.DEBUG);
			Logger.getLogger("org.hibernate").setLevel(Level.INFO);
			//this will help find errors
			printSystemProperties();
		}else{
			try {
				FileAppender fa;
				fa = new FileAppender(new PatternLayout(LOG_PATTERN), LOG_FILE, false);
				root.addAppender(fa);
			} catch (IOException e) {
				JOptionPane.showMessageDialog(null, "File log error "+e.getMessage()+". Using console...", "Log Error", JOptionPane.ERROR_MESSAGE);
				root.addAppender(new ConsoleAppender(new PatternLayout(LOG_PATTERN)));
			}
			root.setLevel(Level.WARN);
			logger = Logger.getLogger(Main.class);
		}
		
		logger.debug("Creating JStudio instance");
		//create jstudio resources
		final JStudio jstudio = new JStudio();
		
		//redirect exceptions to jstudio
		Thread.setDefaultUncaughtExceptionHandler(jstudio);
		Runtime.getRuntime().addShutdownHook(new Thread(){
			public void run(){
				logger.debug("Shutdown hook called...");
				jstudio.finalize();
			}
		});
		
		//start up
		try{
			jstudio.initialize();
		}catch(Exception e){
			logger.fatal("Initialization error",e);
			System.exit(1);
		}
		//waiting for threads to finish
		//finalization is called automatically by the shutdown hook
	}
}
