package jstudio.gui;

import java.awt.event.ActionEvent;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import jstudio.control.Controller;
import jstudio.gui.generic.ContextualMenu;
import jstudio.gui.generic.EntityManagerPanel;
import jstudio.model.Person;
import jstudio.util.Language;

@SuppressWarnings("serial")
public class PersonPopup extends ContextualMenu<Person> {
	
	public PersonPopup(EntityManagerPanel<Person> parent, Controller<Person> controller){
		super(parent, controller);
	}

	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		if(o==viewItem){
			JDialog dialog = new PersonPanel(context, null).createDialog(parent.getGui());
			dialog.setVisible(true);
		}else if(o==editItem){
			JDialog dialog = new PersonPanel(context, controller).createDialog(parent.getGui());
			dialog.setVisible(true);
			parent.refresh();
		}else if(o==removeItem){
			int ch = JOptionPane.showConfirmDialog(parent, 
					Language.string("Are you sure you want to remove {0} {1}?",context.getName(),context.getLastname()),
					Language.string("Romove person?"), 
					JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if(ch==JOptionPane.YES_OPTION){
				//TODO: remove the entry
				parent.refresh();
			}
		}
	}

}
