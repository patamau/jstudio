package jstudio.gui;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import jstudio.model.Event;
import jstudio.util.Language;

@SuppressWarnings("serial")
public class AgendaTableModel extends DefaultTableModel {
	
	private static final Object[] cols = new Object[]{
			Language.string("Time"), 
			Language.string("Contact"),
			Language.string("Description"),
			Language.string("Phone"),
			};
	
	public AgendaTableModel(JTable table) {
		super(cols, 0);
		/*
		TableSorter ts = new TableSorter(this,table.getTableHeader());
		table.setModel(ts);
		ts.setSortingStatus(0, TableSorter.ASCENDING);
		*/
		table.setModel(this);
		table.setAutoCreateRowSorter(true);
		table.getRowSorter().toggleSortOrder(0);
		table.getColumn(cols[0]).setMaxWidth(50);
		table.getColumn(cols[0]).setMinWidth(50);
	}

	public boolean isCellEditable(int row, int col){
		return false;
	}
	
	public Class<?> getColumnClass(int col){
		switch(col){
		case 0: return Event.class;
		default: return String.class;
		}
	}
	
	
}
