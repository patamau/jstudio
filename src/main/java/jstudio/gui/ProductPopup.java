package jstudio.gui;

import java.awt.event.ActionEvent;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import jstudio.control.Controller;
import jstudio.gui.generic.ContextualMenu;
import jstudio.gui.generic.EntityManagerPanel;
import jstudio.model.Invoice;
import jstudio.model.Product;
import jstudio.util.Language;

@SuppressWarnings("serial")
public class ProductPopup extends ContextualMenu<Product> {
	
	private Invoice invoice;
	
	public ProductPopup(EntityManagerPanel<Product> parent, Invoice invoice, Controller<Product> controller){
		super(parent, controller);
		this.invoice = invoice;
	}

	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		if(o==viewItem){
			JDialog dialog = new ProductPanel(context, null, controller).createDialog(parent.getGui());
			dialog.setVisible(true);
		}else if(o==editItem){
			JDialog dialog = new ProductPanel(context, invoice, controller).createDialog(parent.getGui());
			dialog.setVisible(true);
			parent.refresh();
		}else if(o==removeItem){
			int ch = JOptionPane.showConfirmDialog(parent, 
					Language.string("Are you sure you want to remove the event {0}?",context.getDescription()),
					Language.string("Romove event?"), 
					JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if(ch==JOptionPane.YES_OPTION){
				invoice.getProducts().remove(context);
				controller.delete(context);
				parent.refresh();
			}
		}else if(o==newItem){
			JDialog dialog = new ProductPanel(new Product(invoice), invoice, controller).createDialog(parent.getGui());
			dialog.setVisible(true);
			parent.refresh();
		}
	}

}
