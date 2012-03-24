package jstudio.gui.generic;

import java.awt.Font;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;

import jstudio.control.Controller;
import jstudio.db.DatabaseObject;

@SuppressWarnings("serial")
public abstract class ContextualMenu<Context extends DatabaseObject> 
		extends JPopupMenu 
		implements ActionListener {
	
	protected JMenuItem
		viewItem, editItem, deleteItem, newItem;
	protected EntityManagerPanel<Context> parent;
	protected Controller<Context> controller;
	protected Context context;

	public ContextualMenu(EntityManagerPanel<Context> parent){
		this.parent=parent;
		this.controller=parent.getController();
		viewItem = new JMenuItem("View");
	    viewItem.addActionListener(this);
	    this.add(viewItem);
	    editItem = new JMenuItem("Edit");
	    editItem.setFont(editItem.getFont().deriveFont(Font.PLAIN));
	    editItem.addActionListener(this);
	    this.add(editItem);
	    deleteItem = new JMenuItem("Delete");
	    deleteItem.setFont(deleteItem.getFont().deriveFont(Font.PLAIN));
	    deleteItem.addActionListener(this);
	    this.add(deleteItem);	    
	    this.add(new JSeparator());
	    newItem = new JMenuItem("New");
	    newItem.setFont(deleteItem.getFont().deriveFont(Font.PLAIN));
	    newItem.addActionListener(this);
	    this.add(newItem);
	}
	
	public void setContext(Context context){
		this.context = context;
		boolean enable = context!=null;
		viewItem.setEnabled(enable);
		editItem.setEnabled(enable);
		deleteItem.setEnabled(enable);
	}
}
