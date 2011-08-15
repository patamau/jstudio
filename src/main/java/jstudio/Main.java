package jstudio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jstudio.util.Options;

public class Main {
	
	private static final Logger logger = LoggerFactory.getLogger(Main.class);

	public static void main(String args[]){
		Options opts = new Options(args);
		//TODO: load options from command line
		JStudio jstudio = new JStudio();
		jstudio.initialize();
	}
}
