package jstudio.gui;

import java.awt.event.ActionEvent;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import jstudio.model.Person;
import jstudio.util.ContextualMenu;
import jstudio.util.Language;

public class PersonPopup extends ContextualMenu<Person> {

	private Person person;
	
	public PersonPopup(JFrame parent){
		super(parent);
	}
	
	@Override
	public void setContext(Person context) {
		person = context;
	}

	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		if(o==viewItem){
			JDialog dialog = PersonPanel.createDialog(parent, person, false);
			dialog.setVisible(true);
		}else if(o==editItem){
			JDialog dialog = PersonPanel.createDialog(parent, person, true);
			dialog.setVisible(true);
		}else if(o==removeItem){
			int ch = JOptionPane.showConfirmDialog(parent, 
					Language.string("Are you sure you want to remove {0} {1}?",person.getName(),person.getLastname()),
					Language.string("Romove contact?"), 
					JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if(ch==JOptionPane.YES_OPTION){
				//TODO: remove the entry
			}
		}
	}

}
