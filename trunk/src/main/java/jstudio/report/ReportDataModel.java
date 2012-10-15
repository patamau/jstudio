package jstudio.report;

import javax.swing.table.DefaultTableModel;

public class ReportDataModel extends DefaultTableModel {
	
	private static final long serialVersionUID = 8901237980079599878L;
	
	public ReportDataModel(final String[] headers, final int siz){
		super(headers, siz);
	}

	public boolean isCellEditable(final int row, final int column){
		return column==1;
	}
}
