package jstudio;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import javax.swing.JOptionPane;
import javax.swing.JWindow;

import jstudio.control.Agenda;
import jstudio.control.Accounting;
import jstudio.control.AddressBook;
import jstudio.control.Comuni;
import jstudio.db.DatabaseInterface;
import jstudio.db.HibernateDB;
import jstudio.gui.AccountingPanel;
import jstudio.gui.AddressBookPanel;
import jstudio.gui.AgendaPanel;
import jstudio.gui.JStudioGUI;
import jstudio.util.Configuration;
import jstudio.util.FilteredStream;
import jstudio.util.IconPanel;
import jstudio.util.Language;
import jstudio.util.Resources;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the main class for the control flow of the JStudio application
 * It controls DB structures and the GUI front-end
 * @author Matteo
 *
 */
public class JStudio implements Thread.UncaughtExceptionHandler{

	public static final String 
		VERSION = "0.1a",
		BUILD = JStudio.class.getPackage().getImplementationVersion(),
		SPLASH_SCREEN = "splash.png",
		DB_HOST = "localhost",
		DB_NAME = "jstudio",
		DB_USER = "jstudio",
		DB_PASS = "jstudio137";
	
	public static final long SHOW_TIMEOUT = 500;
	
	private static final Logger logger = LoggerFactory.getLogger(JStudio.class);
	
	//dedicated graphical user interface
	private JStudioGUI gui;
	//dedicated configuration (avoid using the global one
	private Configuration configuration; 
	
	private DatabaseInterface database;
	private Agenda agenda;
	private AddressBook addressBook;
	private Accounting accounting;
	private Comuni comuni;
	
	public JStudio(){
	}
	
	private void initializeGUI(){
		//initialize GUI
		gui = new JStudioGUI(this.getClass().getSimpleName()+" "+VERSION, this);
		gui.setIconImage(Resources.getImage("appicon.png").getImage());
		gui.createGUI();
		gui.addPanel(new AddressBookPanel(addressBook));
		gui.addPanel(new AgendaPanel(agenda));
		gui.addPanel(new AccountingPanel(accounting));
	}
	
	private void initializeData(){
		//initialize database manager
		String protocol = Configuration.getGlobal(DatabaseInterface.KEY_PROTOCOL,DatabaseInterface.DEF_PROTOCOL);
		String driver = Configuration.getGlobal(DatabaseInterface.KEY_DRIVER,DatabaseInterface.DEF_DRIVER);
		database = new HibernateDB(protocol, driver);
		String hostname = Configuration.getGlobal(DatabaseInterface.KEY_HOST, DatabaseInterface.DEF_HOST);
		String dbname = Configuration.getGlobal(DatabaseInterface.KEY_NAME, DatabaseInterface.DEF_NAME);
		String user = Configuration.getGlobal(DatabaseInterface.KEY_USER, DatabaseInterface.DEF_USER);
		String password = Configuration.getGlobal(DatabaseInterface.KEY_PASS, DatabaseInterface.DEF_PASS);
		try {
			database.connect(hostname, dbname, user, password);
		} catch (Throwable e) {
			logger.error("Database connection error",e);
			JOptionPane.showMessageDialog(null, Language.string("Database connection error")+": "+e.getLocalizedMessage(), Language.string("Data initialization"), JOptionPane.ERROR_MESSAGE);
		}
		//initialize data handlers
		agenda = new Agenda(this);
		addressBook = new AddressBook(this);
		accounting = new Accounting(this);
		comuni = new Comuni(this);
		
		//FIXME: this is just for quick internal testing
//		if(database.isConnected()){
//			//contacts.addPerson(new Person("Matteo","Pedrotti",new Date(),"Via bomport, 20", "12312424"));
//			for(Invoice i: accounting.getAll()){
//				logger.info(i.toString());
//			}
//			String code = comuni.getCode("TN", "Trento");
//			logger.info("CODE: "+code);
//		}
	}
	
	private void initializeConfiguration(){
		//initialize configuration loading the configuration file
		configuration = new Configuration();
		configuration.load(this.getClass().getSimpleName()+Configuration.FILE_SUFFIX);
		Language.setCurrentLanguage(
				configuration.getProperty(
						"language",
						Language.getCurrentLanguage().getCode()
					)
				);
		//configuration is set globally
		Configuration.setGlobalConfiguration(configuration);
	}
	
	private void initializeStreams(){
		//redirect exceptions to this class
		Thread.setDefaultUncaughtExceptionHandler(this);
		//TODO: configure log4j to output of log file
		//redirect all output to log file
		FilteredStream stream = new FilteredStream(
				new ByteArrayOutputStream(),
				this.getClass().getSimpleName()+".log",
				logger.isDebugEnabled());
		PrintStream filteredPrintStream  =  new PrintStream(stream);
		System.setErr(filteredPrintStream);
		System.setOut(filteredPrintStream);
		if(logger.isDebugEnabled()){
			printSystemProperties();
		}
	}
	
	private void printSystemProperty(String key){
		logger.debug(key+" "+System.getProperty(key));
	}
	
	private void printSystemProperties(){
		printSystemProperty("os.name");
		printSystemProperty("os.version");
		printSystemProperty("os.arch");
		printSystemProperty("java.version");
		printSystemProperty("java.vendor");
		printSystemProperty("java.class.path");
		printSystemProperty("java.library.path");
	}
	
	public void initialize(){
		JWindow splash = new JWindow();
		IconPanel panel = new IconPanel(SPLASH_SCREEN);
		splash.getContentPane().add(panel);
		panel.setHead("JStudio "+VERSION);
		panel.setText(Language.string("Starting up..."));
		panel.setFoot("build "+BUILD);
		splash.pack();
		splash.setLocationRelativeTo(null);
		splash.setVisible(true);
		panel.setText(Language.string("Preparing streams..."));
		initializeStreams();
		panel.setText(Language.string("Loading configuration..."));
		initializeConfiguration();
		panel.setText(Language.string("Initializing data..."));
		initializeData();
		panel.setText(Language.string("Generating graphic interface..."));
		initializeGUI();
		panel.setText(Language.string("Ready!"));
		try{
			Thread.sleep(SHOW_TIMEOUT);
		}catch(InterruptedException ie){
			//i dont care
		}
		splash.setVisible(false);
		splash.dispose();
		gui.setVisible(true);
	}

	public void uncaughtException(Thread t, Throwable e) {
		logger.error("Uncaught exception in "+t.toString(), e);
		JOptionPane.showMessageDialog(null, e.getLocalizedMessage(), Language.string("Uncaught exception"), JOptionPane.ERROR_MESSAGE);
	}
	
	public DatabaseInterface getDatabase(){
		return database;
	}
	
	public Agenda getAgenda(){
		return agenda;
	}
	
	public AddressBook getAddressBook(){
		return addressBook;
	}
	
	public Accounting getAccounting(){
		return accounting;
	}
	
	public Comuni getComuni(){
		return comuni;
	}
	
	public JStudioGUI getGUI(){
		return gui;
	}
}
