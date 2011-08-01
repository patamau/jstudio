package jstudio.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import jstudio.control.Accounting;
import jstudio.control.Controller;
import jstudio.gui.generic.EntityPanel;
import jstudio.model.Invoice;
import jstudio.model.Product;
import jstudio.util.GUITool;
import jstudio.util.Language;

@SuppressWarnings("serial")
public class InvoicePanel extends EntityPanel<Invoice> implements ListSelectionListener {
	
	private JTextField 
		idField,
		dateField, 
		nameField,
		lastnameField,
		addressField,
		cityField,
		provinceField,
		capField,
		codeField;
	private JTable productTable;
	// internally used to catch a double click on the table
	private int lastSelectedRow = -1;
	private long lastSelectionTime = 0;

	public InvoicePanel(Invoice invoice, Controller<Invoice> controller){
		super(invoice, controller);
		boolean editable = controller!=null;
		
		this.setLayout(new BorderLayout());
		
		JPanel head = new JPanel(new GridBagLayout());
		GridBagConstraints gc = new GridBagConstraints();
		gc.gridx=0;
		gc.gridy=0;
		gc.insets= new Insets(4,4,4,4);
		
		idField = GUITool.createField(head, gc, 
				Language.string("Id"), 
				Long.toString(this.entity.getId()), false);
		
		dateField = GUITool.createField(head, gc, 
				Language.string("Date"), 
				Invoice.dateFormat.format(this.entity.getDate()), editable);
		//TODO: add persons browse button
		nameField = GUITool.createField(head, gc, 
				Language.string("Name"), 
				this.entity.getName(), editable);
		lastnameField = GUITool.createField(head, gc,
				Language.string("Lastname"),
				this.entity.getLastname(), editable);
		addressField = GUITool.createField(head, gc,
				Language.string("Address"),
				this.entity.getAddress(), editable);
		cityField = GUITool.createField(head, gc,
				Language.string("City"),
				this.entity.getCity(), editable);
		provinceField = GUITool.createField(head, gc,
				Language.string("Province"),
				this.entity.getProvince(), editable);
		capField = GUITool.createField(head, gc, 
				Language.string("CAP"), 
				this.entity.getCap(), editable);
		codeField = GUITool.createField(head, gc,
				Language.string("Code"),
				this.entity.getCode(), editable);
		
		JPanel body = new JPanel(new BorderLayout());
		
		productTable = new JTable(){
		  public Dimension getPreferredScrollableViewportSize() {
			  return getPreferredSize();
		  }
		};
		DefaultTableModel model = new ProductTableModel(productTable, invoice);
		productTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		productTable.getSelectionModel().addListSelectionListener(this);
		
		int quantity_tot = 0;
		float cost_tot = 0f;
		for(Product t: invoice.getProducts()){
			model.addRow(new Object[]{
					t,
					t.getQuantity(),
					t.getCost()
			});
			quantity_tot += t.getQuantity();
			cost_tot += t.getCost();
		}
		
		body.add(new JScrollPane(productTable), BorderLayout.CENTER);
		
		JTable total = new JTable();
		DefaultTableModel tmodel = new ProductTableModel(total, null);
		total.setRowSelectionAllowed(false);		
		tmodel.addRow(new Object[]{
				Language.string("Total"),
				quantity_tot,
				cost_tot
		});
		
		body.add(total, BorderLayout.SOUTH);
		
		this.add(head, BorderLayout.NORTH);
		this.add(body, BorderLayout.CENTER);
		
		//table.addMouseListener(new PopupListener<Product>(table, new TreatmentPopup(this.gui, this.gui.getApplication().getAccounting().getProductManager())));
	}
	
	public void valueChanged(ListSelectionEvent event) {
        int viewRow = productTable.getSelectedRow();
        if (0<=viewRow){        
        	if(viewRow==lastSelectedRow&&
        			200>(System.currentTimeMillis()-lastSelectionTime)){
        		showProduct((Product)productTable.getValueAt(viewRow, 0));	
        		productTable.getSelectionModel().removeSelectionInterval(viewRow, viewRow);
        		lastSelectedRow = -1;
        	}else{
            	lastSelectedRow = viewRow;
            	lastSelectionTime = System.currentTimeMillis();
        		productTable.getSelectionModel().removeSelectionInterval(viewRow, viewRow);
        	}
        }
    }
	
	public void showProduct(Product t){
		//TODO:		
		Controller<Product> cp = controller==null?null:((Accounting)controller).getProducts();
		JDialog dialog = new ProductPanel(t, cp).createDialog(getDialog());
		dialog.setVisible(true);
	}

	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		//TODO: ok, cancel, edit
	}
}
