package jstudio.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;

import jstudio.JStudio;
import jstudio.db.DatabaseObject;
import jstudio.gui.generic.EntityManagerPanel;
import jstudio.util.Configuration;
import jstudio.util.ConfigurationDialog;
import jstudio.util.Language;

@SuppressWarnings("serial")
public class JStudioGUI extends JFrame implements ActionListener {
	
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
		
	private JMenu viewMenu;
	private JLabel statusLabel;
	private JTabbedPane tabbedPane;
	//private ArrayList<EntityManagerPanel<? extends DatabaseObject>> panels;

	public JStudioGUI(String title, JStudio app){
		super(title);
		super.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		super.setSize(
				Configuration.getGlobal(WIDTH_KEY, WIDTH_DEF),
				Configuration.getGlobal(HEIGHT_KEY, HEIGHT_DEF));
		super.setLocationRelativeTo(null);
		statusLabel = new JLabel(Language.string("Ready..."));
		this.app = app;
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
		panel.refresh();
	}
	
	public void createGUI(){
		//only one gui created at a time
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
		toolsMenu.add(new JMenuItem("Backup"));
		toolsMenu.add(new JMenuItem("Restore"));
		toolsMenu.add(new JMenuItem("Clear"));		
		menuBar.add(toolsMenu);
		
		//create help menu		
		JMenu helpMenu = new JMenu(Language.string("Help"));
		helpMenu.add(new JMenuItem("Credits"));
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
		if(src==optionsItem){
			ConfigurationDialog cdialog = new ConfigurationDialog(this);
			Configuration c = cdialog.showDialog(Configuration.getGlobalConfiguration());
			Configuration.setGlobalConfiguration(c);
		}else if(src==connectionItem){
			DBDialog dbdialog = new DBDialog(this,app.getDatabase());
			dbdialog.showDialog(Configuration.getGlobalConfiguration());			
		}else if(src==exitItem){
			this.dispose();
		}
	}
	
	public void setStatusLabel(String text){
		statusLabel.setText(text);
	}
}
