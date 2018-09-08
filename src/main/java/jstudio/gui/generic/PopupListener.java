package jstudio.gui.generic;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JTable;

import jstudio.control.Controller;
import jstudio.db.DatabaseObject;

public class PopupListener<Context extends DatabaseObject> extends MouseAdapter {
	private Controller<Context> controller;
	private JTable table;
	private ContextualMenu<Context> popup;
	
	public PopupListener(EntityManagerPanel<Context> manager, ContextualMenu<Context> popup){
		this.table=manager.table;
		this.controller=manager.controller;
		this.popup=popup;
	}
	
    public void mousePressed(MouseEvent e) {
        maybeShowPopup(e);
    }

    public void mouseReleased(MouseEvent e) {
        maybeShowPopup(e);
    }

    private void maybeShowPopup(MouseEvent e) {
        if (e.isPopupTrigger()) {
        	int row = table.rowAtPoint(e.getPoint());
        	if(row>=0){
	        	table.setRowSelectionInterval(row, row);
	        	int mrow = table.convertRowIndexToModel(row);
	        	@SuppressWarnings("unchecked")
				Context c = (Context)table.getModel().getValueAt(mrow, 0);
	        	Context actualContext = controller.get(c.getId().intValue());
	        	popup.setContext(actualContext);
	            popup.show(e.getComponent(), e.getX(), e.getY());
        	}
        }
    }
}
