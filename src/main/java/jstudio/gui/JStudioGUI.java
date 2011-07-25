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
		parchiveItem,
		invoiceItem;
		
	private JLabel statusLabel;
	private JTabbedPane tabbedPane;
	private AgendaPanel agendaPanel;
	private AddressBookPanel contactsPanel;
	private InvoicePanel invoicePanel;

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
		parchiveItem = new JMenuItem(Language.string("Address book"), Resources.getImage(PIC_ADDRESSBOOK));
		parchiveItem.addActionListener(this);
		agendaItem = new JMenuItem(Language.string("Agenda"), Resources.getImage(PIC_AGENDA));
		agendaItem.addActionListener(this);
		invoiceItem = new JMenuItem(Language.string("Accounting"), Resources.getImage(PIC_ACCOUNTING));
		invoiceItem.addActionListener(this);
		viewMenu.add(parchiveItem);
		viewMenu.add(agendaItem);
		viewMenu.add(invoiceItem);
		menuBar.add(viewMenu);
		setJMenuBar(menuBar);
		
		JToolBar statusBar = new JToolBar();
		statusBar.setFloatable(false);
		statusBar.add(statusLabel);
		
		getContentPane().add(statusBar, BorderLayout.SOUTH);
		
		tabbedPane = new JTabbedPane();
		getContentPane().add(tabbedPane, BorderLayout.CENTER);
		//initialize sub panels
		contactsPanel = new AddressBookPanel(this);
		agendaPanel = new AgendaPanel(this);
		invoicePanel = new InvoicePanel();
		//by default show contacts
		tabbedPane.addTab(Language.string("Address book"),
				Resources.getImage(PIC_ADDRESSBOOK),
				contactsPanel);
		tabbedPane.addTab(Language.string("Agenda"),
				Resources.getImage(PIC_AGENDA),
				agendaPanel);
		tabbedPane.addTab(Language.string("Accounting"),
				Resources.getImage(PIC_ACCOUNTING),
				invoicePanel);
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
		contactsPanel.clear();
		Collection<Person> pps = app.getAddressBook().getAll();
		if(pps!=null){
			for(Person p: pps){
				contactsPanel.addPerson(p);
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
				agendaPanel.addEvent(e);
			}
		}else{
			JOptionPane.showMessageDialog(this, Language.string("Unable to load events"),Language.string("Database error"),JOptionPane.ERROR_MESSAGE);
		}
	}
	
	public void loadInvoices(){
		//TODO: invoicePanel.clear();
		Collection<Invoice> list = app.getAccounting().getAll();
		if(list!=null){
			for(Invoice i: list){
				//TODO: invoicePanel.addInvoice(i);
			}
		}else{
			JOptionPane.showMessageDialog(this, Language.string("Unable to load invoices"),Language.string("Database error"),JOptionPane.ERROR_MESSAGE);
		}
	}

	private void showPanel(JPanel panel){
		tabbedPane.setSelectedComponent(panel);
	}
	
	public void showContacts(){
		showPanel(contactsPanel);
	}
	
	public void showAgenda(){
		showPanel(agendaPanel);
	}
	
	public void showInvoice(){
		showPanel(invoicePanel);
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
		}else if(src==parchiveItem){
			showContacts();
		}else if(src==agendaItem){
			showAgenda();
		}else if(src==invoiceItem){
			showInvoice();
		}else if(src==exitItem){
			this.dispose();
		}
	}
	
	public void setStatusLabel(String text){
		statusLabel.setText(text);
	}
}
