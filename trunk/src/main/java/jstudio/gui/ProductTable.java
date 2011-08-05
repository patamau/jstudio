package jstudio.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;

import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

import jstudio.gui.generic.EntityManagerPanel;
import jstudio.gui.generic.PopupListener;
import jstudio.model.Invoice;
import jstudio.model.Product;

@SuppressWarnings("serial")
public class ProductTable 
		extends EntityManagerPanel<Product> {

	public ProductTable(JStudioGUI gui, Invoice invoice){
		super(gui);
		this.setLayout(new BorderLayout());

		table = new JTable();
		model = new ProductTableModel(table, invoice);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		JScrollPane scrollpane = new JScrollPane(table);
		//scrollpane.setPreferredSize(new Dimension(this.getWidth(),this.getHeight()));
		this.add(scrollpane, BorderLayout.CENTER);
		
		//TODO: which controller?
		this.popup = new ProductPopup(this, null);
		scrollpane.addMouseListener(this);
		table.addMouseListener(this);
		table.addMouseListener(new PopupListener<Product>(table, popup));
	}
	
	public void showEntity(Product p){
		JDialog dialog = new ProductPanel(p,null).createDialog(gui);
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
		//FIXME?
	}

	@Override
	public void refresh() {
		//FIXME?
	}
}
