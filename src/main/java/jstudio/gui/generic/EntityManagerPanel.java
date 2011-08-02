package jstudio.gui.generic;

import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jstudio.db.DatabaseObject;
import jstudio.gui.JStudioGUI;

@SuppressWarnings("serial")
public abstract class EntityManagerPanel<T extends DatabaseObject> 
		extends JPanel
		implements ActionListener, MouseListener {
	
	private static Logger logger = LoggerFactory.getLogger(EntityManagerPanel.class);
	
	protected DefaultTableModel model;
	protected JTable table;
	protected JStudioGUI gui;
	protected ContextualMenu<T> popup;
	
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
	
	@SuppressWarnings("unchecked")
	public void mouseClicked(MouseEvent e){ 
		int row = table.getSelectedRow();
		if(e.getClickCount()==2){
			if(row>=0){
				showEntity((T)table.getValueAt(row, 0));
			}
		}
	}

	public void mouseEntered(MouseEvent e){ }
	public void mouseExited(MouseEvent e){ }
	public void mousePressed(MouseEvent e){ }
	public void mouseReleased(final MouseEvent e){ 
		logger.warn("Mouse released");
		if(SwingUtilities.isRightMouseButton(e)){
			int row = table.getSelectedRow();
			System.out.println("RMouse clicked at row "+row);
			if(row<0){
				System.out.println("Popup is null? "+(popup==null));
				if(popup!=null){
					popup.setContext(null);
					System.out.println("Popup showing now!");
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
