package jstudio.util;

import java.util.ArrayList;
import java.util.List;

import javax.swing.SortOrder;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

public class CustomRowSorter extends TableRowSorter<TableModel> {
	
	public CustomRowSorter(TableModel model) {
		super(model);
		this.setMaxSortKeys(1);
	}
	
	/**
	 * Facilitate specific sorting for a column
	 * @param column
	 * @param order
	 */
	/*public void setColumnSorting(int column, SortOrder order) {
		SortKey sk = new SortKey(column, order);
		List<SortKey> keys = new ArrayList<SortKey>();
		keys.add(sk);
		this.setSortKeys(keys);
		this.fireSortOrderChanged();
	}*/
	
	public void sort() {
		//intentionally empty!
	}

}
