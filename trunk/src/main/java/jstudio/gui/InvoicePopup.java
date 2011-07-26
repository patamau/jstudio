package jstudio.gui;

import java.awt.event.ActionEvent;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import jstudio.model.Invoice;
import jstudio.util.ContextualMenu;
import jstudio.util.Language;

@SuppressWarnings("serial")
public class InvoicePopup extends ContextualMenu<Invoice> {

	private Invoice invoice;
	
	public InvoicePopup(JFrame parent){
		super(parent);
	}
	
	@Override
	public void setContext(Invoice context) {
		invoice = context;
	}

	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		if(o==viewItem){
			JDialog dialog = InvoicePanel.createDialog(parent, invoice, false);
			dialog.setVisible(true);
		}else if(o==editItem){
			JDialog dialog = InvoicePanel.createDialog(parent, invoice, true);
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
