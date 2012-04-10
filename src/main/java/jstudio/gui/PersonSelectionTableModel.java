package jstudio.gui;

import java.util.Date;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import jstudio.model.Person;
import jstudio.util.Language;

@SuppressWarnings("serial")
public class PersonSelectionTableModel extends DefaultTableModel {
	
	private static final Object[] cols = new Object[]{
			Language.string("Lastname"), 
			Language.string("Name"), 
			Language.string("Birthdate"),
			Language.string("City"),
			};
	
	public PersonSelectionTableModel(JTable table) {
		super(cols, 0);
		table.setModel(this);
		table.setAutoCreateRowSorter(true);
		table.getRowSorter().toggleSortOrder(0);
		table.getRowSorter().toggleSortOrder(0);
	}

	public boolean isCellEditable(int row, int col){
		return false;
	}
	
	public Class<?> getColumnClass(int col){
		switch(col){
		case 0: return Person.class;
		case 2: return Date.class;
		default: return String.class;
		}
	}
}
