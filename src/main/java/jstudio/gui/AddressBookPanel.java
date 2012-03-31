package jstudio.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
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
import jstudio.gui.generic.FilterFieldFocusListener;
import jstudio.gui.generic.PopupListener;
import jstudio.model.Person;
import jstudio.util.Language;
import jstudio.util.Resources;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public class AddressBookPanel extends EntityManagerPanel<Person> {
	
	private static final Logger logger = LoggerFactory.getLogger(AddressBookPanel.class);

	public static final String PIC_ADDRESSBOOK="personicon.png";
	
	private JButton newButton, viewButton, editButton, deleteButton, refreshButton;
	
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
		newButton = new JButton(Language.string("New"));
		newButton.addActionListener(this);
		newButton.setMnemonic(KeyEvent.VK_N);
		actionPanel.add(newButton);
		actionPanel.addSeparator();
		viewButton = new JButton(Language.string("View"));
		viewButton.addActionListener(this);
		viewButton.setMnemonic(KeyEvent.VK_V);
		actionPanel.add(viewButton);
		editButton = new JButton(Language.string("Edit"));
		editButton.addActionListener(this);
		editButton.setMnemonic(KeyEvent.VK_E);
		actionPanel.add(editButton);
		deleteButton = new JButton(Language.string("Delete"));
		deleteButton.addActionListener(this);
		deleteButton.setMnemonic(KeyEvent.VK_D);
		actionPanel.add(deleteButton);
		actionPanel.addSeparator();
		refreshButton = new JButton(Language.string("Refresh"));
		refreshButton.addActionListener(this);
		refreshButton.setPreferredSize(new Dimension(60,25));
		refreshButton.setMnemonic(KeyEvent.VK_R);
		actionPanel.add(refreshButton);
		actionPanel.addSeparator();
		filterField = new JTextField();
		filterField.addKeyListener(this);
		filterField.setPreferredSize(new Dimension(0,25));
		filterField.addFocusListener(new FilterFieldFocusListener(filterField));
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
	
	public void showEntity(Person p, boolean edit){
		JDialog dialog = new PersonPanel(p,this, edit).createDialog((Frame)this.getTopLevelAncestor());
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
			JOptionPane.showMessageDialog(this, Language.string("Unable to load persons data"),Language.string("Database error"),JOptionPane.ERROR_MESSAGE);
		}
	}

	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		if(o==refreshButton){
			refresh();
		} else if(o==newButton){
			showEntity(new Person(), true);
		} else if(o==viewButton){
			int row = table.convertRowIndexToModel(table.getSelectedRow());
			if(row>=0){
				logger.debug("ROW Selected "+row);
				showEntity((Person)model.getValueAt(row, 0), false);
			}
		} else if(o==editButton){
			int row = table.convertRowIndexToModel(table.getSelectedRow());
			if(row>=0){
				logger.debug("ROW Selected "+row);
				showEntity((Person)model.getValueAt(row, 0), true);
			}
		} else if(o==deleteButton){
			int row = table.convertRowIndexToModel(table.getSelectedRow());
			if(row>=0){
				logger.debug("ROW Selected "+row);
				Person context = (Person)model.getValueAt(row, 0);
				int ch = JOptionPane.showConfirmDialog(this, 
						Language.string("Are you sure you want to remove {0} {1}?",context.getName(),context.getLastname()),
						Language.string("Romove person?"), 
						JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
				if(ch==JOptionPane.YES_OPTION){
					controller.delete(context);
					this.refresh();
				}
			}
		} else {
			logger.warn("Event source not mapped: "+o);
		}
	}
}
