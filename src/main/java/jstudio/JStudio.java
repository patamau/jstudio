package jstudio;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Constructor;
import java.text.MessageFormat;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JWindow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jstudio.control.Accounting;
import jstudio.control.AddressBook;
import jstudio.control.Agenda;
import jstudio.db.DatabaseInterface;
import jstudio.gui.AccountingPanel;
import jstudio.gui.AddressBookPanel;
import jstudio.gui.AgendaPanel;
import jstudio.gui.DBDialog;
import jstudio.gui.JStudioGUI;
import jstudio.util.Configuration;
import jstudio.util.DatePicker;
import jstudio.util.IconPanel;
import jstudio.util.Language;
import jstudio.util.Resources;

/**
 * This is the main class for the control flow of the JStudio application
 * It controls DB structures and the GUI front-end
 * @author Matteo
 *
 */
public class JStudio implements UncaughtExceptionHandler{

	public static final String 
		VERSION = "0.5.0a",
		BUILD = JStudio.class.getPackage().getImplementationVersion(),
		AUTHOR = JStudio.class.getPackage().getImplementationVendor(),
		SPLASH_SCREEN = "splash.png",
		DB_HOST = "localhost",
		DB_NAME = "jstudio",
		DB_USER = "jstudio",
		DB_PASS = "jstudio137";
	
	public static final String
		LOADFILE_KEY = "load.file",
		LOADFILE_DEF = ".",
		BACKUPFILE_KEY = "backup.file",
		BACKUPFILE_DEF = ".",
		CUSTOMNAME_KEY = "name.custom",
		CUSTOMNAME_DEF = "";
	
	public static final MessageFormat backupNameFormat = new MessageFormat("jstudio_{0}.bak"); 
	
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

	public JStudio(){
	}
	
	private void initializeGUI(){
		//initialize GUI
		String customName = Configuration.getGlobal(CUSTOMNAME_KEY, CUSTOMNAME_DEF);
		String clsName = this.getClass().getSimpleName() + " " + VERSION;
		String appName = 0 < customName.length() ? appName = clsName + " - " + customName : clsName;
		gui = new JStudioGUI(appName, this);
		gui.setIconImage(Resources.getImage("appicon.png").getImage());
		gui.createGUI();
		gui.addPanel(new AddressBookPanel(addressBook));
		gui.addPanel(new AgendaPanel(agenda));
		gui.addPanel(new AccountingPanel(accounting));
	}	
	
	/**
	 * Using currently set configuration parameters
	 * creates a new DatabaseInterface object
	 * @return
	 * @throws Exception
	 */
	public static DatabaseInterface getDatabaseInterface(
			final String jdbc,
			final String protocol,
			final String driver) 
	throws Exception {
		//initialize database manager
		//TODO: load jdbc class dynamically
		//database = new HibernateDB(protocol, driver);
		//XXX: use sqlite prefix and standard driver
		@SuppressWarnings("unchecked")
		Class<DatabaseInterface> d = (Class<DatabaseInterface>) Class.forName(jdbc);
        Class<?> partypes[] = new Class[]{ String.class, String.class };
        Constructor<DatabaseInterface> dc = d.getConstructor(partypes);
        Object arglist[] = new String[]{ protocol, driver };
        return dc.newInstance(arglist);
	}
	
