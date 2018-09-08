package jstudio.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.SortOrder;
import javax.swing.SwingUtilities;
import javax.swing.RowSorter.SortKey;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterListener;

import jstudio.control.Controller;
import jstudio.gui.generic.EntityManagerPanel;
import jstudio.gui.generic.PopupListener;
import jstudio.model.Invoice;
import jstudio.model.Product;
import jstudio.report.ReportGenerator;
import jstudio.report.ReportGeneratorGUI;
import jstudio.util.CustomRowSorter;
import jstudio.util.Language;
import jstudio.util.Resources;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public class AccountingPanel extends EntityManagerPanel<Invoice> implements RowSorterListener {
	
	private static final Logger logger = LoggerFactory.getLogger(AccountingPanel.class);
	
	public static final String
		LABEL_ACCOUNTING="Accounting",
		PIC_ACCOUNTING="invoiceicon.png";
	
	private JButton printButton;

	public AccountingPanel(Controller<Invoice> controller){
		super(controller);
		this.setLayout(new BorderLayout());
		
		model = new AccountingTableModel(table);
		CustomRowSorter rowsorter = new CustomRowSorter(model);
		table.setRowSorter(rowsorter);
		table.getRowSorter().addRowSorterListener(this);
		table.getRowSorter().toggleSortOrder(0);
		
		JScrollPane scrollpane = new JScrollPane(table);
		this.add(scrollpane, BorderLayout.CENTER);

		JToolBar actionPanel = new JToolBar(Language.string("Actions"));
		actionPanel.setFloatable(false);
		actionPanel.add(newButton);
		actionPanel.addSeparator();
		actionPanel.add(viewButton);
		actionPanel.add(editButton);
		actionPanel.add(deleteButton);
		printButton = new JButton(Language.string("Print"));
		printButton.setEnabled(false);
		printButton.addActionListener(this);
		actionPanel.add(printButton);
		actionPanel.add(Box.createHorizontalGlue());
		actionPanel.add(filterField);
		actionPanel.setPreferredSize(new Dimension(0,25));
		actionPanel.add(refreshButton);
		
		this.add(actionPanel, BorderLayout.NORTH);
		
		this.popup = new InvoicePopup(this);
		scrollpane.addMouseListener(this);
		table.addMouseListener(this);
		table.addMouseListener(new PopupListener<Invoice>(this, popup));
	}
	
	public String getLabel(){
		return Language.string(LABEL_ACCOUNTING);
	}
	
	public ImageIcon getIcon(){
		return Resources.getImage(PIC_ACCOUNTING);
	}
	
	public void showEntity(Invoice i, boolean edit){
		JDialog dialog = new InvoicePanel(i, this, edit).createDialog(this.getTopLevelAncestor());
		dialog.setVisible(true);
	}
	
	public synchronized void clear(){
		while(model.getRowCount()>0) model.removeRow(0);
	}
	
	private String getColumnDatavalue(int col) {
		switch(col) {
			case 0:
				return "id";
			case 1:
				return "date";
			case 2:
				return "lastname";
			case 3:
				return null;
			case 4:
				return null;
			default:
				return null;
		}
	}

	public synchronized void addEntity(Invoice i){
		StringBuffer sb = new StringBuffer();
		int c = i.getProducts().size();
		for(Product t: i.getProducts()){
			sb.append(t.getDescription());
			//total += t.getCost()*t.getQuantity();
			--c;
			if(c>0){
				sb.append(", ");
			}
		}
		model.addRow(new Object[]{
				i,
				i.getDate(),
				i.getLastname()+" "+i.getName(),
				sb.toString(),
				i.getTotal()
		});
	}
	
	public synchronized void removeInvoice(int id){
		Invoice ob;
		int frow = -1;
		for(int i=0; i<model.getRowCount(); i++){
			ob = (Invoice)model.getValueAt(i, 0);
			if(ob.getId()==id){
				frow = i;
				break;
			}
		}
		if(frow>-1){
			model.removeRow(frow);
		}
	}

	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		if(o==refreshButton){
			refresh();
		}else if(o==newButton){
			showEntity(new Invoice(), true);
			//this.refresh();
		}else if(o==editButton){
			int row = table.convertRowIndexToModel(table.getSelectedRow());
			if(row>=0){
				Invoice stubInvoice = (Invoice)model.getValueAt(row, 0);
				Invoice actualInvoice = controller.get(stubInvoice.getId().intValue());
				showEntity(actualInvoice, false);
				this.refresh();
			}
		}else if(o==viewButton){
			int row = table.convertRowIndexToModel(table.getSelectedRow());
			if(row>=0){
				Invoice stubInvoice = (Invoice)model.getValueAt(row, 0);
				Invoice actualInvoice = controller.get(stubInvoice.getId().intValue());
				showEntity(actualInvoice, false);
				this.refresh();
			}
		}else if(o==deleteButton){
			int row = table.convertRowIndexToModel(table.getSelectedRow());
			if(row>=0){
				Invoice context = (Invoice)model.getValueAt(row, 0);
				int ch = JOptionPane.showConfirmDialog(this, 
						Language.string("Are you sure you want to remove invoice {0} {1}?",context.getId(),Invoice.dateFormat.format(context.getDate())),
						Language.string("Remove invoice?"), 
						JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
				if(ch==JOptionPane.YES_OPTION){
					controller.delete(context);
					this.refresh();
				}
			}
		}else if(o==printButton){
			int row = table.convertRowIndexToModel(table.getSelectedRow());
			if(row<0) return;
			Invoice stubInvoice = (Invoice)model.getValueAt(row, 0);
			Invoice context = controller.get(stubInvoice.getId().intValue());
			ReportGenerator rg = new ReportGenerator();
			context.setReport(rg);
			ReportGeneratorGUI rgui = new ReportGeneratorGUI(rg,"invoice_"+context.getFilePrefix());
			rgui.showGUI((Window)SwingUtilities.getRoot(this));		
		}else{
			logger.warn("Event source not mapped: "+o);
		}
	}
	
	public void valueChanged(ListSelectionEvent e) {
        //Ignore extra messages.
        if (e.getValueIsAdjusting()) return;		
		super.valueChanged(e);

        ListSelectionModel lsm = (ListSelectionModel)e.getSource();
        if (lsm.isSelectionEmpty()) {
            printButton.setEnabled(false);
        } else {
        	printButton.setEnabled(true);
        }
    }
	
	private void updateData() {
		List<? extends SortKey> keys = table.getRowSorter().getSortKeys();
		List<SortKey> newKeys = new ArrayList<SortKey>();
		Map<String, String> order = new HashMap<String, String>();
		for(SortKey s : keys) {
			String col = getColumnDatavalue(s.getColumn());
			if(null != col) {
				SortOrder so = s.getSortOrder();
				if(so == SortOrder.UNSORTED) continue;
				order.put(col, so==SortOrder.ASCENDING?"ASC":"DESC");
				newKeys.add(s);
				break;
			}
		}
		table.getRowSorter().setSortKeys(newKeys);
		Collection<Invoice> ts;
		String text = filterField.getText().trim();
		//filter by filter field status (gray is disabled)
		if (text.length() > 0 && filterField.getForeground()!=Color.GRAY) {
			String[] vals = text.split(" ");
			String[] cols = new String[] { "name", "lastname", "date" };
			ts = controller.findAll(vals, cols, null, order);
		} else {
			ts = controller.getAll(null, order);
		}
		clear();
		if(ts!=null){
			for(Invoice t: ts){
				this.addEntity(t);
			}
		}else{
			JOptionPane.showMessageDialog(this, Language.string("Unable to load data"),Language.string("Database error"),JOptionPane.ERROR_MESSAGE);
		}
	}
	

	/**
	 * This method is triggered when the sorting order of the table is changed
	 * and is used to correctly fetch the data based on the filter
	 * @param e
	 */
	@Override
	public synchronized void sorterChanged(RowSorterEvent e) {
		if(e.getType() != RowSorterEvent.Type.SORT_ORDER_CHANGED) return;
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				updateData();
			}
		});
	}
	
	@Override
	public void refresh() {
		updateData();
	}
	
	@Override
	public synchronized void filter(String text){
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				updateData();
			}
		});
	}
}
