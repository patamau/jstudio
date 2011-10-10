package jstudio.gui;

import java.awt.event.ActionEvent;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import jstudio.gui.generic.ContextualMenu;
import jstudio.gui.generic.EntityManagerPanel;
import jstudio.model.Invoice;
import jstudio.util.Language;

@SuppressWarnings("serial")
public class InvoicePopup extends ContextualMenu<Invoice> {
	
	public InvoicePopup(EntityManagerPanel<Invoice> parent){
		super(parent);
	}

	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		if(o==viewItem){
			JDialog dialog = new InvoicePanel(context, parent, false).createDialog(parent.getTopLevelAncestor());
			dialog.setVisible(true);
		}else if(o==editItem){
			JDialog dialog = new InvoicePanel(context, parent, true).createDialog(parent.getTopLevelAncestor());
			dialog.setVisible(true);
		}else if(o==removeItem){
			int ch = JOptionPane.showConfirmDialog(parent, 
					Language.string("Are you sure you want to remove invoice {0} {1}?",context.getId(),Invoice.dateFormat.format(context.getDate())),
					Language.string("Romove contact?"), 
					JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if(ch==JOptionPane.YES_OPTION){
				controller.delete(context);
				parent.refresh();
			}
		}else if(o==newItem){
			JDialog dialog = new InvoicePanel(new Invoice(0l), parent, true).createDialog(parent.getTopLevelAncestor());
			dialog.setVisible(true);
		}
	}

}
