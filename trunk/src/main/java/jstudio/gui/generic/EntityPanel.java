package jstudio.gui.generic;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JDialog;
import javax.swing.JPanel;

import jstudio.control.Controller;
import jstudio.db.DatabaseObject;
import jstudio.util.Configuration;
import jstudio.util.Language;

@SuppressWarnings("serial")
public abstract class EntityPanel<T extends DatabaseObject>
		extends JPanel
		implements ActionListener {

	private static Map<Class<? extends DatabaseObject>,JDialog> 
		dialogs = new HashMap<Class<? extends DatabaseObject>,JDialog>();
	protected T entity;
	protected EntityManagerPanel<T> manager;
	protected Controller<T> controller;
	
	public EntityPanel(T entity, EntityManagerPanel<T> manager){
		this.entity = entity;
		this.manager = manager;
		this.controller = manager.getController();
	}
	
	private void initializeDialog(JDialog dialog){
		dialog.setTitle(Language.string(entity.getClass().getSimpleName()+" dialog"));
		dialog.getContentPane().setLayout(new BorderLayout());
	}
	
	private void prepareDialog(JDialog dialog){
		boolean modal = controller!=null;
		if(dialog.isVisible() && modal && !dialog.isModal()){
			dialog.setVisible(false);
			//before rendering a modal dialog, ensure the old non-modal one is disabled
		}
		dialog.setModal(false); //XXX
		dialog.getContentPane().removeAll();
		dialog.getContentPane().add(this,BorderLayout.CENTER);
		dialog.pack();
		int minwidth = Configuration.getGlobal("panel.width.min", 400);
		if(dialog.getWidth()<minwidth){
			dialog.setSize(minwidth, dialog.getHeight());
		}
	}
	
	public JDialog getDialog(){
		return dialogs.get(entity.getClass());
	}
	
	private void setDialog(JDialog dialog){
		dialogs.put(entity.getClass(),dialog);
	}
	
	public JDialog createDialog(Container container){
		JDialog dialog = getDialog();
		if(dialog==null){
			if(container == null){
				throw new NullPointerException("Expected Frame or Dialog, found Null");
			}else if(container instanceof Dialog){
				dialog = new JDialog((Dialog)container);
			}else if(container instanceof Frame){
				dialog = new JDialog((Frame)container);
			}else {
				throw new IllegalArgumentException("Frame or Dialog expected, found "+container.getClass());
			}
			setDialog(dialog);
			initializeDialog(dialog);
		}
		prepareDialog(dialog);
		dialog.setLocationRelativeTo(container);
		return dialog;
	}
}
