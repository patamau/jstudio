package jstudio.gui;

import java.awt.event.ActionEvent;
import java.util.Date;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import jstudio.gui.generic.ContextualMenu;
import jstudio.gui.generic.EntityManagerPanel;
import jstudio.model.Event;
import jstudio.util.Language;

@SuppressWarnings("serial")
public class EventPopup extends ContextualMenu<Event> {
	
	public EventPopup(EntityManagerPanel<Event> parent){
		super(parent);
	}

	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		if(o==viewItem){
			JDialog dialog = new EventPanel(context, parent, false).createDialog(parent.getTopLevelAncestor());
			dialog.setVisible(true);
		}else if(o==editItem){
			JDialog dialog = new EventPanel(context, parent, true).createDialog(parent.getTopLevelAncestor());
			dialog.setVisible(true);
		}else if(o==deleteItem){
			int ch = JOptionPane.showConfirmDialog(parent, 
					Language.string("Are you sure you want to remove the event {0} at {1}?",context.getDescription(), Event.timeFormat.format(context.getDate())),
					Language.string("Romove event?"), 
					JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if(ch==JOptionPane.YES_OPTION){
				controller.delete(context);
				parent.refresh();
			}
		}else if(o==newItem){
			//override default event date using the current agendapanel date
			Date d = ((AgendaPanel)parent).getDate();
			Event ev = new Event(0l);
			ev.setDate(d);
			JDialog dialog = new EventPanel(ev, parent, true).createDialog(parent.getTopLevelAncestor());
			dialog.setVisible(true);
		}
	}

}
