package jstudio;

import org.apache.log4j.BasicConfigurator;

import jstudio.util.Options;

public class Main {

	public static void main(String args[]){
		BasicConfigurator.configure();
		Options opts = new Options(args);
		//TODO: load options from command line
		JStudio jstudio = new JStudio();
		jstudio.initialize();
	}
}
