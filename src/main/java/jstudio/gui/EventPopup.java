package jstudio.gui;

import java.awt.event.ActionEvent;
import java.util.Date;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import jstudio.control.Controller;
import jstudio.gui.generic.ContextualMenu;
import jstudio.gui.generic.EntityManagerPanel;
import jstudio.model.Event;
import jstudio.util.Language;

@SuppressWarnings("serial")
public class EventPopup extends ContextualMenu<Event> {
	
	public EventPopup(EntityManagerPanel<Event> parent, Controller<Event> controller){
		super(parent, controller);
	}

	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		if(o==viewItem){
			JDialog dialog = new EventPanel(context, null).createDialog(parent.getGui());
			dialog.setVisible(true);
		}else if(o==editItem){
			JDialog dialog = new EventPanel(context, controller).createDialog(parent.getGui());
			dialog.setVisible(true);
			parent.refresh();
		}else if(o==removeItem){
			int ch = JOptionPane.showConfirmDialog(parent, 
					Language.string("Are you sure you want to remove the event {0}?",context.getDescription()),
					Language.string("Romove event?"), 
					JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if(ch==JOptionPane.YES_OPTION){
				controller.delete(context);
				parent.refresh();
			}
		}else if(o==newItem){
			//override default event date using the current agendapanel date
			Date d = ((AgendaPanel)parent).getDate();
			Event ev = new Event();
			ev.setDate(d);
			JDialog dialog = new EventPanel(ev, controller).createDialog(parent.getGui());
			dialog.setVisible(true);
			parent.refresh();
		}
	}

}
