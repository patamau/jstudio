package jstudio;

import java.io.File;
import java.util.Date;

import javax.swing.JFileChooser;
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
import jstudio.util.DatePicker;
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
	
	public static final String
		BACKUPFILE_KEY = "backup.file",
		BACKUPFILE_DEF = "jstudio.bak",
		BACKUP_EXT = ".jsbak";
	
	public static final long SHOW_TIMEOUT = 1000;
	
	private static final Logger logger = LoggerFactory.getLogger(JStudio.class);
	
	//dedicated graphical user interface
	private JStudioGUI gui;
	//dedicated configuration (avoid using the global one)
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
			//database.dump(new File("db.dump"));
			//database.clear();
			//database.restore(new File("db.dump"));
		} catch (Throwable e) {
			logger.error("Database connection error",e);
			JOptionPane.showMessageDialog(null, Language.string("Database connection error")+": "+e.getLocalizedMessage(), Language.string("Data initialization"), JOptionPane.ERROR_MESSAGE);
		}
		//initialize data handlers
		agenda = new Agenda(this);
		addressBook = new AddressBook(this);
		accounting = new Accounting(this);
		comuni = new Comuni(this);
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
	
	/**
	 * BEWARE: After this call initialize() is required for the rest of the stuff to work properly again :)
	 */
	public void finalize(){
		//kill all gui listeners
		gui.finalize();
		//async call, dont care
		database.close();
		// I dont care if overwrite
		Configuration.getGlobalConfiguration().save(new File(this.getClass().getSimpleName()+Configuration.FILE_SUFFIX));		
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
	
	public void doRestore(){
		JFileChooser fc = new JFileChooser();
		File lastBackup = new File(Configuration.getGlobal(BACKUPFILE_KEY, BACKUPFILE_DEF));
		fc.setSelectedFile(lastBackup);
		fc.setCurrentDirectory(lastBackup);
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		int ch = fc.showOpenDialog(gui);
		if(ch!=JFileChooser.APPROVE_OPTION){
			logger.info("Restore canceled by user");
			return;
		}
		File f = fc.getSelectedFile();
		if(!f.exists()||!f.canRead()){
			JOptionPane.showMessageDialog(gui, 
					Language.string("Unable to access backup file"),
					Language.string("Restore error"),
					JOptionPane.ERROR_MESSAGE);
		}else{
			if(doClear()){
				try {
					database.restore(f);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public boolean doClear(){
		int ch = JOptionPane.showConfirmDialog(gui, 
				Language.string("Are you really-really sure you want to remove all the data from the database?\n Everything lost forever?\n Are you Sure?"),
				Language.string("Really-really sure?"),
				JOptionPane.YES_NO_OPTION,
				JOptionPane.WARNING_MESSAGE);
		if(ch==JOptionPane.YES_OPTION){
			database.clear();
			return true;
		}else{
			return false;
		}
	}
	
	public void doBackup(){
		JFileChooser fc = new JFileChooser();
		File lastBackup = new File(Configuration.getGlobal(BACKUPFILE_KEY, BACKUPFILE_DEF));
		fc.setSelectedFile(lastBackup);
		fc.setCurrentDirectory(lastBackup);
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		int ch = fc.showSaveDialog(gui);
		if(ch!=JFileChooser.APPROVE_OPTION){
			logger.info("Backup canceled by user");
			return;
		}
		File sf = fc.getSelectedFile();
		Configuration.getGlobalConfiguration().setProperty(BACKUPFILE_KEY, sf.getAbsolutePath());
		String filename = sf.getName();
		int extpos = filename.lastIndexOf('.');
		if(extpos>=0){
			filename = filename.substring(0, extpos);
			filename += DatePicker.getTimestamp(new Date());
			filename += BACKUP_EXT;
		}
		String fullpath = sf.getParentFile().getAbsolutePath()+File.separatorChar+filename;
		File actualBackupFile = new File(fullpath); //TODO
		if(actualBackupFile.exists()){
			ch = JOptionPane.showConfirmDialog(gui, 
					Language.string("A file with the same name already exists: are you sure you want to overwrite?"),
					Language.string("Overwrite?"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if(ch!=JOptionPane.YES_OPTION){
				logger.info("Backup overwrite aborted by user");
				return;
			}
		}
		try {
			database.dump(actualBackupFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
