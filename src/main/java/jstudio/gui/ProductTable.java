package jstudio.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
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
	
	private JLabel totalLabel;
	private Invoice invoice;
	private EntityManagerPanel<Invoice> accounting;
	private JButton newButton;
	
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
			newButton = new JButton(Language.string("Add Product"));
			newButton.addActionListener(this);
			toolBar.add(newButton);
			this.add(toolBar, BorderLayout.NORTH);
		}

		table = new JTable(){
			public Dimension getPreferredScrollableViewportSize() {
				return getPreferredSize();
			}
		};
		model = new ProductTableModel(table, invoice);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		if(!editable){
			table.setBackground(this.getBackground());
			table.setCellSelectionEnabled(false);
			table.setFocusable(false);
			table.setShowGrid(false);
		}
		table.setMinimumSize(new Dimension(50,50));
		table.setPreferredSize(new Dimension(50,50));

		JScrollPane scrollpane = new JScrollPane(table);
		scrollpane.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
		//scrollpane.setPreferredSize(new Dimension(this.getWidth(),this.getHeight()));
		this.add(scrollpane, BorderLayout.CENTER);
		
		if(editable){
			this.popup = new ProductPopup(this, invoice, accounting);
			scrollpane.addMouseListener(this);
			table.addMouseListener(this);
			table.addMouseListener(new PopupListener<Product>(table, popup));
		}
		
		JPanel totalPanel = new JPanel(new GridBagLayout());
		GridBagConstraints gc = new GridBagConstraints();
		gc.gridx=gc.gridy=0;
		gc.weightx=1.0f;
		gc.fill=GridBagConstraints.HORIZONTAL;
		totalPanel.add(new JLabel("Total"), gc);
		totalLabel = new JLabel("0");
		++gc.gridx;
		gc.weightx=0.0f;
		gc.fill=GridBagConstraints.NONE;
		totalPanel.add(totalLabel,gc);
		this.add(totalPanel, BorderLayout.SOUTH);
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
		Object o = e.getSource();
		if(o==newButton){
			JDialog dialog = new ProductPanel(new Product(), this, invoice, accounting).createDialog(this.getTopLevelAncestor());
			dialog.setVisible(true);
		}
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
		totalLabel.setText(Float.toString(total));
	}
}
