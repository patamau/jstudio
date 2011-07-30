package jstudio.gui;

import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import jstudio.model.Invoice;
import jstudio.model.Product;
import jstudio.util.Language;

@SuppressWarnings("serial")
public class TreatmentTableModel extends DefaultTableModel {
	
	private static final Object[] cols = new Object[]{
			Language.string("Description"), 
			Language.string("Quantity"), 
			Language.string("Cost")
			};
	
	public TreatmentTableModel(JTable table, Invoice invoice) {
		super(cols, 0);
		table.setModel(this);
		//FIXME: translation!
		table.getColumn("Quantity").setMaxWidth(50);
		table.getColumn("Quantity").setMinWidth(50);
		table.getColumn("Cost").setMaxWidth(50);
		table.getColumn("Cost").setMinWidth(50);
		table.getColumn("Description").setPreferredWidth(150);
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
