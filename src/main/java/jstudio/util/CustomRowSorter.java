package jstudio.util;

import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

public class CustomRowSorter extends TableRowSorter<TableModel> {
	
	public CustomRowSorter(TableModel model) 
	{
		super(model);
	}
	
	public void sort() {
		
	}

}
