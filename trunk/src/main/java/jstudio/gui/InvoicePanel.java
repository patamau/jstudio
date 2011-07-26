package jstudio.gui;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

import jstudio.model.Event;
import jstudio.model.Invoice;
import jstudio.util.GUITool;
import jstudio.util.Language;

@SuppressWarnings("serial")
public class InvoicePanel extends JPanel implements ActionListener {
	
	private static JDialog dialog;
	private Invoice invoice;
	private JTextField 
		dateField, 
		nameField,
		lastnameField,
		addressField,
		cityField,
		capField,
		codeField;

	public InvoicePanel(Invoice invoice, boolean editable){
		this.invoice = invoice;
		
		this.setLayout(new GridBagLayout());
		GridBagConstraints gc = new GridBagConstraints();
		gc.gridx=0;
		gc.gridy=0;
		gc.insets= new Insets(4,4,4,4);
		
		dateField = GUITool.createField(this, gc, 
				Language.string("Date"), 
				Invoice.dateFormat.format(this.invoice.getDate()), editable);
		//TODO: add persons browse button
		nameField = GUITool.createField(this, gc, 
				Language.string("Name"), 
				this.invoice.getName(), editable);
		lastnameField = GUITool.createField(this, gc,
				Language.string("Lastname"),
				this.invoice.getLastname(), editable);
		addressField = GUITool.createField(this, gc,
				Language.string("Address"),
				this.invoice.getAddress(), editable);
		cityField = GUITool.createField(this, gc,
				Language.string("City"),
				this.invoice.getCity(), editable);
		capField = GUITool.createField(this, gc, 
				Language.string("CAP"), 
				this.invoice.getCap(), editable);
		codeField = GUITool.createField(this, gc,
				Language.string("Code"),
				this.invoice.getCode(), editable);
	}
	
	/**
	 * Creates a specific dialog for the person,
	 * handling specifically changes, removal and additional links
	 * The dialog is modal if it is editable
	 * @param parent
	 * @return
	 */
	public static JDialog createDialog(JFrame parent, Invoice object, boolean editable){
		if(dialog==null){
			dialog = new JDialog(parent);
			dialog.setTitle(Language.string("Invoice dialog"));
			dialog.getContentPane().setLayout(new BorderLayout());
		}
		dialog.setLocationRelativeTo(parent);
		dialog.setModal(editable);
		dialog.getContentPane().removeAll();
		dialog.getContentPane().add(new InvoicePanel(object, editable),BorderLayout.CENTER);
		dialog.pack();
		return dialog;
	}

	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		//TODO: ok, cancel, edit
	}
}
