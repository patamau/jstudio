package jstudio.gui;

import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;

import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import jstudio.gui.generic.ContextualMenu;
import jstudio.gui.generic.EntityManagerPanel;
import jstudio.model.Invoice;
import jstudio.model.Person;
import jstudio.model.Product;
import jstudio.report.ReportGenerator;
import jstudio.report.ReportGeneratorGUI;
import jstudio.util.Configuration;
import jstudio.util.Language;

@SuppressWarnings("serial")
public class InvoicePopup extends ContextualMenu<Invoice> {
	
	private static final Logger logger = Logger.getLogger(InvoicePopup.class);
	
	private JMenuItem printItem;
	
	public InvoicePopup(EntityManagerPanel<Invoice> parent){
		super(parent);
		this.remove(newItem);
		printItem = new JMenuItem(Language.string("Print..."));
	    printItem.setFont(deleteItem.getFont().deriveFont(Font.PLAIN));
	    printItem.addActionListener(this);
	    this.add(printItem);
		this.add(new JSeparator());
		this.add(newItem);
	}
	
	public void setContext(Invoice invoice){
		super.setContext(invoice);
		printItem.setEnabled(invoice!=null);
	}

	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		if(o==viewItem){
			JDialog dialog = new InvoicePanel(context, parent, false).createDialog(parent.getTopLevelAncestor());
			dialog.setVisible(true);
		}else if(o==editItem){
			JDialog dialog = new InvoicePanel(context, parent, true).createDialog(parent.getTopLevelAncestor());
			dialog.setVisible(true);
		}else if(o==deleteItem){
			int ch = JOptionPane.showConfirmDialog(parent, 
					Language.string("Are you sure you want to remove invoice {0} {1}?",context.getId(),Invoice.dateFormat.format(context.getDate())),
					Language.string("Remove invoice?"), 
					JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if(ch==JOptionPane.YES_OPTION){
				controller.delete(context);
				parent.refresh();
			}
		}else if(o==newItem){
			JDialog dialog = new InvoicePanel(new Invoice(0l), parent, true).createDialog(parent.getTopLevelAncestor());
			dialog.setVisible(true);
		}else if(o==printItem){
			if(context==null) logger.error("No such context");
			ReportGenerator rg = new ReportGenerator();
			rg.setReport(Configuration.getGlobal(InvoicePanel.INVOICE_REPORT, InvoicePanel.INVOICE_REPORT_DEF));
			rg.setHead(context);
			rg.setHeadValue("date", Person.birthdateFormat.format(context.getDate()));
			rg.setHeadValue("note", Language.string(context.getNote()));
			rg.setData(context.getProducts());
			rg.setHeadValue("stamp", Product.formatCurrency(context.getStamp()));
			float tot = 0f;
			for(Product p: context.getProducts()){
				tot += p.getCost()*p.getQuantity();
			}
			rg.setHeadValue("totalcost", Product.formatCurrency(tot));
			ReportGeneratorGUI rgui = new ReportGeneratorGUI(rg,"invoice_"+context.getFilePrefix());
			rgui.showGUI((Window)SwingUtilities.getRoot(this));		
		}
	}

}
