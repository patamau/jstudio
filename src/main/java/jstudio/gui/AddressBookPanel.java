package jstudio.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.KeyListener;
import java.util.Collection;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;

import jstudio.control.Controller;
import jstudio.gui.generic.EntityManagerPanel;
import jstudio.gui.generic.PopupListener;
import jstudio.model.Person;
import jstudio.util.Language;
import jstudio.util.Resources;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public class AddressBookPanel extends EntityManagerPanel<Person>
		implements KeyListener {
	
	private static final Logger logger = LoggerFactory.getLogger(AddressBookPanel.class);

	public static final String PIC_ADDRESSBOOK="personicon.png";
	
	private JButton refreshButton;
	
	public AddressBookPanel(Controller<Person> controller){
		super(controller);
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
		filterField.addKeyListener(this);
		filterField.setPreferredSize(new Dimension(0,25));
		actionPanel.add(filterField);
		actionPanel.setPreferredSize(new Dimension(0,25));
		this.add(actionPanel, BorderLayout.NORTH);
		
		scrollpane.addMouseListener(this);
		table.addMouseListener(this);
		this.popup = new PersonPopup(this, controller);
	    table.addMouseListener(new PopupListener<Person>(table, super.popup));
	}
	
	public String getLabel(){
		return Language.string("Address book");
	}
	
	public ImageIcon getIcon(){
		return Resources.getImage(PIC_ADDRESSBOOK);
	}
	
	public void showEntity(Person p){
		JDialog dialog = new PersonPanel(p,this, false).createDialog((Frame)this.getTopLevelAncestor());
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
	
	public void filter(String text){
		text = text.trim();
		this.clear();
		String[] vals = text.split(" ");
		String[] cols = new String[]{
				"name",
				"lastname" };
		Collection<Person> ts = controller.findAll(vals, cols);
		if(ts!=null){
			for(Person t: ts){
				this.addEntity(t);
			}
		}else{
			JOptionPane.showMessageDialog(this, Language.string("Unable to load data"),Language.string("Database error"),JOptionPane.ERROR_MESSAGE);
		}
	}

	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		if(o==refreshButton){
			refresh();
		} else {
			logger.warn("Event source not mapped: "+o);
		}
	}
}
