package jstudio.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.SortOrder;
import javax.swing.SwingUtilities;
import javax.swing.RowSorter.SortKey;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterListener;

import jstudio.control.Controller;
import jstudio.gui.generic.EntityManagerPanel;
import jstudio.gui.generic.PopupListener;
import jstudio.model.Person;
import jstudio.report.ReportChooser;
import jstudio.report.ReportGenerator;
import jstudio.util.CustomRowSorter;
import jstudio.util.Language;
import jstudio.util.Resources;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public class AddressBookPanel extends EntityManagerPanel<Person> implements RowSorterListener  {
	
	private static final Logger logger = LoggerFactory.getLogger(AddressBookPanel.class);

	public static final String PIC_ADDRESSBOOK="personicon.png";
	
	private JButton printButton;
	
	public AddressBookPanel(Controller<Person> controller){
		super(controller);
		this.setLayout(new BorderLayout());
		model = new AddressBookTableModel(table);
		table.setRowSorter(new CustomRowSorter(model));
		table.getRowSorter().addRowSorterListener(this);
		table.getRowSorter().toggleSortOrder(0);

		JScrollPane scrollpane = new JScrollPane(table);
		//scrollpane.setPreferredSize(new Dimension(this.getWidth(),this.getHeight()));
		this.add(scrollpane, BorderLayout.CENTER);

		JToolBar actionPanel = new JToolBar(Language.string("Actions"));
		actionPanel.setFloatable(false);
		actionPanel.add(newButton);
		actionPanel.addSeparator();
		actionPanel.add(viewButton);
		actionPanel.add(editButton);
		actionPanel.add(deleteButton);
		printButton = new JButton(Language.string("Print"));
		printButton.setEnabled(false);
		printButton.addActionListener(this);
		actionPanel.add(printButton);
		actionPanel.add(Box.createHorizontalGlue());
		actionPanel.add(filterField);
		actionPanel.setPreferredSize(new Dimension(0,25));
		actionPanel.add(refreshButton);
		this.add(actionPanel, BorderLayout.NORTH);
		
		scrollpane.addMouseListener(this);
		table.addMouseListener(this);
		this.popup = new PersonPopup(this, controller);
	    table.addMouseListener(new PopupListener<Person>(table, super.popup));
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
			case 4:
				return "phone";
			default:
				return null;
		}
	}

	public synchronized void addEntity(Person p){
		model.addRow(new Object[]{
				p,
				p.getName(),
				p.getBirthdate(),
				p.getCity(),
				p.getPhone()});
	}
	
	@Override
	public synchronized void filter(String text){
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
		} else if(o==newButton){
			showEntity(new Person(), true);
			this.refresh();
		} else if(o==viewButton){
			int row = table.convertRowIndexToModel(table.getSelectedRow());
			if(row>=0){
				showEntity((Person)model.getValueAt(row, 0), false);
				this.refresh();
			}
		} else if(o==editButton){
			int row = table.convertRowIndexToModel(table.getSelectedRow());
			if(row>=0){
				showEntity((Person)model.getValueAt(row, 0), true);
				this.refresh();
			}
		} else if(o==printButton){
			int row = table.convertRowIndexToModel(table.getSelectedRow());
			if(row>=0){
				ReportGenerator rg = new ReportGenerator();
				rg.setHead((Person)model.getValueAt(row, 0));
				ReportChooser rc = new ReportChooser(rg);
				rc.showGUI(this.getController().getApplication().getGUI());
			}
			
		} else if(o==deleteButton){
			int row = table.convertRowIndexToModel(table.getSelectedRow());
			if(row>=0){
				Person context = (Person)model.getValueAt(row, 0);
				int ch = JOptionPane.showConfirmDialog(this, 
						Language.string("Are you sure you want to remove {0} {1}?",context.getName(),context.getLastname()),
						Language.string("Remove person?"), 
						JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
				if(ch==JOptionPane.YES_OPTION){
					controller.delete(context);
					this.refresh();
				}
			}
		} else {
			logger.warn("Event source not mapped: "+o);
		}
	}
	
	public void valueChanged(ListSelectionEvent e) {
        //Ignore extra messages.
        if (e.getValueIsAdjusting()) return;		
		super.valueChanged(e);

        ListSelectionModel lsm = (ListSelectionModel)e.getSource();
        if (lsm.isSelectionEmpty()) {
            printButton.setEnabled(false);
        } else {
        	printButton.setEnabled(true);
        }
    }
	
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
		//filter by filter field status (gray is disabled)
		if (text.length() > 0 && filterField.getForeground()!=Color.GRAY) {
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
