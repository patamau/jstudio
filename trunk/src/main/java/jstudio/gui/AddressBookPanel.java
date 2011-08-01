package jstudio.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;

import jstudio.gui.generic.EntityManagerPanel;
import jstudio.gui.generic.PopupListener;
import jstudio.model.Person;
import jstudio.util.Language;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public class AddressBookPanel extends EntityManagerPanel<Person> {
	
	private static final Logger logger = LoggerFactory.getLogger(AddressBookPanel.class);

	private JButton refreshButton;
	private JTextField filterField;
	
	// internally used to catch a double click on the table
	private int lastSelectedRow = -1;
	private long lastSelectionTime = 0;
	
	public AddressBookPanel(JStudioGUI gui){
		super(gui);
		this.setLayout(new BorderLayout());
		
		table = new JTable();
		model = new AddressBookTableModel(table);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.getSelectionModel().addListSelectionListener(this);

		JScrollPane scrollpane = new JScrollPane(table);
		//scrollpane.setPreferredSize(new Dimension(this.getWidth(),this.getHeight()));
		this.add(scrollpane, BorderLayout.CENTER);

		JToolBar actionPanel = new JToolBar(Language.string("Actions"));
		actionPanel.setFloatable(false);
		refreshButton = new JButton(Language.string("Refresh"));
		refreshButton.addActionListener(this);
		refreshButton.setPreferredSize(new Dimension(60,25));
		actionPanel.add(refreshButton);
		filterField = new JTextField();
		filterField.addActionListener(this);
		filterField.setPreferredSize(new Dimension(0,25));
		actionPanel.add(filterField);
		actionPanel.setPreferredSize(new Dimension(0,25));
		this.add(actionPanel, BorderLayout.NORTH);
		
	    table.addMouseListener(new PopupListener<Person>(table, new PersonPopup(this, this.gui.getApplication().getAddressBook())));
	}
	
	public void valueChanged(ListSelectionEvent event) {
//        int viewRow = table.getSelectedRow();
//        if (0<=viewRow){        
//        	if(viewRow==lastSelectedRow&&
//        			200>(System.currentTimeMillis()-lastSelectionTime)){
//        		showPerson((Person)table.getValueAt(viewRow, 0));	
//        		table.getSelectionModel().removeSelectionInterval(viewRow, viewRow);
//        		lastSelectedRow = -1;
//        	}else{
//            	lastSelectedRow = viewRow;
//            	lastSelectionTime = System.currentTimeMillis();
//        		table.getSelectionModel().removeSelectionInterval(viewRow, viewRow);
//        	}
//        }
    }
	
	public void showEntity(Person p){
		JDialog dialog = new PersonPanel(p,null).createDialog(this.gui);
		dialog.setVisible(true);
	}

	public synchronized void addEntity(Person p){
		model.addRow(new Object[]{
				p,
				p.getName(),
				p.getBirthdate(),
				p.getCity(),
				p.getPhone()});
	}
	
	public synchronized void refresh(){
		gui.loadContacts();
	}

	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		if(o==refreshButton){
			refresh();
		}else if(o==filterField){
			//TODO: apply filter
		}else{
			logger.warn("Event source not mapped: "+o);
		}
	}
}
