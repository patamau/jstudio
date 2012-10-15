package jstudio.gui;

import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.util.Date;

import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;

import jstudio.control.Controller;
import jstudio.gui.generic.ContextualMenu;
import jstudio.gui.generic.EntityManagerPanel;
import jstudio.model.Invoice;
import jstudio.model.Person;
import jstudio.report.ReportChooser;
import jstudio.report.ReportGenerator;
import jstudio.util.Language;

import org.apache.log4j.Logger;

@SuppressWarnings("serial")
public class PersonPopup extends ContextualMenu<Person> {
	
	private static final Logger logger = Logger.getLogger(PersonPopup.class);
	
	private JMenuItem printItem;
	
	public PersonPopup(EntityManagerPanel<Person> parent, Controller<Person> controller){
		super(parent);
		this.remove(newItem);
		printItem = new JMenuItem(Language.string("Print..."));
	    printItem.setFont(deleteItem.getFont().deriveFont(Font.PLAIN));
	    printItem.addActionListener(this);
	    this.add(printItem);
		this.add(new JSeparator());
		this.add(newItem);
	}
	
	/*
	public void setContext(Person p){
		super.setContext(p);
		printItem.setEnabled(p!=null);
	}
	*/

	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		if(o==viewItem){
			JDialog dialog = new PersonPanel(context, parent, false).createDialog(parent.getTopLevelAncestor());
			dialog.setVisible(true);
		}else if(o==editItem){
			JDialog dialog = new PersonPanel(context, parent, true).createDialog(parent.getTopLevelAncestor());
			dialog.setVisible(true);
		}else if(o==deleteItem){
			int ch = JOptionPane.showConfirmDialog(parent, 
					Language.string("Are you sure you want to remove {0} {1}?",context.getName(),context.getLastname()),
					Language.string("Remove person?"), 
					JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if(ch==JOptionPane.YES_OPTION){
				controller.delete(context);
				parent.refresh();
			}
		}else if(o==newItem){
			JDialog dialog = new PersonPanel(new Person(0l), parent, true).createDialog(parent.getTopLevelAncestor());
			dialog.setVisible(true);
		}else if(o==printItem){
			ReportGenerator rg = new ReportGenerator();
			if(context!=null) rg.setHead(context);
			rg.setHeadValue("date", Invoice.dateFormat.format(new Date()));
			ReportChooser rc = new ReportChooser(rg);
			rc.showGUI((Window)parent.getTopLevelAncestor());
		}
	}

}
