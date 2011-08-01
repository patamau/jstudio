package jstudio.gui;

import java.util.Date;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import jstudio.util.Language;
import jstudio.util.TableSorter;

@SuppressWarnings("serial")
public class AddressBookTableModel extends DefaultTableModel {
	
	private static final Object[] cols = new Object[]{
		 	Language.string("Lastname"), 
			Language.string("Name"),
			Language.string("Birthdate"), 
			Language.string("City"), 
			Language.string("Phone")};
	
	public AddressBookTableModel(JTable table) {
		super(cols, 0);
		TableSorter ts = new TableSorter(this,table.getTableHeader());
		table.setModel(ts);
		ts.setSortingStatus(0, TableSorter.DESCENDING);
	}

	public boolean isCellEditable(int row, int col){
		return false;
	}
	
	public Class<?> getColumnClass(int col){
		switch(col){
		case 2: return Date.class;
		default: return String.class;
		}
	}
	
	
}
