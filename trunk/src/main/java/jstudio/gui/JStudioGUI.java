package jstudio.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Date;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;

import jstudio.JStudio;
import jstudio.model.Event;
import jstudio.model.Invoice;
import jstudio.model.Person;
import jstudio.util.Configuration;
import jstudio.util.ConfigurationDialog;
import jstudio.util.Language;
import jstudio.util.Resources;

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
		PIC_ADDRESSBOOK="personicon.png",
		PIC_ACCOUNTING="invoiceicon.png";
	
	private boolean initialized = false;
	private JStudio app;
	private JMenuItem 
		optionsItem,
		connectionItem,
		exitItem,
		agendaItem,
		addressBookItem,
		accountingItem;
		
	private JLabel statusLabel;
	private JTabbedPane tabbedPane;
	private AgendaPanel agendaPanel;
	private AddressBookPanel addressBookPanel;
	private AccountingPanel accountingPanel;

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
	
	private void createGUI(){
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
		JMenu viewMenu = new JMenu(Language.string("View"));
		addressBookItem = new JMenuItem(Language.string("Address book"), Resources.getImage(PIC_ADDRESSBOOK));
		addressBookItem.addActionListener(this);
		agendaItem = new JMenuItem(Language.string("Agenda"), Resources.getImage(PIC_AGENDA));
		agendaItem.addActionListener(this);
		accountingItem = new JMenuItem(Language.string("Accounting"), Resources.getImage(PIC_ACCOUNTING));
		accountingItem.addActionListener(this);
		viewMenu.add(addressBookItem);
		viewMenu.add(agendaItem);
		viewMenu.add(accountingItem);
		menuBar.add(viewMenu);
		setJMenuBar(menuBar);
		
		JToolBar statusBar = new JToolBar();
		statusBar.setFloatable(false);
		statusBar.add(statusLabel);
		
		getContentPane().add(statusBar, BorderLayout.SOUTH);
		
		tabbedPane = new JTabbedPane();
		getContentPane().add(tabbedPane, BorderLayout.CENTER);
		//initialize sub panels
		addressBookPanel = new AddressBookPanel(this);
		agendaPanel = new AgendaPanel(this);
		accountingPanel = new AccountingPanel(this);
		//by default show contacts
		tabbedPane.addTab(Language.string("Address book"),
				Resources.getImage(PIC_ADDRESSBOOK),
				addressBookPanel);
		tabbedPane.addTab(Language.string("Agenda"),
				Resources.getImage(PIC_AGENDA),
				agendaPanel);
		tabbedPane.addTab(Language.string("Accounting"),
				Resources.getImage(PIC_ACCOUNTING),
				accountingPanel);
	}
	
	@Override
	public void setVisible(boolean visible){
		createGUI();
		super.setVisible(visible);
		loadContacts();
		loadEvents(new Date());
		loadInvoices();
	} 
	
	public void loadContacts(){
		addressBookPanel.clear();
		Collection<Person> pps = app.getAddressBook().getAll();
		if(pps!=null){
			for(Person p: pps){
				addressBookPanel.addEntity(p);
			}
		}else{
			JOptionPane.showMessageDialog(this, Language.string("Unable to load contacts"),Language.string("Database error"),JOptionPane.ERROR_MESSAGE);
		}
	}
	
	public void loadEvents(Date date){
		agendaPanel.clear();
		Collection<Event> list = app.getAgenda().getByDate(date);
		if(list!=null){
			for(Event e: list){
				agendaPanel.addEntity(e);
			}
		}else{
			JOptionPane.showMessageDialog(this, Language.string("Unable to load events"),Language.string("Database error"),JOptionPane.ERROR_MESSAGE);
		}
	}
	
	public void loadInvoices(){
		accountingPanel.clear();
		Collection<Invoice> list = app.getAccounting().getAll();
		if(list!=null){
			for(Invoice i: list){
				accountingPanel.addEntity(i);
			}
		}else{
			JOptionPane.showMessageDialog(this, Language.string("Unable to load invoices"),Language.string("Database error"),JOptionPane.ERROR_MESSAGE);
		}
	}

	private void showPanel(JPanel panel){
		tabbedPane.setSelectedComponent(panel);
	}
	
	public void showAddressBook(){
		showPanel(addressBookPanel);
	}
	
	public void showAgenda(){
		showPanel(agendaPanel);
	}
	
	public void showAccounting(){
		showPanel(accountingPanel);
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
		}else if(src==addressBookItem){
			showAddressBook();
		}else if(src==agendaItem){
			showAgenda();
		}else if(src==accountingItem){
			showAccounting();
		}else if(src==exitItem){
			this.dispose();
		}
	}
	
	public void setStatusLabel(String text){
		statusLabel.setText(text);
	}
}
