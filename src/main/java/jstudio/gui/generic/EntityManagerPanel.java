package jstudio.gui.generic;

import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Collection;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jstudio.control.Controller;
import jstudio.db.DatabaseObject;
import jstudio.util.Language;

@SuppressWarnings("serial")
public abstract class EntityManagerPanel<T extends DatabaseObject> 
		extends JPanel
		implements ActionListener, MouseListener, KeyListener {
	
	private static final Logger logger = LoggerFactory.getLogger(EntityManagerPanel.class);
	
	protected DefaultTableModel model;
	protected JTable table;
	protected ContextualMenu<T> popup;
	protected Controller<T> controller;
	
	//filterig stuff
	protected JTextField filterField;
	private FilterThread filterThread;
	
	private class FilterThread extends Thread{
		boolean stop = false;
		public void run(){
			try {
				while(!stop){					
					synchronized(this){
						wait();
						if(stop) break;
						wait(100);
					}		
					//logger.debug("going to filter! "+filterField.getText());
					filter(filterField.getText());
				}
			} catch (InterruptedException e) {
				logger.error("Filter thread interrupted");
			}
			//logger.debug("FilterThread finished");
		}
	}
	
	public void finalize(){
		if(filterThread!=null){
			filterThread.stop=true;
			synchronized(filterThread){
				filterThread.notify();
			}
		}
	}
	
	public void keyPressed(KeyEvent e){}
	public void keyReleased(KeyEvent e){}
	public void keyTyped(KeyEvent e){
		synchronized(filterThread){
			filterThread.notify();
		}
	}
	
	/*
	 * Default controller constructor
	 * assumes filter is true
	 */
	public EntityManagerPanel(Controller<T> controller){
		this(controller, true);
	}
	
	public EntityManagerPanel(Controller<T> controller, boolean filter){
		this.controller=controller;
		if(filter){
			filterThread = new FilterThread();
			filterThread.start();
		}
	}
	
	public void filter(String text){
		text = text.trim();
		if(text.length()==0) return;
		this.clear();
		String[] vals = text.split(" ");
		String[] cols = new String[]{
				"name",
				"lastname" };
		Collection<T> ts = controller.findAll(vals, cols);
		if(ts!=null){
			for(T t: ts){
				this.addEntity(t);
			}
		}else{
			JOptionPane.showMessageDialog(this, Language.string("Unable to load data"),Language.string("Database error"),JOptionPane.ERROR_MESSAGE);
		}
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
	
	public abstract void showEntity(T entity, boolean edit);
	
	@SuppressWarnings("unchecked")
	public void mouseClicked(MouseEvent e){ 
		if(e.getClickCount()==2){
			int row = table.rowAtPoint(e.getPoint());
			if(row>=0){
				showEntity((T)table.getValueAt(row, 0), false);
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
