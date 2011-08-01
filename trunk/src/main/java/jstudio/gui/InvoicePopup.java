package jstudio.gui;

import java.awt.event.ActionEvent;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import jstudio.control.Controller;
import jstudio.gui.generic.ContextualMenu;
import jstudio.gui.generic.EntityManagerPanel;
import jstudio.model.Invoice;
import jstudio.util.Language;

@SuppressWarnings("serial")
public class InvoicePopup extends ContextualMenu<Invoice> {

	private Invoice invoice;
	
	public InvoicePopup(EntityManagerPanel<Invoice> parent, Controller<Invoice> controller){
		super(parent, controller);
	}
	
	@Override
	public void setContext(Invoice context) {
		invoice = context;
	}

	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		if(o==viewItem){
			JDialog dialog = new InvoicePanel(invoice, null).createDialog(parent.getGui());
			dialog.setVisible(true);
		}else if(o==editItem){
			JDialog dialog = new InvoicePanel(invoice, controller).createDialog(parent.getGui());
			dialog.setVisible(true);
		}else if(o==removeItem){
			int ch = JOptionPane.showConfirmDialog(parent, 
					Language.string("Are you sure you want to remove invoice {0} {1}?",invoice.getId(),Invoice.dateFormat.format(invoice.getDate())),
					Language.string("Romove contact?"), 
					JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if(ch==JOptionPane.YES_OPTION){
				//TODO: remove the entry
			}
		}
	}

}
