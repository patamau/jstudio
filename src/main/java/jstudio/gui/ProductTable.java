package jstudio.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

import jstudio.control.Accounting;
import jstudio.gui.generic.EntityManagerPanel;
import jstudio.gui.generic.PopupListener;
import jstudio.model.Invoice;
import jstudio.model.Product;
import jstudio.util.Language;

@SuppressWarnings("serial")
public class ProductTable 
		extends EntityManagerPanel<Product> {
	
	private JTable totals;
	private DefaultTableModel totalsModel;
	private Invoice invoice;
	private EntityManagerPanel<Invoice> accounting;
	
	public ProductTable(Invoice invoice, EntityManagerPanel<Invoice> accounting){
		super(((Accounting)accounting.getController()).getProducts());
		this.invoice = invoice;
		this.accounting = accounting;
		
		this.setLayout(new BorderLayout());

		table = new JTable(){
			public Dimension getPreferredScrollableViewportSize() {
				return getPreferredSize();
			}
		};
		model = new ProductTableModel(table, invoice);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		JScrollPane scrollpane = new JScrollPane(table);
		//scrollpane.setPreferredSize(new Dimension(this.getWidth(),this.getHeight()));
		this.add(scrollpane, BorderLayout.CENTER);
		
		this.popup = new ProductPopup(this, invoice, accounting);
		scrollpane.addMouseListener(this);
		table.addMouseListener(this);
		table.addMouseListener(new PopupListener<Product>(table, popup));
		
		totals = new JTable();
		totalsModel = new ProductTableModel(totals, null);
		totals.setRowSelectionAllowed(false);		
		
		this.add(totals, BorderLayout.SOUTH);
	}
	
	public String getLabel(){
		return Language.string("Products");
	}
	
	public void showEntity(Product p){
		JDialog dialog;
		dialog = new ProductPanel(p, this, invoice, accounting).createDialog(this.getTopLevelAncestor());
		dialog.setVisible(true);
	}

	public synchronized void addEntity(Product p){
		model.addRow(new Object[]{
				p,
				p.getQuantity(),
				p.getCost()
		});
	}

	public void actionPerformed(ActionEvent e) {
		//Object o = e.getSource();
	}

	@Override
	public void refresh() {
		this.clear();
		int quantity_tot = 0;
		float cost_tot = 0f;
		for(Product t: invoice.getProducts()){
			addEntity(t);
			quantity_tot += t.getQuantity();
			cost_tot += t.getQuantity()*t.getCost();
		}
		while(totalsModel.getRowCount()>0) totalsModel.removeRow(0);
		totalsModel.addRow(new Object[]{
				Language.string("Total"),
				quantity_tot,
				cost_tot
		});
	}
}
