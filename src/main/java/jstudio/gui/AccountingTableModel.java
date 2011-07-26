package jstudio.gui;

import java.util.Date;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import jstudio.model.Invoice;
import jstudio.util.Language;
import jstudio.util.TableSorter;

@SuppressWarnings("serial")
public class AccountingTableModel extends DefaultTableModel {
	
	private static final Object[] cols = new Object[]{
			Language.string("Id"),
			Language.string("Date"), 
			Language.string("Contact"), 
			Language.string("Description"),
			Language.string("Total"),
			};
	
	public AccountingTableModel(JTable table) {
		super(cols, 0);
		TableSorter ts = new TableSorter(this,table.getTableHeader());
		table.setModel(ts);
		ts.setSortingStatus(0, TableSorter.ASCENDING);
		table.getColumn("Id").setMaxWidth(50);
		table.getColumn("Id").setMinWidth(50);
	}

	public boolean isCellEditable(int row, int col){
		return false;
	}
	
	public Class<?> getColumnClass(int col){
		switch(col){
		case 0: return Invoice.class;
		case 1: return Date.class;
		default: return String.class;
		}
	}
	
	
}
