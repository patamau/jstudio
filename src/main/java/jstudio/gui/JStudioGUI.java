package jstudio.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;

import javax.swing.Box;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;

import org.apache.log4j.Logger;

import jstudio.JStudio;
import jstudio.db.DatabaseObject;
import jstudio.gui.generic.EntityManagerPanel;
import jstudio.model.Invoice;
import jstudio.report.ReportChooser;
import jstudio.report.ReportGenerator;
import jstudio.util.Configuration;
import jstudio.util.ConfigurationDialog;
import jstudio.util.Language;

@SuppressWarnings("serial")
public class JStudioGUI extends JFrame implements ActionListener {
	
	private static final Logger logger = Logger.getLogger(JStudioGUI.class);
	
	public static final String
		WIDTH_KEY = "window.width",
		HEIGHT_KEY = "window.height";
	
	public static final int 
		WIDTH_DEF=640, 
		HEIGHT_DEF=480;
	
	public static final String
		PIC_AGENDA="eventicon.png",
		PIC_ACCOUNTING="invoiceicon.png";
	
	private boolean initialized = false;
	private JStudio app;
	private JMenuItem 
		optionsItem,
		connectionItem,
		exitItem;
	private JMenuItem
		customPrintItem,
		pruneItem,
		backupItem,
		restoreItem,
		loadItem,
		clearItem,
		aboutItem;
		
	private JMenu viewMenu;
	private JLabel statusLabel;
	private JTabbedPane tabbedPane;

	public JStudioGUI(String title, JStudio app){
		super(title);
		super.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		super.setSize(
				Configuration.getGlobal(WIDTH_KEY, WIDTH_DEF),
				Configuration.getGlobal(HEIGHT_KEY, HEIGHT_DEF));
		super.setLocationRelativeTo(null);
		statusLabel = new JLabel(Language.string("Ready..."));
		this.app = app;
		logger.debug("GUI initialized");
	}
	
	public JStudio getApplication(){
		return app;
	}
	
	public void addPanel(final EntityManagerPanel<? extends DatabaseObject> panel){
		tabbedPane.addTab(panel.getLabel(), panel.getIcon(), panel);
		JMenuItem item = new JMenuItem(panel.getLabel(), panel.getIcon());
		item.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				tabbedPane.setSelectedComponent(panel);
			}			
		});
		viewMenu.add(item);
		//panel.refresh();
	}
	
	public void createGUI(){
		//only one gui created at a time please
		if(initialized) return;
		initialized = true;
		
		getContentPane().setLayout(new BorderLayout());
		
		//create menu bar
		JMenuBar menuBar = new JMenuBar();
		JMenu fileMenu = new JMenu(Language.string("File"));		
		
		connectionItem = new JMenuItem(Language.string("Connection..."));
		connectionItem.addActionListener(this);
		optionsItem = new JMenuItem(Language.string("Advanced options..."));
		optionsItem.addActionListener(this);
		exitItem = new JMenuItem(Language.string("Quit"));
		exitItem.addActionListener(this);
		
		fileMenu.add(connectionItem);
		fileMenu.add(optionsItem);
		fileMenu.add(new JSeparator());
		fileMenu.add(exitItem);
		menuBar.add(fileMenu);
		
		//create view menu
		viewMenu = new JMenu(Language.string("View"));
		menuBar.add(viewMenu);
		
		//create tools menu		
		JMenu toolsMenu = new JMenu(Language.string("Tools"));		
		customPrintItem = new JMenuItem(Language.string("Custom print..."));
		customPrintItem.addActionListener(this);
		toolsMenu.add(customPrintItem);
		toolsMenu.add(new JSeparator());
		pruneItem = new JMenuItem(Language.string("Prune..."));
		pruneItem.addActionListener(this);
		toolsMenu.add(pruneItem);
		toolsMenu.add(new JSeparator());
		backupItem = new JMenuItem(Language.string("Backup..."));
		backupItem.addActionListener(this);
		toolsMenu.add(backupItem);
		restoreItem = new JMenuItem(Language.string("Restore..."));
		restoreItem.addActionListener(this);
		toolsMenu.add(restoreItem);
		clearItem = new JMenuItem(Language.string("Reset"));
		clearItem.addActionListener(this);
		toolsMenu.add(clearItem);
		toolsMenu.add(new JSeparator());
		loadItem = new JMenuItem(Language.string("Load SQL..."));
		loadItem.addActionListener(this);
		toolsMenu.add(loadItem);
		menuBar.add(toolsMenu);
		
		//create help menu		
		JMenu helpMenu = new JMenu(Language.string("Help"));
		aboutItem = new JMenuItem(Language.string("About..."));
		aboutItem.addActionListener(this);
		helpMenu.add(aboutItem);
		menuBar.add(Box.createHorizontalGlue());
		menuBar.add(helpMenu);

		setJMenuBar(menuBar);
		JToolBar statusBar = new JToolBar();
		statusBar.setFloatable(false);
		statusBar.add(statusLabel);
		
		getContentPane().add(statusBar, BorderLayout.SOUTH);
		
		tabbedPane = new JTabbedPane();
		getContentPane().add(tabbedPane, BorderLayout.CENTER);
	}

	public void actionPerformed(ActionEvent event) {
		Object src = event.getSource();
		if(src==backupItem){
			app.doBackup();
		}else if(src==restoreItem){
			app.doRestore();
		}else if(src==clearItem){
			app.doClear();
		}else if(src==loadItem){
			app.doLoad();
		}else if(src==pruneItem){
			app.doPrune();
		}else if(src==optionsItem){
			ConfigurationDialog cdialog = new ConfigurationDialog(this);
			Configuration c = cdialog.showDialog(Configuration.getGlobalConfiguration());
			Configuration.setGlobalConfiguration(c);
		}else if(src==connectionItem){
			DBDialog dbdialog = new DBDialog(this,app.getDatabase());
			if(dbdialog.showDialog(Configuration.getGlobalConfiguration())){
				app.setDatabase(dbdialog.getDatabase());
			}
		}else if(src==exitItem){
			this.dispose();
		}else if(src==aboutItem){
			JOptionPane.showMessageDialog(this, JStudio.class.getSimpleName()+" "+JStudio.VERSION+" ("+JStudio.BUILD+")\n"+JStudio.AUTHOR, JStudio.class.getSimpleName(), JOptionPane.INFORMATION_MESSAGE);
		}else if(src==customPrintItem){
			ReportGenerator rg = new ReportGenerator();
			rg.setHeadValue("date", Invoice.dateFormat.format(new Date()));
			ReportChooser rc = new ReportChooser(rg);
			rc.showGUI(this);
		}
	}
	
	public void setStatusLabel(String text){
		statusLabel.setText(text);
	}
	
	public void finalize(){
		for(Component c: tabbedPane.getComponents()){
			EntityManagerPanel<?> p = (EntityManagerPanel<?>)c;
			p.finalize();
		}
	}
	
	public void dispose(){
		super.dispose();
		finalize();
	}
}
