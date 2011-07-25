package jstudio.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import jstudio.model.Person;
import jstudio.util.Language;
import jstudio.util.PopupListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PersonsPanel 
		extends JPanel 
		implements ListSelectionListener, ActionListener {
	
	private static final Logger logger = LoggerFactory.getLogger(PersonsPanel.class);

	private DefaultTableModel model;
	private JTable table;
	private JButton refreshButton;
	private JTextField filterField;
	private JStudioGUI gui;
	
	// internally used to catch a double click on the table
	private int lastSelectedRow = -1;
	private long lastSelectionTime = 0;
	
	public PersonsPanel(JStudioGUI gui){
		this.gui = gui;
		this.setLayout(new BorderLayout());
		
		table = new JTable();
		model = new PersonsTableModel(table);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.getSelectionModel().addListSelectionListener(this);

		JScrollPane scrollpane = new JScrollPane(table);
		//scrollpane.setPreferredSize(new Dimension(this.getWidth(),this.getHeight()));
		this.add(scrollpane, BorderLayout.CENTER);

		JToolBar actionPanel = new JToolBar(Language.string("Actions"));
		actionPanel.setFloatable(false);
		refreshButton = new JButton(Language.string("Refresh"));
		refreshButton.addActionListener(this);
		actionPanel.add(refreshButton);
		filterField = new JTextField();
		filterField.addActionListener(this);
		actionPanel.add(filterField);
		this.add(actionPanel, BorderLayout.NORTH);
		
	    table.addMouseListener(new PopupListener<Person>(table, new PersonPopup(this.gui)));
	}
	
	public void valueChanged(ListSelectionEvent event) {
        int viewRow = table.getSelectedRow();
        if (0<=viewRow){        
        	System.err.println("Selected "+viewRow+" at "+System.currentTimeMillis()+" and last time was "+lastSelectionTime);
        	if(viewRow==lastSelectedRow&&
        			200>(System.currentTimeMillis()-lastSelectionTime)){
        		showPerson((Person)table.getValueAt(viewRow, 0));	
        		table.getSelectionModel().removeSelectionInterval(viewRow, viewRow);
        		lastSelectedRow = -1;
        	}else{
            	lastSelectedRow = viewRow;
            	lastSelectionTime = System.currentTimeMillis();
        		table.getSelectionModel().removeSelectionInterval(viewRow, viewRow);
        	}
        }
    }
	
	public void showPerson(Person p){
		JDialog dialog = PersonPanel.createDialog(gui, p, false);
		dialog.setVisible(true);
	}
	
	public synchronized void clear(){
		while(model.getRowCount()>0) model.removeRow(0);
	}

	public synchronized void addPerson(Person p){
		model.addRow(new Object[]{
				p,
				p.getName(),
				p.getLastname(),
				p.getBirthdate(),
				p.getAddress(),
				p.getPhone()});
	}
	
	public synchronized void removePerson(int id){
		Person p;
		int frow = -1;
		for(int i=0; i<model.getRowCount(); i++){
			p = (Person)model.getValueAt(i, 0);
			if(p.getId()==id){
				frow = i;
				break;
			}
		}
		if(frow>-1){
			model.removeRow(frow);
		}
	}

	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		if(o==refreshButton){
			gui.loadContacts();
		}else if(o==filterField){
			//TODO: apply filter
		}else{
			logger.warn("Event source not mapped: "+o);
		}
	}
}
