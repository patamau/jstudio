package jstudio.util;

import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

public abstract class ContextualMenu<Context> 
		extends JPopupMenu 
		implements ActionListener {
	
	protected JMenuItem
		viewItem, editItem, removeItem;
	protected JFrame
		parent;

	public ContextualMenu(JFrame parent){
		this.parent=parent;
		viewItem = new JMenuItem("View");
	    viewItem.addActionListener(this);
	    this.add(viewItem);
	    editItem = new JMenuItem("Edit");
	    editItem.addActionListener(this);
	    this.add(editItem);
	    removeItem = new JMenuItem("Remove");
	    removeItem.addActionListener(this);
	    this.add(removeItem);
	}
	
	public abstract void setContext(Context context);
}
