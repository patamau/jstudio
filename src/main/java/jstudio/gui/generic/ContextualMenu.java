package jstudio.gui.generic;

import java.awt.Font;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import jstudio.control.Controller;
import jstudio.db.DatabaseObject;

@SuppressWarnings("serial")
public abstract class ContextualMenu<Context extends DatabaseObject> 
		extends JPopupMenu 
		implements ActionListener {
	
	protected JMenuItem
		viewItem, editItem, removeItem;
	protected EntityManagerPanel<Context> parent;
	protected Controller<Context> controller;
	protected Context context;

	public ContextualMenu(EntityManagerPanel<Context> parent, Controller<Context> controller){
		this.parent=parent;
		this.controller=controller;
		viewItem = new JMenuItem("View");
	    viewItem.addActionListener(this);
	    this.add(viewItem);
	    editItem = new JMenuItem("Edit");
	    editItem.setFont(editItem.getFont().deriveFont(Font.PLAIN));
	    editItem.addActionListener(this);
	    this.add(editItem);
	    removeItem = new JMenuItem("Remove");
	    removeItem.setFont(removeItem.getFont().deriveFont(Font.PLAIN));
	    removeItem.addActionListener(this);
	    this.add(removeItem);
	}
	
	public void setContext(Context context){
		this.context = context;
	}
}
