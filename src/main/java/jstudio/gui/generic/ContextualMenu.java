package jstudio.gui.generic;

import java.awt.Font;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;

import jstudio.control.Controller;
import jstudio.db.DatabaseObject;
import jstudio.util.Language;

@SuppressWarnings("serial")
public abstract class ContextualMenu<Context extends DatabaseObject> 
		extends JPopupMenu 
		implements ActionListener {
	
	protected JMenuItem
		newItem, viewItem, editItem, deleteItem;
	protected EntityManagerPanel<Context> parent;
	protected Controller<Context> controller;
	protected Context context;

	public ContextualMenu(EntityManagerPanel<Context> parent){
		this.parent=parent;
		this.controller=parent.getController();
		viewItem = new JMenuItem(Language.string("View"));
	    viewItem.addActionListener(this);
	    this.add(viewItem);
	    editItem = new JMenuItem(Language.string("Edit"));
	    editItem.setFont(editItem.getFont().deriveFont(Font.PLAIN));
	    editItem.addActionListener(this);
	    this.add(editItem);
	    deleteItem = new JMenuItem(Language.string("Delete"));
	    deleteItem.setFont(deleteItem.getFont().deriveFont(Font.PLAIN));
	    deleteItem.addActionListener(this);
	    this.add(deleteItem);	    
	    this.add(new JSeparator());
	    newItem = new JMenuItem(Language.string("New..."));
	    newItem.setFont(newItem.getFont().deriveFont(Font.PLAIN));
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
