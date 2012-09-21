package jstudio.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.util.Collection;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

import jstudio.control.Controller;
import jstudio.gui.generic.EntityManagerPanel;
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
	
	public AddressBookPanel(Controller<Person> controller){
		super(controller);
		this.setLayout(new BorderLayout());
		model = new AddressBookTableModel(table);

		JScrollPane scrollpane = new JScrollPane(table);
		//scrollpane.setPreferredSize(new Dimension(this.getWidth(),this.getHeight()));
		this.add(scrollpane, BorderLayout.CENTER);

		JToolBar actionPanel = new JToolBar(Language.string("Actions"));
		actionPanel.setFloatable(false);
		actionPanel.add(newButton);
		actionPanel.addSeparator();
		actionPanel.add(viewButton);
		actionPanel.add(editButton);
		actionPanel.add(deleteButton);
		actionPanel.add(Box.createHorizontalGlue());
		actionPanel.add(filterField);
		actionPanel.setPreferredSize(new Dimension(0,25));
		actionPanel.add(refreshButton);
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
	
	public synchronized void filter(String text){
		text = text.trim();
		if(text.length()==0){
			this.refresh();
			return;
		}
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
			this.refresh();
		} else if(o==viewButton){
			int row = table.convertRowIndexToModel(table.getSelectedRow());
			if(row>=0){
				logger.debug("ROW Selected "+row);
				showEntity((Person)model.getValueAt(row, 0), false);
				this.refresh();
			}
		} else if(o==editButton){
			int row = table.convertRowIndexToModel(table.getSelectedRow());
			if(row>=0){
				logger.debug("ROW Selected "+row);
				showEntity((Person)model.getValueAt(row, 0), true);
				this.refresh();
			}
		} else if(o==deleteButton){
			int row = table.convertRowIndexToModel(table.getSelectedRow());
			if(row>=0){
				logger.debug("ROW Selected "+row);
				Person context = (Person)model.getValueAt(row, 0);
				int ch = JOptionPane.showConfirmDialog(this, 
						Language.string("Are you sure you want to remove {0} {1}?",context.getName(),context.getLastname()),
						Language.string("Remove person?"), 
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
