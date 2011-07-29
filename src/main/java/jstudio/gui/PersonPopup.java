package jstudio.gui;

import java.awt.event.ActionEvent;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import jstudio.control.Controller;
import jstudio.model.Person;
import jstudio.util.ContextualMenu;
import jstudio.util.Language;

@SuppressWarnings("serial")
public class PersonPopup extends ContextualMenu<Person> {
	
	public PersonPopup(JFrame parent, Controller<Person> controller){
		super(parent, controller);
	}

	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		if(o==viewItem){
			JDialog dialog = PersonPanel.createDialog(parent, context, null);
			dialog.setVisible(true);
		}else if(o==editItem){
			JDialog dialog = PersonPanel.createDialog(parent, context, controller);
			dialog.setVisible(true);
		}else if(o==removeItem){
			int ch = JOptionPane.showConfirmDialog(parent, 
					Language.string("Are you sure you want to remove {0} {1}?",context.getName(),context.getLastname()),
					Language.string("Romove contact?"), 
					JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if(ch==JOptionPane.YES_OPTION){
				//TODO: remove the entry
			}
		}
	}

}
