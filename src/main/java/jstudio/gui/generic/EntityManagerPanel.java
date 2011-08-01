package jstudio.gui.generic;

import java.awt.event.ActionListener;

import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import jstudio.db.DatabaseObject;
import jstudio.gui.JStudioGUI;

@SuppressWarnings("serial")
public abstract class EntityManagerPanel<T extends DatabaseObject> 
		extends JPanel
		implements ListSelectionListener, ActionListener {
	
	protected DefaultTableModel model;
	protected JTable table;
	protected JStudioGUI gui;
	
	public EntityManagerPanel(JStudioGUI gui){
		this.gui=gui;
	}
	
	public JStudioGUI getGui(){
		return gui;
	}
	
	public synchronized void clear(){
		while(model.getRowCount()>0){
			model.removeRow(0);
		}
	}
	
	@SuppressWarnings("unchecked")
	public synchronized void removeEntity(int id){
		T t;
		int frow = -1;
		for(int i=0; i<model.getRowCount(); i++){
			t = (T)model.getValueAt(i, 0);
			if(t.getId()==id){
				frow = i;
				break;
			}
		}
		if(frow>-1){
			model.removeRow(frow);
		}
	}
	
	public abstract void refresh();
	
	public abstract void addEntity(T entity);
	
	public abstract void showEntity(T entity);
}