	private boolean initializeData(){
		//keep tring to connect to a db
		//every loop asks for credentatials
		do{
			try {
				String jdbc = Configuration.getGlobal(DatabaseInterface.KEY_JDBC,DatabaseInterface.DEF_JDBC);
				String protocol = Configuration.getGlobal(DatabaseInterface.KEY_PROTOCOL,DatabaseInterface.DEF_PROTOCOL);
				String driver = Configuration.getGlobal(DatabaseInterface.KEY_DRIVER,DatabaseInterface.DEF_DRIVER);
				database = getDatabaseInterface(jdbc, protocol, driver);
				String hostname = Configuration.getGlobal(DatabaseInterface.KEY_HOST, DatabaseInterface.DEF_HOST);
				String dbname = Configuration.getGlobal(DatabaseInterface.KEY_NAME, DatabaseInterface.DEF_NAME);
				String user = Configuration.getGlobal(DatabaseInterface.KEY_USER, DatabaseInterface.DEF_USER);
				String password = Configuration.getGlobal(DatabaseInterface.KEY_PASS, DatabaseInterface.DEF_PASS);
				database.connect(hostname, dbname, user, password);
			} catch (Exception e) {
				logger.error("Database connection error",e);
				String msg = Language.string("Database connection error");
				msg += ": "+e.getLocalizedMessage();
				JOptionPane.showMessageDialog(null, 
						msg, 
						Language.string("Data initialization"), 
						JOptionPane.ERROR_MESSAGE);
				DBDialog dbdialog = new DBDialog(gui, database);
				if(!dbdialog.showDialog(configuration)){
					return false;
				}else{
					database = dbdialog.getDatabase();
				}
			}
		}while(!database.isConnected());
		//initialize data handlers
		agenda = new Agenda(this);
		addressBook = new AddressBook(this);
		accounting = new Accounting(this);
		return true;
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
		if(!initializeData()){
			throw new RuntimeException(Language.string("Connection to database aborted"));
		}
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
		if(agenda.countAllToPrune(new Date())>0){
			doPrune();
		}
	}

	public void uncaughtException(Thread t, Throwable e) {
		gui.setStatusLabel(e.getLocalizedMessage());
		logger.error("Uncaught exception in "+t.toString(), e);
		JOptionPane.showMessageDialog(null, e.getLocalizedMessage(), Language.string("Uncaught exception"), JOptionPane.ERROR_MESSAGE);
	}
	
	/**
	 * BEWARE: After this call initialize() is required for the rest of the stuff to work properly again :)
	 */
	public void finalize(){
		logger.debug("Finalizing");
		//kill all gui listeners
		if(gui!=null) {
			gui.finalize();
			logger.debug("GUI finalized");
			gui = null;
		}
		//async call, dont care
		if(database!=null){
			database.close();
			logger.debug("DB closed");
			database = null;
		}
		//configuration save!
		if(Configuration.getGlobalConfiguration().isModified()){
			Configuration.getGlobalConfiguration().save(new File(this.getClass().getSimpleName()+Configuration.FILE_SUFFIX));
			logger.debug("Configuration saved");
		}
	}
	
