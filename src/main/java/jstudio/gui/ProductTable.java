package jstudio.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

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

	private JButton refreshButton;
	
	public ProductTable(JStudioGUI gui, Invoice invoice){
		super(gui);
		this.invoice = invoice;
		this.setLayout(new BorderLayout());
		
		JPanel buttonsPanel = new JPanel();
		refreshButton = new JButton(Language.string("Refresh"));
		refreshButton.addActionListener(this);
		buttonsPanel.add(refreshButton);
		
		this.add(buttonsPanel, BorderLayout.NORTH);

		table = new JTable();
		model = new ProductTableModel(table, invoice);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		JScrollPane scrollpane = new JScrollPane(table);
		//scrollpane.setPreferredSize(new Dimension(this.getWidth(),this.getHeight()));
		this.add(scrollpane, BorderLayout.CENTER);
		
		if(gui!=null){
		this.popup = new ProductPopup(this, 
				invoice, 
				super.gui.getApplication().getAccounting().getProducts());
		}else{
			this.popup = new ProductPopup(this, 
				null, 
				null);
		}
		scrollpane.addMouseListener(this);
		table.addMouseListener(this);
		table.addMouseListener(new PopupListener<Product>(table, popup));
		
		totals = new JTable();
		totalsModel = new ProductTableModel(totals, null);
		totals.setRowSelectionAllowed(false);		
		
		this.add(totals, BorderLayout.SOUTH);
	}
	
	public void showEntity(Product p){
		JDialog dialog = new ProductPanel(p, invoice, gui.getApplication().getAccounting().getProducts()).createDialog(gui);
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
		Object o = e.getSource();
		if(o==refreshButton){
			refresh();
		}
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
