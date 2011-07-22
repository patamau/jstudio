package jstudio.gui;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JTable;

import jstudio.util.ContextualMenu;

public class PopupListener<Context> extends MouseAdapter {
	private JTable table;
	private ContextualMenu<Context> popup;
	
	public PopupListener(JTable table, ContextualMenu<Context> popup){
		this.table=table;
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
        	@SuppressWarnings("unchecked")
			Context c = (Context)table.getModel().getValueAt(row, 0);
        	popup.setContext(c);
        	table.setRowSelectionInterval(row, row);
            popup.show(e.getComponent(), e.getX(), e.getY());
        }
    }
}