	/**
	 * Force the app to use the database specified.
	 * This happens when a new driver has been loaded from a dialog
	 * @param database
	 */
	public void setDatabase(final DatabaseInterface database){
		this.database = database;
		//initialize data handlers
		agenda = new Agenda(this);
		addressBook = new AddressBook(this);
		accounting = new Accounting(this);
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
	
	public JStudioGUI getGUI(){
		return gui;
	}
	
	public void doLoad(){
		JFileChooser fc = new JFileChooser();
		File lastBackup = new File(Configuration.getGlobal(LOADFILE_KEY, LOADFILE_DEF));
		fc.setSelectedFile(lastBackup);
		fc.setCurrentDirectory(lastBackup.getParentFile());
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		int ch = fc.showOpenDialog(gui);
		if(ch!=JFileChooser.APPROVE_OPTION){
			gui.setStatusLabel(Language.string("Load canceled by user"));
			logger.info("Load canceled by user");
			return;
		}
		File f = fc.getSelectedFile();
		if(!f.exists()||!f.canRead()){
			gui.setStatusLabel(Language.string("Unable to access {0}", f.getName()));
			JOptionPane.showMessageDialog(gui, 
					Language.string("Unable to access file"),
					Language.string("Load error"),
					JOptionPane.ERROR_MESSAGE);
		}else{
			Configuration.getGlobalConfiguration().setProperty(LOADFILE_KEY, f.getAbsolutePath());
			askClear();
			String str;
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new FileReader(f));
				int c = 0;
				while((str = reader.readLine())!=null){
					str = str.trim();
					//logger.debug("Read: "+str);
					if(str.length()==0) continue;
					if(str.startsWith("-")) continue;
					database.execute(str);
					++c;
					//break; //FIXME: remove this break to continue loading stuff
				}
				logger.info("loaded "+c+" lines");
				JOptionPane.showMessageDialog(gui, 
						Language.string("${0} SQL statements executed on the database",c),
						Language.string("Load finished"),
						JOptionPane.INFORMATION_MESSAGE);
			} catch (Exception e) {
				logger.error("Loading "+f,e);
				JOptionPane.showMessageDialog(gui, 
						e.getLocalizedMessage(),
						Language.string("Loading error"),
						JOptionPane.ERROR_MESSAGE);
			} finally {
				if(null != reader){
					try{
						reader.close();
					}catch(IOException e){
						logger.error("closing buffered reader on "+f+": "+e);
					}
				}
			}
		}
	}
	
	public void doRestore(){
		JFileChooser fc = new JFileChooser();
		File lastBackup = new File(Configuration.getGlobal(BACKUPFILE_KEY, BACKUPFILE_DEF));
		fc.setSelectedFile(lastBackup);
		fc.setCurrentDirectory(lastBackup.getParentFile());
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		int ch = fc.showOpenDialog(gui);
		if(ch!=JFileChooser.APPROVE_OPTION){
			logger.info("Restore canceled by user");
			gui.setStatusLabel(Language.string("Restore canceled by user"));
			return;
		}
		final File f = fc.getSelectedFile();
		if(!f.exists()||!f.canRead()){
			gui.setStatusLabel(Language.string("Unable to access {0}",f.getName()));
			JOptionPane.showMessageDialog(gui, 
					Language.string("Unable to access backup file"),
					Language.string("Restore error"),
					JOptionPane.ERROR_MESSAGE);
		}else{
			if(doClear()){
				//TODO: try to improve this somehow
				final JDialog progressMonitor = new JDialog(gui);
				progressMonitor.setTitle(Language.string("Restore in progress..."));
				progressMonitor.setModal(true);
				final Thread t = new Thread(new Runnable(){
					public void run(){						
						progressMonitor.setVisible(true);
						logger.debug("Progress monitor window was closed");
					}
				});
				final Thread w = new Thread(new Runnable(){
					public void run(){
						JLabel status = new JLabel(Language.string("Loading {0}...",f.getName()));
						progressMonitor.setLayout(new GridBagLayout());
						GridBagConstraints gc = new GridBagConstraints();
						gc.insets = new Insets(5,5,5,5);
						gc.gridx=gc.gridy=0;
						gc.weightx=1.0f;
						gc.fill=GridBagConstraints.HORIZONTAL;
						status.setAlignmentX(JLabel.CENTER_ALIGNMENT);
						status.setHorizontalAlignment(JLabel.CENTER);
						progressMonitor.add(status, gc);
						gc.gridy++;
						JProgressBar progressBar = new JProgressBar(JProgressBar.HORIZONTAL, 100);
						progressBar.setIndeterminate(true);
						progressBar.setStringPainted(true);
						progressMonitor.add(progressBar, gc);
						JButton closeButton = new JButton(Language.string("Close"));
						closeButton.addActionListener(new ActionListener(){
							public void actionPerformed(ActionEvent e) {
								progressMonitor.dispose();
							}
						});
						closeButton.setEnabled(false);
						gc.gridy++;
						progressMonitor.add(closeButton, gc);
						progressMonitor.setResizable(false);
						progressMonitor.setSize(400, 250);
						progressMonitor.setLocationRelativeTo(gui);
						for(int i=0; ; ++i){
							//progressBar.setValue(i%100);
							try{
								Thread.sleep(500);
							}catch(InterruptedException e){
								break;
							}
						}
						logger.debug("Progress monitor thread interrupted");
						progressBar.setIndeterminate(false);
						progressBar.setValue(100);
						status.setText(Language.string("Finished loading {0}",f.getName()));
						closeButton.setEnabled(true);
					}
				});
				w.start();
				t.start();
				final Thread r = new Thread(new Runnable(){
					public void run(){
						try {
							database.restore(f);
							w.interrupt();
							gui.setStatusLabel(Language.string("Database restored from {0}",f.getName()));
						} catch (Exception e) {
							gui.setStatusLabel(Language.string("Restore error"));
							JOptionPane.showMessageDialog(gui, e.getLocalizedMessage(), Language.string("Restore error"), JOptionPane.ERROR_MESSAGE);
						}
					}
				});
				r.start();
				gui.setStatusLabel(Language.string("Started restore from {0}",f.getName()));
			}
		}
	}
	
	public boolean askClear(){
		int ch = JOptionPane.showConfirmDialog(gui, 
				Language.string("Do you want to remove all the data from the database?"),
				Language.string("Clear database?"),
				JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE);
		if(ch==JOptionPane.YES_OPTION){
			database.clear();
			gui.setStatusLabel(Language.string("Database cleared"));
			return true;
		}else{
			gui.setStatusLabel(Language.string("Database clear canceled"));
			return false;
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
			gui.setStatusLabel(Language.string("Database cleared"));
			return true;
		}else{
			gui.setStatusLabel(Language.string("Database clear canceled"));
			return false;
		}
	}
	
	public void doBackup(){
		JFileChooser fc = new JFileChooser();
		File lastBackup = new File(Configuration.getGlobal(BACKUPFILE_KEY, BACKUPFILE_DEF));
		fc.setSelectedFile(lastBackup);
		fc.setCurrentDirectory(lastBackup.getParentFile());
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int ch = fc.showSaveDialog(gui);
		if(ch!=JFileChooser.APPROVE_OPTION){
			logger.info("Backup canceled by user");
			return;
		}
		File sf = fc.getSelectedFile();
		if(!sf.exists()||!sf.canWrite()||!sf.isDirectory()){
			logger.error("Wrong folder "+sf.getAbsolutePath());
			JOptionPane.showMessageDialog(gui,
					Language.string("Unable to write to the specified folder"),
					Language.string("Write error"),
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		String filename = backupNameFormat.format(new Object[]{DatePicker.getTimestamp(new Date())});
		sf = new File(sf.getAbsoluteFile()+File.separator+filename);
		if(sf.exists()){
			ch = JOptionPane.showConfirmDialog(gui, 
					Language.string("A file with the same name already exists: are you sure you want to overwrite?"),
					Language.string("Overwrite?"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if(ch!=JOptionPane.YES_OPTION){
				logger.info("Backup overwrite aborted by user");
				return;
			}
		}
		boolean created = false;
		try {
			created = sf.createNewFile();
		} catch (Exception e1) {
			logger.error("Creating new dump file "+sf.getAbsolutePath(),e1);
		} 
		if(!created){
			logger.error("Cannot create file "+sf.getAbsolutePath());
			JOptionPane.showMessageDialog(gui,
					Language.string("Unable to create the target file"),
					Language.string("Write error"),
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		Configuration.getGlobalConfiguration().setProperty(BACKUPFILE_KEY, sf.getAbsolutePath());
		try {
			database.dump(sf);
			gui.setStatusLabel(Language.string("Backup {0} completed", sf.getName()));		
		} catch (Exception e) {
			gui.setStatusLabel(Language.string("Backup {0} error", sf.getName()));	
			JOptionPane.showMessageDialog(gui, e.getLocalizedMessage(), Language.string("Dump error"), JOptionPane.ERROR_MESSAGE);
		}
	}
	
	public void doPrune(){
		final Date d = new Date();
		final int c = agenda.countAllToPrune(d);
		if(c==0){
			JOptionPane.showMessageDialog(gui, Language.string("No events to prune"), Language.string("Pruning events"), JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		final int n = -Configuration.getGlobal(Agenda.PRUNE_DAYS_KEY, Agenda.PRUNE_DAYS_DEF);
		final int ch = JOptionPane.showConfirmDialog(gui, 
				Language.string("This will remove all events older than {0} days ({1} events).\nPlease double check your system date is correct before proceeding.\nYour system date is {2}", n, c, d),
				Language.string("Confirm pruning"),
				JOptionPane.YES_NO_OPTION,
				JOptionPane.WARNING_MESSAGE);
		if(ch==JOptionPane.YES_OPTION){
			if(agenda.removeAllBefore(d)){
				gui.setStatusLabel(Language.string("{0} events pruned", c));
			}else{
				JOptionPane.showMessageDialog(gui, 
						Language.string("Pruning encountered an error while removing events"), 
						Language.string("Prune error"), 
						JOptionPane.ERROR_MESSAGE);
				gui.setStatusLabel(Language.string("Prune error"));
			}
		}else{
			gui.setStatusLabel(Language.string("Pruning aborted by user"));
		}
	}
}
