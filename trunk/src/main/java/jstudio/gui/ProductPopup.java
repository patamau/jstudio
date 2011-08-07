package jstudio.gui;

import java.awt.event.ActionEvent;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import jstudio.gui.generic.ContextualMenu;
import jstudio.gui.generic.EntityManagerPanel;
import jstudio.model.Invoice;
import jstudio.model.Product;
import jstudio.util.Language;

@SuppressWarnings("serial")
public class ProductPopup extends ContextualMenu<Product> {
	
	private Invoice invoice;
	private EntityManagerPanel<Invoice> accounting;
	
	public ProductPopup(EntityManagerPanel<Product> parent, Invoice invoice, EntityManagerPanel<Invoice> accounting){
		super(parent);
		this.invoice = invoice;
		this.accounting = accounting;
	}

	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		if(o==viewItem){
			JDialog dialog = new ProductPanel(context, null, invoice, accounting).createDialog(parent.getTopLevelAncestor());
			dialog.setVisible(true);
		}else if(o==editItem){
			JDialog dialog = new ProductPanel(context, null, invoice, accounting).createDialog(parent.getTopLevelAncestor());
			dialog.setVisible(true);
		}else if(o==removeItem){
			int ch = JOptionPane.showConfirmDialog(parent, 
					Language.string("Are you sure you want to remove the event {0}?",context.getDescription()),
					Language.string("Romove event?"), 
					JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if(ch==JOptionPane.YES_OPTION){
				if(!invoice.getProducts().remove(context)){
					throw new IllegalArgumentException("No such context to be removed");
				}
				controller.delete(context);
				accounting.getController().store(invoice);
				parent.refresh();
				accounting.refresh();
			}
		}else if(o==newItem){
			JDialog dialog = new ProductPanel(new Product(invoice), parent, invoice, accounting).createDialog(parent.getTopLevelAncestor());
			dialog.setVisible(true);
		}
	}

}
