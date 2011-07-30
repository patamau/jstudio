package jstudio.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import jstudio.model.Invoice;
import jstudio.model.Product;
import jstudio.util.GUITool;
import jstudio.util.Language;

@SuppressWarnings("serial")
public class InvoicePanel extends JPanel implements ListSelectionListener, ActionListener {
	
	private static JDialog dialog;
	private Invoice invoice;
	private JStudioGUI gui;
	private JTextField 
		idField,
		dateField, 
		nameField,
		lastnameField,
		addressField,
		cityField,
		capField,
		codeField;
	private JTable table; //treatments
	// internally used to catch a double click on the table
	private int lastSelectedRow = -1;
	private long lastSelectionTime = 0;

	public InvoicePanel(Invoice invoice, boolean editable, JStudioGUI gui){
		this.invoice = invoice;
		this.gui = gui;
		
		this.setLayout(new BorderLayout());
		
		JPanel head = new JPanel(new GridBagLayout());
		GridBagConstraints gc = new GridBagConstraints();
		gc.gridx=0;
		gc.gridy=0;
		gc.insets= new Insets(4,4,4,4);
		
		idField = GUITool.createField(head, gc, 
				Language.string("Id"), 
				Long.toString(this.invoice.getId()), false);
		
		dateField = GUITool.createField(head, gc, 
				Language.string("Date"), 
				Invoice.dateFormat.format(this.invoice.getDate()), editable);
		//TODO: add persons browse button
		nameField = GUITool.createField(head, gc, 
				Language.string("Name"), 
				this.invoice.getName(), editable);
		lastnameField = GUITool.createField(head, gc,
				Language.string("Lastname"),
				this.invoice.getLastname(), editable);
		addressField = GUITool.createField(head, gc,
				Language.string("Address"),
				this.invoice.getAddress(), editable);
		cityField = GUITool.createField(head, gc,
				Language.string("City"),
				this.invoice.getCity(), editable);
		capField = GUITool.createField(head, gc, 
				Language.string("CAP"), 
				this.invoice.getCap(), editable);
		codeField = GUITool.createField(head, gc,
				Language.string("Code"),
				this.invoice.getCode(), editable);
		
		JPanel body = new JPanel(new BorderLayout());
		
		table = new JTable(){
		  public Dimension getPreferredScrollableViewportSize() {
			  return getPreferredSize();
		  }
		};
		DefaultTableModel model = new TreatmentTableModel(table, invoice);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.getSelectionModel().addListSelectionListener(this);
		
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
		
		body.add(new JScrollPane(table), BorderLayout.CENTER);
		
		JTable total = new JTable();
		DefaultTableModel tmodel = new TreatmentTableModel(total, null);
		total.setRowSelectionAllowed(false);		
		tmodel.addRow(new Object[]{
				Language.string("Total"),
				quantity_tot,
				cost_tot
		});
		
		body.add(total, BorderLayout.SOUTH);
		
		this.add(head, BorderLayout.NORTH);
		this.add(body, BorderLayout.CENTER);
		
		//table.addMouseListener(new PopupListener<Treatment>(table, new TreatmentPopup(this.gui, this.gui.getApplication().getAccounting().getTreatmentManager())));
	}
	
	/**
	 * Creates a specific dialog for the person,
	 * handling specifically changes, removal and additional links
	 * The dialog is modal if it is editable
	 * @param parent
	 * @return
	 */
	public static JDialog createDialog(JStudioGUI gui, Invoice object, boolean editable){
		if(dialog==null){
			dialog = new JDialog(gui);
			dialog.setTitle(Language.string("Invoice dialog"));
			dialog.getContentPane().setLayout(new BorderLayout());
		}
		dialog.setLocationRelativeTo(gui);
		dialog.setModal(editable);
		dialog.getContentPane().removeAll();
		dialog.getContentPane().add(new InvoicePanel(object, editable, gui),BorderLayout.CENTER);
		dialog.pack();
		return dialog;
	}
	
	public void valueChanged(ListSelectionEvent event) {
        int viewRow = table.getSelectedRow();
        if (0<=viewRow){        
        	if(viewRow==lastSelectedRow&&
        			200>(System.currentTimeMillis()-lastSelectionTime)){
        		showProduct((Product)table.getValueAt(viewRow, 0));	
        		table.getSelectionModel().removeSelectionInterval(viewRow, viewRow);
        		lastSelectedRow = -1;
        	}else{
            	lastSelectedRow = viewRow;
            	lastSelectionTime = System.currentTimeMillis();
        		table.getSelectionModel().removeSelectionInterval(viewRow, viewRow);
        	}
        }
    }
	
	public void showProduct(Product t){
		JDialog dialog = ProductPanel.createDialog(gui, t, gui.getApplication().getAccounting().getTreatmentManager());
		dialog.setVisible(true);
	}

	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		//TODO: ok, cancel, edit
	}
}
