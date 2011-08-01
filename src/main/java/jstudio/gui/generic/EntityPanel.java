package jstudio.gui.generic;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JDialog;
import javax.swing.JPanel;

import jstudio.control.Controller;
import jstudio.db.DatabaseObject;
import jstudio.util.Language;

@SuppressWarnings("serial")
public abstract class EntityPanel<T extends DatabaseObject>
		extends JPanel
		implements ActionListener {

	private static Map<Class<? extends DatabaseObject>,JDialog> 
		dialogs = new HashMap<Class<? extends DatabaseObject>,JDialog>();
	protected T entity;
	protected Controller<T> controller;
	
	public EntityPanel(T entity, Controller<T> controller){
		this.entity = entity;
		this.controller = controller;
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
		dialog.setModal(modal);
		dialog.getContentPane().removeAll();
		dialog.getContentPane().add(this,BorderLayout.CENTER);
		dialog.pack();
	}
	
	public JDialog getDialog(){
		return dialogs.get(entity.getClass());
	}
	
	private void setDialog(JDialog dialog){
		dialogs.put(entity.getClass(),dialog);
	}
	
	/**
	 * Sub dialog
	 * @param parent
	 * @return
	 */
	public JDialog createDialog(Dialog parent){
		JDialog dialog = getDialog();
		if(dialog==null){
			dialog = new JDialog(parent);
			setDialog(dialog);
			dialog.setLocationRelativeTo(parent);
			initializeDialog(dialog);
		}
		prepareDialog(dialog);
		return dialog;
	}
	
	public JDialog createDialog(Frame parent){
		JDialog dialog = getDialog();
		if(dialog==null){
			dialog = new JDialog(parent);
			setDialog(dialog);
			dialog.setLocationRelativeTo(parent);
			initializeDialog(dialog);
		}
		prepareDialog(dialog);
		return dialog;
	}
}
