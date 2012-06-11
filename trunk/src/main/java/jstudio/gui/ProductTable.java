package jstudio.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

import jstudio.control.Accounting;
import jstudio.gui.generic.EntityManagerPanel;
import jstudio.gui.generic.PopupListener;
import jstudio.model.Invoice;
import jstudio.model.Product;
import jstudio.util.Configuration;
import jstudio.util.Language;

import org.apache.log4j.Logger;

@SuppressWarnings("serial")
public class ProductTable 
		extends EntityManagerPanel<Product> {
	
	private static final Logger logger = Logger.getLogger(ProductTable.class);
	
	private JLabel totalLabel;
	private Invoice invoice;
	private EntityManagerPanel<Invoice> accounting;
	//private JButton newButton, editButton, deleteButton;
	
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
			toolBar.add(newButton);
			toolBar.add(Box.createHorizontalGlue());
			toolBar.add(editButton);
			toolBar.add(deleteButton);
			this.add(toolBar, BorderLayout.NORTH);
		}

		model = new ProductTableModel(table, invoice);
		if(!editable){
			table.setBackground(this.getBackground());
			table.setCellSelectionEnabled(false);
			table.setFocusable(false);
		}
		table.setShowGrid(false);
		table.setShowHorizontalLines(true);

		JScrollPane scrollpane = new JScrollPane(table);
		scrollpane.setBorder(BorderFactory.createEmptyBorder(2, 2, 5, 2));
		scrollpane.setPreferredSize(new Dimension(0,100));
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
	
	public void showEntity(Product p, boolean edit){
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
		}else if(o==editButton){
			int r = table.getSelectedRow();
			if(0<=r){
				Product p = (Product) model.getValueAt(r, 0);
				if(null!=p){
					JDialog dialog = new ProductPanel(p, this, invoice, accounting).createDialog(this.getTopLevelAncestor());
					dialog.setVisible(true);
				}else{
					logger.error("No product at row "+r);
				}
			}else{
				logger.warn("No row selected");
			}
		}else if(o==deleteButton){
			int r = table.getSelectedRow();
			if(0<=r){
				Product p = (Product) model.getValueAt(r, 0);
				if(null!=p){
					int ch = JOptionPane.showConfirmDialog(this, 
							Language.string("Are you sure you want to remove the product {0}?",p.getDescription()),
							Language.string("Romove product?"), 
							JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
					if(ch==JOptionPane.YES_OPTION){
						if(!invoice.getProducts().remove(p)){
							throw new IllegalArgumentException("No such product to be removed");
						}
						controller.delete(p);
						accounting.getController().store(invoice);
						refresh();
						accounting.refresh();
					}
				}else{
					logger.error("No product at row "+r);
				}
			}else{
				logger.warn("No row selected");
			}
		}
	}
	
	public float getTotal(){
		return total;
	}

	@Override
	public void refresh() {
		this.clear();
		for(Product t: invoice.getProducts()){
			addEntity(t);
		}
		totalLabel.setText(Product.formatCurrency(invoice.getTotal()));
	}
}
