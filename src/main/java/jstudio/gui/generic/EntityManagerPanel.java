package jstudio.gui.generic;

import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Collection;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import jstudio.control.Controller;
import jstudio.db.DatabaseObject;
import jstudio.util.Language;

@SuppressWarnings("serial")
public abstract class EntityManagerPanel<T extends DatabaseObject> 
		extends JPanel
		implements ActionListener, MouseListener {
	
	protected DefaultTableModel model;
	protected JTable table;
	protected ContextualMenu<T> popup;
	protected Controller<T> controller;
	
	public EntityManagerPanel(Controller<T> controller){
		this.controller=controller;
	}
	
	public abstract String getLabel();
	
	public ImageIcon getIcon(){
		return null;
	}
	
	public Controller<T> getController(){
		return controller;
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
	
	public void refresh(){
		this.clear();
		Collection<T> ts = controller.getAll();
		if(ts!=null){
			for(T t: ts){
				this.addEntity(t);
			}
		}else{
			JOptionPane.showMessageDialog(this, Language.string("Unable to load data"),Language.string("Database error"),JOptionPane.ERROR_MESSAGE);
		}
	}
	
	public abstract void addEntity(T entity);
	
	public abstract void showEntity(T entity);
	
	@SuppressWarnings("unchecked")
	public void mouseClicked(MouseEvent e){ 
		if(e.getClickCount()==2){
			int row = table.rowAtPoint(e.getPoint());
			if(row>=0){
				showEntity((T)table.getValueAt(row, 0));
			}
		}
	}

	public void mouseEntered(MouseEvent e){ }
	public void mouseExited(MouseEvent e){ }
	public void mousePressed(MouseEvent e){ }
	
	public void mouseReleased(final MouseEvent e){ 
		if(SwingUtilities.isRightMouseButton(e)){
			int row = table.rowAtPoint(e.getPoint());
			if(row<0){
				if(popup!=null){
					popup.setContext(null);
					SwingUtilities.invokeLater(new Runnable(){
						public void run(){
							popup.show(e.getComponent(), e.getX(), e.getY());	
						}
					});
				}
			}
		}
	}
}
