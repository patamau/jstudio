package jstudio.gui;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import jstudio.model.Invoice;

@SuppressWarnings("serial")
public class ProductTableModel extends DefaultTableModel {
	
	private static final Object[] cols = new Object[]{"","",""};
	/*
			Language.string("Description"), 
			Language.string("Quantity"), 
			Language.string("Cost")
			};
	*/
	
	public ProductTableModel(JTable table, Invoice invoice) {
		super(cols, 0);
		table.setModel(this);
		table.getColumnModel().getColumn(1).setMaxWidth(50);
		table.getColumnModel().getColumn(1).setMinWidth(50);
		table.getColumnModel().getColumn(2).setMaxWidth(100);
		table.getColumnModel().getColumn(2).setPreferredWidth(100);
		table.getColumnModel().getColumn(2).setMinWidth(50);
		table.getColumnModel().getColumn(0).setPreferredWidth(150);
	}
	
	public boolean isCellEditable(int row, int col){
		return false;
	}
	
	public Class<?> getColumnClass(int col){
		switch(col){
		case 1: return Integer.class;
		case 2: return Float.class;
		default: return String.class;
		}
	}
	
	
}
