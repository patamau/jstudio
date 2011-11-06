package jstudio.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;

import jstudio.control.Controller;
import jstudio.gui.generic.EntityManagerPanel;
import jstudio.gui.generic.PopupListener;
import jstudio.model.Invoice;
import jstudio.model.Product;
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
	
	private JButton refreshButton;

	public AccountingPanel(Controller<Invoice> controller){
		super(controller);
		this.setLayout(new BorderLayout());

		table = new JTable();
		model = new AccountingTableModel(table);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

	
		JScrollPane scrollpane = new JScrollPane(table);
		//scrollpane.setPreferredSize(new Dimension(this.getWidth(),this.getHeight()));
		this.add(scrollpane, BorderLayout.CENTER);

		JToolBar actionPanel = new JToolBar(Language.string("Actions"));
		actionPanel.setFloatable(false);
		refreshButton = new JButton(Language.string("Refresh"));
		refreshButton.addActionListener(this);
		actionPanel.add(refreshButton);
		filterField = new JTextField();
		filterField.addKeyListener(this);
		filterField.addActionListener(this);
		actionPanel.add(filterField);
		
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
	
	public void showEntity(Invoice i){
		JDialog dialog = new InvoicePanel(i, this, false).createDialog(this.getTopLevelAncestor());
		dialog.setVisible(true);
	}
	
	public synchronized void clear(){
		while(model.getRowCount()>0) model.removeRow(0);
	}

	public synchronized void addEntity(Invoice i){
		StringBuffer sb = new StringBuffer();
		float total = 0f;
		for(Product t: i.getProducts()){
			//FIXME: proper formatting plz
			sb.append(" ");
			sb.append(t.getDescription());
			total += t.getCost()*t.getQuantity();
		}
		model.addRow(new Object[]{
				i,
				i.getDate(),
				i.getName()+" "+i.getLastname(),
				sb.toString(),
				total
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
		}else{
			logger.warn("Event source not mapped: "+o);
		}
	}
}