package jstudio.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
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
	private JButton newButton, removeButton;
	
	private float total;
	
	public ProductTable(Invoice invoice, EntityManagerPanel<Invoice> accounting, boolean editable){
		super(((Accounting)accounting.getController()).getProducts(), false);
		this.invoice = invoice;
		this.accounting = accounting;
		
		this.setLayout(new BorderLayout());
		
		if(editable){
			JToolBar toolBar = new JToolBar();
			toolBar.setPreferredSize(new Dimension(0,20));
			toolBar.setFloatable(false);
			newButton = new JButton("+");
			newButton.addActionListener(this);
			removeButton = new JButton("-");
			removeButton.addActionListener(this);
			toolBar.add(newButton);
			toolBar.add(removeButton);
			this.add(toolBar, BorderLayout.NORTH);
		}

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
		
		if(editable){
			this.popup = new ProductPopup(this, invoice, accounting);
			scrollpane.addMouseListener(this);
			table.addMouseListener(this);
			table.addMouseListener(new PopupListener<Product>(table, popup));
		}
		
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
	
	public float getTotal(){
		return total;
	}

	@Override
	public void refresh() {
		this.clear();
		int quantity_tot = 0;
		total = 0f;
		for(Product t: invoice.getProducts()){
			addEntity(t);
			quantity_tot += t.getQuantity();
			total += t.getQuantity()*t.getCost();
		}
		while(totalsModel.getRowCount()>0) totalsModel.removeRow(0);
		totalsModel.addRow(new Object[]{
				Language.string("Total"),
				quantity_tot,
				total
		});
	}
}