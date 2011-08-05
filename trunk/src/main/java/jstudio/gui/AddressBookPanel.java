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
	
	public AddressBookPanel(JStudioGUI gui){
		super(gui);
		this.setLayout(new BorderLayout());
		
		table = new JTable();
		model = new AddressBookTableModel(table);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

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
		
		scrollpane.addMouseListener(this);
		table.addMouseListener(this);
		this.popup = new PersonPopup(this, this.gui.getApplication().getAddressBook());
	    table.addMouseListener(new PopupListener<Person>(table, super.popup));
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
