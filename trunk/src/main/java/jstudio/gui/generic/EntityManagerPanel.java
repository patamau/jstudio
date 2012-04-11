package jstudio.gui.generic;

import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import jstudio.control.Controller;
import jstudio.db.DatabaseObject;
import jstudio.util.Language;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public abstract class EntityManagerPanel<T extends DatabaseObject> 
		extends JPanel
		implements ActionListener, MouseListener, KeyListener, ListSelectionListener {
	
	private static final Logger logger = LoggerFactory.getLogger(EntityManagerPanel.class);
	
	protected DefaultTableModel model;
	protected JTable table;
	protected ContextualMenu<T> popup;
	protected Controller<T> controller;
	
	private FilterFieldFocusListener filterFieldFocusListener;
	protected JButton newButton, viewButton, editButton, deleteButton, refreshButton;
	
	//filterig stuff
	protected JTextField filterField;
	private FilterThread filterThread;
	
	private class FilterThread extends Thread{
		boolean stop = false;
		public void run(){
			try {
				String lastFilter = "";
				while(!stop){					
					synchronized(this){
						wait();
						if(stop) break;
						wait(100);
					}		
					//logger.debug("going to filter! "+filterField.getText());
					final String search = filterField.getText();
					if(!search.equals(lastFilter)){
						lastFilter = search;
						try {
							SwingUtilities.invokeAndWait(new Runnable(){
								public void run(){
									filter(search);
								}
							});
						} catch (InvocationTargetException e) {
							e.printStackTrace();
							logger.error(e.getMessage());
						}
					}
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
	
	public boolean isFilterValid(){
		return filterFieldFocusListener.isValid();
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
		
		table = new JTable();
		table.getSelectionModel().addListSelectionListener(this);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		newButton = new JButton(Language.string("New"));
		newButton.addActionListener(this);
		newButton.setMnemonic(KeyEvent.VK_N);
		viewButton = new JButton(Language.string("View"));
		viewButton.addActionListener(this);
		viewButton.setMnemonic(KeyEvent.VK_V);
		viewButton.setEnabled(false);
		editButton = new JButton(Language.string("Edit"));
		editButton.addActionListener(this);
		editButton.setMnemonic(KeyEvent.VK_E);
		editButton.setEnabled(false);
		deleteButton = new JButton(Language.string("Delete"));
		deleteButton.addActionListener(this);
		deleteButton.setMnemonic(KeyEvent.VK_D);
		deleteButton.setEnabled(false);
		filterField = new JTextField();
		filterField.addKeyListener(this);
		filterField.setMaximumSize(new Dimension(250,25));
		filterField.setPreferredSize(new Dimension(250,25));
		filterFieldFocusListener = new FilterFieldFocusListener(filterField);
		filterField.addFocusListener(filterFieldFocusListener);
		refreshButton = new JButton(Language.string("Refresh"));
		refreshButton.addActionListener(this);
		refreshButton.setMnemonic(KeyEvent.VK_R);
	}
	
	public synchronized void filter(String text){
		text = text.trim();
		if(text.length()==0){
			this.refresh();
			return;
		}
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
	
	public void valueChanged(ListSelectionEvent e) {
        //Ignore extra messages.
        if (e.getValueIsAdjusting()) return;

        ListSelectionModel lsm = (ListSelectionModel)e.getSource();
        if (lsm.isSelectionEmpty()) {
            viewButton.setEnabled(false);
            editButton.setEnabled(false);
            deleteButton.setEnabled(false);
        } else {
        	viewButton.setEnabled(true);
            editButton.setEnabled(true);
            deleteButton.setEnabled(true);
        }
    }
}
