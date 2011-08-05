package jstudio.gui;

import java.awt.event.ActionEvent;
import java.util.Date;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import jstudio.control.Controller;
import jstudio.gui.generic.ContextualMenu;
import jstudio.gui.generic.EntityManagerPanel;
import jstudio.model.Event;
import jstudio.model.Product;
import jstudio.util.Language;

@SuppressWarnings("serial")
public class ProductPopup extends ContextualMenu<Product> {
	
	public ProductPopup(EntityManagerPanel<Product> parent, Controller<Product> controller){
		super(parent, controller);
	}

	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		if(o==viewItem){
			JDialog dialog = new ProductPanel(context, null).createDialog(parent.getGui());
			dialog.setVisible(true);
		}else if(o==editItem){
			JDialog dialog = new ProductPanel(context, controller).createDialog(parent.getGui());
			dialog.setVisible(true);
			parent.refresh();
		}else if(o==removeItem){
			int ch = JOptionPane.showConfirmDialog(parent, 
					Language.string("Are you sure you want to remove the event {0}?",context.getDescription()),
					Language.string("Romove event?"), 
					JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if(ch==JOptionPane.YES_OPTION){
				controller.delete(context);
				parent.refresh();
			}
		}else if(o==newItem){
			JDialog dialog = new ProductPanel(new Product(), controller).createDialog(parent.getGui());
			dialog.setVisible(true);
			parent.refresh();
		}
	}

}
