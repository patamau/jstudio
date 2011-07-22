package jstudio.gui;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import jstudio.util.Language;
import jstudio.util.TableSorter;

public class EventsTableModel extends DefaultTableModel {
	
	private static final Object[] cols = new Object[]{
			Language.string("Time"), 
			Language.string("Contact"), 
			Language.string("Description")
			};
	
	public EventsTableModel(JTable table) {
		super(cols, 0);
		TableSorter ts = new TableSorter(this,table.getTableHeader());
		table.setModel(ts);
		ts.setSortingStatus(0, TableSorter.ASCENDING);
		table.getColumn("Time").setMaxWidth(50);
		table.getColumn("Time").setMinWidth(50);
	}

	public boolean isCellEditable(int row, int col){
		return false;
	}
	
	public Class<?> getColumnClass(int col){
		switch(col){
		default: return String.class;
		}
	}
	
	
}
