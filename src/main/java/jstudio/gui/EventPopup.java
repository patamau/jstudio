package jstudio.gui;

import java.awt.event.ActionEvent;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import jstudio.control.Controller;
import jstudio.gui.generic.ContextualMenu;
import jstudio.gui.generic.EntityManagerPanel;
import jstudio.model.Event;
import jstudio.util.Language;

@SuppressWarnings("serial")
public class EventPopup extends ContextualMenu<Event> {

	private Event event;
	
	public EventPopup(EntityManagerPanel<Event> parent, Controller<Event> controller){
		super(parent, controller);
	}
	
	@Override
	public void setContext(Event context) {
		event = context;
	}

	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		if(o==viewItem){
			JDialog dialog = new EventPanel(event, null).createDialog(parent.getGui());
			dialog.setVisible(true);
		}else if(o==editItem){
			JDialog dialog = new EventPanel(event, controller).createDialog(parent.getGui());
			dialog.setVisible(true);
		}else if(o==removeItem){
			int ch = JOptionPane.showConfirmDialog(parent, 
					Language.string("Are you sure you want to remove the event {0}?",event.getDescription()),
					Language.string("Romove event?"), 
					JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if(ch==JOptionPane.YES_OPTION){
				//TODO: remove the entry
			}
		}
	}

}
