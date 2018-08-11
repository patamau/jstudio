package jstudio.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;
import javax.swing.SwingUtilities;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterListener;

import jstudio.control.Controller;
import jstudio.gui.generic.EntityManagerPanel;
import jstudio.gui.generic.NicePanel;
import jstudio.model.Person;
import jstudio.util.Language;
import jstudio.util.Resources;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public class PersonSelectionPanel extends EntityManagerPanel<Person> implements RowSorterListener {

	private static final Logger logger = LoggerFactory.getLogger(PersonSelectionPanel.class);

	public static final String PIC_ADDRESSBOOK="personicon.png";

	private Person selectedPerson;
	private JButton refreshButton, okButton, cancelButton;
	
	public PersonSelectionPanel(Controller<Person> controller){
		super(controller);
		NicePanel panel = new NicePanel(Language.string("Address Book"));
		panel.getBody().setLayout(new BorderLayout());
		this.setLayout(new BorderLayout());
		this.add(panel, BorderLayout.CENTER);
		
		table = new JTable(){
			public Dimension getPreferredScrollableViewportSize() {
				return getPreferredSize();
			}
		};
		model = new PersonSelectionTableModel(table);
		table.getRowSorter().addRowSorterListener(this);

		JScrollPane scrollpane = new JScrollPane(table);
		//scrollpane.setPreferredSize(new Dimension(this.getWidth(),this.getHeight()));
		panel.getBody().add(scrollpane, BorderLayout.CENTER);

		JToolBar actionPanel = new JToolBar(Language.string("Actions"));
		actionPanel.setFloatable(false);
		refreshButton = new JButton(Language.string("Refresh"));
		refreshButton.addActionListener(this);
		refreshButton.setPreferredSize(new Dimension(60,25));
		actionPanel.add(refreshButton);
		filterField = new JTextField(){
			public void addNotify(){
				super.addNotify();
				this.requestFocusInWindow();
			}
		};
		filterField.addKeyListener(this);
		filterField.setPreferredSize(new Dimension(0,25));
		actionPanel.add(filterField);
		actionPanel.setPreferredSize(new Dimension(0,25));
		panel.getBody().add(actionPanel, BorderLayout.NORTH);
		
		//scrollpane.addMouseListener(this);
		table.addMouseListener(this);
		/*
		this.popup = new PersonPopup(this, controller);
	    table.addMouseListener(new PopupListener<Person>(table, super.popup));
	    */
		
		okButton = new JButton(Language.string("Ok"));
		okButton.addActionListener(this);
		cancelButton = new JButton(Language.string("Cancel"));
		cancelButton.addActionListener(this);
		
		panel.addButtonsGlue();
		panel.addButton(okButton);
		panel.addButton(cancelButton);
	}
	
	public Person getSelected(){
		return selectedPerson;
	}
	
	public String getLabel(){
		return Language.string("Address book");
	}
	
	public ImageIcon getIcon(){
		return Resources.getImage(PIC_ADDRESSBOOK);
	}
	
	public void showEntity(Person p, boolean edit){
		JDialog dialog = new PersonPanel(p,this, edit).createDialog((Frame)this.getTopLevelAncestor());
		dialog.setVisible(true);
	}
	
	private String getColumnDatavalue(int col) {
		switch(col) {
			case 0:
				return "lastname";
			case 1:
				return "name";
			case 2:
				return "birthdate";
			case 3:
				return "city";
			default:
				return null;
		}
	}

	public synchronized void addEntity(Person p){
		model.addRow(new Object[]{
				p,
				p.getName(),
				p.getBirthdate(),
				p.getCity()});
	}
	
	public synchronized void filter(String text) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				updateData();
			}
		});
	}

	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		if(o==refreshButton){
			refresh();
		} else if(o==okButton){
			if(selectedPerson!=null){
				Window w = (Window)SwingUtilities.getRoot(this);
				w.dispose();
			}
		} else if(o==cancelButton){
			selectedPerson=null;
			Window w = (Window)SwingUtilities.getRoot(this);
			w.dispose();
		} else {
			logger.warn("Event source not mapped: "+o);
		}
	}
	
	public void showDialog(Component parent){
		this.showDialog(parent, null);
	}
	
	public void showDialog(final Component parent, final String filterText){
		JDialog dialog = new JDialog();
		dialog.setTitle(Language.string("Select Person"));
		dialog.setModal(true);
		dialog.add(this);
		this.filterField.setText(filterText);
		this.filterField.setSelectionEnd(filterText.length());
		this.filterField.setSelectionStart(0);
		this.filter(filterText);
		//dialog.pack();
		dialog.setSize(500, 300);
		dialog.setLocationRelativeTo(parent);
		dialog.setVisible(true);
		logger.debug("Finalizing selection panel");
		this.finalize();
		dialog.dispose();
	}
	
	public void mouseClicked(MouseEvent e){ 
		int row = table.rowAtPoint(e.getPoint());
		if(row>=0){
			row = table.convertRowIndexToModel(row);
			selectedPerson = (Person)model.getValueAt(row, 0);
			if(e.getClickCount()==2){
				Window w = (Window)SwingUtilities.getRoot(this);
				w.dispose();
			}
		}
	}
	
	/**
	 * Intentionally empty to avoid popup to show
	 */
	public void mouseReleased(final MouseEvent e){ }
	
	private void updateData() {
		List<? extends SortKey> keys = table.getRowSorter().getSortKeys();
		List<SortKey> newKeys = new ArrayList<SortKey>();
		Map<String, String> order = new HashMap<String, String>();
		for(SortKey s : keys) {
			String col = getColumnDatavalue(s.getColumn());
			if(null != col) {
				SortOrder so = s.getSortOrder();
				if(so == SortOrder.UNSORTED) continue;
				order.put(col, so==SortOrder.ASCENDING?"ASC":"DESC");
				newKeys.add(s);
				break;
			}
		}
		table.getRowSorter().setSortKeys(newKeys);
		Collection<Person> ts;
		String text = filterField.getText().trim();
		if (text.length() > 0) {
			String[] vals = text.split(" ");
			String[] cols = new String[] { "name", "lastname" };
			ts = controller.findAll(vals, cols, null, order);
		} else {
			ts = controller.getAll(null, order);
		}
		clear();
		if(ts!=null){
			for(Person t: ts){
				logger.debug("person "+t.getLastname()+" "+t.getName());
				this.addEntity(t);
			}
		}else{
			JOptionPane.showMessageDialog(this, Language.string("Unable to load data"),Language.string("Database error"),JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * This method is triggered when the sorting order of the table is changed
	 * and is used to correctly fetch the data based on the filter
	 * @param e
	 */
	@Override
	public synchronized void sorterChanged(RowSorterEvent e) {
		if(e.getType() != RowSorterEvent.Type.SORT_ORDER_CHANGED) return;
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				updateData();
			}
		});
	}
	
	@Override
	public void refresh() {
		updateData();
	}
}
