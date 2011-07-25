package jstudio.gui;

import java.util.Date;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import jstudio.model.Person;
import jstudio.util.Language;
import jstudio.util.TableSorter;

@SuppressWarnings("serial")
public class AddressBookTableModel extends DefaultTableModel {
	
	private static final Object[] cols = new Object[]{
			Language.string("Id"),
			Language.string("Name"), 
			Language.string("Lastname"), 
			Language.string("Birthdate"), 
			Language.string("Address"), 
			Language.string("Phone")};
	
	public AddressBookTableModel(JTable table) {
		super(cols, 0);
		TableSorter ts = new TableSorter(this,table.getTableHeader());
		table.setModel(ts);
		ts.setSortingStatus(0, TableSorter.ASCENDING);
		table.getColumn("Id").setMaxWidth(30); //id column is smaller
	}

	public boolean isCellEditable(int row, int col){
		return false;
	}
	
	public Class<?> getColumnClass(int col){
		switch(col){
		case 0: return Person.class;
		case 3: return Date.class;
		default: return String.class;
		}
	}
	
	
}
