package jstudio.gui;

import java.util.Date;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import jstudio.model.Person;
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
	}

	public boolean isCellEditable(int row, int col){
		return false;
	}
	
	public Class<?> getColumnClass(int col){
		switch(col){
		case 0: return Long.class;
		case 2: return Person.class;
		default: return String.class;
		}
	}
	
	
}
