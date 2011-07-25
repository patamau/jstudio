package jstudio.gui;

import java.awt.event.ActionEvent;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import jstudio.model.Event;
import jstudio.util.ContextualMenu;
import jstudio.util.Language;

@SuppressWarnings("serial")
public class EventPopup extends ContextualMenu<Event> {

	private Event event;
	
	public EventPopup(JFrame parent){
		super(parent);
	}
	
	@Override
	public void setContext(Event context) {
		event = context;
	}

	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		if(o==viewItem){
			JDialog dialog = EventPanel.createDialog(parent, event, false);
			dialog.setVisible(true);
		}else if(o==editItem){
			JDialog dialog = EventPanel.createDialog(parent, event, true);
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
