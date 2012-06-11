package jstudio.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;

import jstudio.control.Controller;
import jstudio.gui.generic.EntityManagerPanel;
import jstudio.gui.generic.PopupListener;
import jstudio.model.Invoice;
import jstudio.model.Person;
import jstudio.model.Product;
import jstudio.report.ReportGenerator;
import jstudio.report.ReportGeneratorGUI;
import jstudio.util.Configuration;
import jstudio.util.Language;
import jstudio.util.Resources;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public class AccountingPanel extends EntityManagerPanel<Invoice> {
	
	private static final Logger logger = LoggerFactory.getLogger(AccountingPanel.class);
	
	public static final String
		LABEL_ACCOUNTING="Accounting",
		PIC_ACCOUNTING="invoiceicon.png";
	
	private JButton printButton;

	public AccountingPanel(Controller<Invoice> controller){
		super(controller);
		this.setLayout(new BorderLayout());
		
		model = new AccountingTableModel(table);
		
		JScrollPane scrollpane = new JScrollPane(table);
		this.add(scrollpane, BorderLayout.CENTER);

		JToolBar actionPanel = new JToolBar(Language.string("Actions"));
		actionPanel.setFloatable(false);
		actionPanel.add(newButton);
		actionPanel.addSeparator();
		actionPanel.add(viewButton);
		actionPanel.add(editButton);
		actionPanel.add(deleteButton);
		printButton = new JButton(Language.string("Print"));
		printButton.setEnabled(false);
		printButton.addActionListener(this);
		actionPanel.add(printButton);
		actionPanel.add(Box.createHorizontalGlue());
		actionPanel.add(filterField);
		actionPanel.setPreferredSize(new Dimension(0,25));
		actionPanel.add(refreshButton);
		
		this.add(actionPanel, BorderLayout.NORTH);
		
		this.popup = new InvoicePopup(this);
		scrollpane.addMouseListener(this);
		table.addMouseListener(this);
		table.addMouseListener(new PopupListener<Invoice>(table, popup));
	}
	
	public String getLabel(){
		return Language.string(LABEL_ACCOUNTING);
	}
	
	public ImageIcon getIcon(){
		return Resources.getImage(PIC_ACCOUNTING);
	}
	
	public void showEntity(Invoice i, boolean edit){
		JDialog dialog = new InvoicePanel(i, this, edit).createDialog(this.getTopLevelAncestor());
		dialog.setVisible(true);
	}
	
	public synchronized void clear(){
		while(model.getRowCount()>0) model.removeRow(0);
	}

	public synchronized void addEntity(Invoice i){
		StringBuffer sb = new StringBuffer();
		int c = i.getProducts().size();
		for(Product t: i.getProducts()){
			sb.append(t.getDescription());
			//total += t.getCost()*t.getQuantity();
			--c;
			if(c>0){
				sb.append(", ");
			}
		}
		model.addRow(new Object[]{
				i,
				i.getDate(),
				i.getName()+" "+i.getLastname(),
				sb.toString(),
				i.getTotal()
		});
	}
	
	public synchronized void removeInvoice(int id){
		Invoice ob;
		int frow = -1;
		for(int i=0; i<model.getRowCount(); i++){
			ob = (Invoice)model.getValueAt(i, 0);
			if(ob.getId()==id){
				frow = i;
				break;
			}
		}
		if(frow>-1){
			model.removeRow(frow);
		}
	}

	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		if(o==refreshButton){
			refresh();
		}else if(o==newButton){
			showEntity(new Invoice(), true);
			this.refresh();
		}else if(o==editButton){
			int row = table.convertRowIndexToModel(table.getSelectedRow());
			if(row>=0){
				showEntity((Invoice)model.getValueAt(row, 0), false);
				this.refresh();
			}
		}else if(o==viewButton){
			int row = table.convertRowIndexToModel(table.getSelectedRow());
			if(row>=0){
				showEntity((Invoice)model.getValueAt(row, 0), false);
				this.refresh();
			}
		}else if(o==deleteButton){
			int row = table.convertRowIndexToModel(table.getSelectedRow());
			if(row>=0){
				Invoice context = (Invoice)model.getValueAt(row, 0);
				int ch = JOptionPane.showConfirmDialog(this, 
						Language.string("Are you sure you want to remove invoice {0} {1}?",context.getId(),Invoice.dateFormat.format(context.getDate())),
						Language.string("Romove invoice?"), 
						JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
				if(ch==JOptionPane.YES_OPTION){
					controller.delete(context);
					this.refresh();
				}
			}
		}else if(o==printButton){
			int row = table.convertRowIndexToModel(table.getSelectedRow());
			if(row<0) return;
			Invoice context = (Invoice)model.getValueAt(row, 0);
			ReportGenerator rg = new ReportGenerator();
			rg.setReport(Configuration.getGlobal(InvoicePanel.INVOICE_REPORT, InvoicePanel.INVOICE_REPORT_DEF));
			rg.setHead(context);
			rg.setHeadValue("date", Person.birthdateFormat.format(context.getDate()));
			rg.setHeadValue("note", context.getNote());
			rg.setData(context.getProducts());
			rg.setHeadValue("stamp", Product.formatCurrency(context.getStamp()));
			float tot = 0f;
			for(Product p: context.getProducts()){
				tot += p.getCost()*p.getQuantity();
			}
			rg.setHeadValue("totalcost", Product.formatCurrency(tot));
			ReportGeneratorGUI rgui = new ReportGeneratorGUI(rg,"invoice_"+context.getFilePrefix());
			rgui.showGUI((Window)SwingUtilities.getRoot(this));		
		}else{
			logger.warn("Event source not mapped: "+o);
		}
	}
	
	public void valueChanged(ListSelectionEvent e) {
        //Ignore extra messages.
        if (e.getValueIsAdjusting()) return;		
		super.valueChanged(e);

        ListSelectionModel lsm = (ListSelectionModel)e.getSource();
        if (lsm.isSelectionEmpty()) {
            printButton.setEnabled(false);
        } else {
        	printButton.setEnabled(true);
        }
    }
}
