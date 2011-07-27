package jstudio.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import jstudio.model.Invoice;
import jstudio.model.Treatment;
import jstudio.util.Language;
import jstudio.util.PopupListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public class AccountingPanel 
		extends JPanel 
		implements ListSelectionListener, ActionListener {
	
	private static final Logger logger = LoggerFactory.getLogger(AccountingPanel.class);
	
	private DefaultTableModel model;
	private JTable table;
	private JButton refreshButton;
	private JTextField filterField;
	private JStudioGUI gui;
	
	// internally used to catch a double click on the table
	private int lastSelectedRow = -1;
	private long lastSelectionTime = 0;

	public AccountingPanel(JStudioGUI gui){
		this.gui = gui;
		this.setLayout(new BorderLayout());

		table = new JTable();
		model = new AccountingTableModel(table);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.getSelectionModel().addListSelectionListener(this);

		JScrollPane scrollpane = new JScrollPane(table);
		//scrollpane.setPreferredSize(new Dimension(this.getWidth(),this.getHeight()));
		this.add(scrollpane, BorderLayout.CENTER);

		JToolBar actionPanel = new JToolBar(Language.string("Actions"));
		actionPanel.setFloatable(false);
		refreshButton = new JButton(Language.string("Refresh"));
		refreshButton.addActionListener(this);
		actionPanel.add(refreshButton);
		filterField = new JTextField();
		filterField.addActionListener(this);
		actionPanel.add(filterField);
		
		this.add(actionPanel, BorderLayout.NORTH);
		
		table.addMouseListener(new PopupListener<Invoice>(table, new InvoicePopup(this.gui, this.gui.getApplication().getAccounting())));
	}
	
	public void valueChanged(ListSelectionEvent event) {
        int viewRow = table.getSelectedRow();
        if (0<=viewRow){        
        	if(viewRow==lastSelectedRow&&
        			200>(System.currentTimeMillis()-lastSelectionTime)){
        		showInvoice((Invoice)table.getValueAt(viewRow, 0));	
        		table.getSelectionModel().removeSelectionInterval(viewRow, viewRow);
        		lastSelectedRow = -1;
        	}else{
            	lastSelectedRow = viewRow;
            	lastSelectionTime = System.currentTimeMillis();
        		table.getSelectionModel().removeSelectionInterval(viewRow, viewRow);
        	}
        }
    }
	
	public void showInvoice(Invoice i){
		JDialog dialog = InvoicePanel.createDialog(gui, i, false);
		dialog.setVisible(true);
	}
	
	public synchronized void clear(){
		while(model.getRowCount()>0) model.removeRow(0);
	}

	public synchronized void addInvoice(Invoice i){
		StringBuffer sb = new StringBuffer();
		float total = 0f;
		for(Treatment t: i.getTreatments()){
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
			gui.loadInvoices();
		}else{
			logger.warn("Event source not mapped: "+o);
		}
	}
}
