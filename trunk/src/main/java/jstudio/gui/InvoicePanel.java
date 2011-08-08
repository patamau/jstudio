package jstudio.gui;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.text.ParseException;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import jstudio.gui.generic.EntityManagerPanel;
import jstudio.gui.generic.EntityPanel;
import jstudio.model.Invoice;
import jstudio.model.Person;
import jstudio.model.Product;
import jstudio.report.ReportGenerator;
import jstudio.util.GUITool;
import jstudio.util.Language;

@SuppressWarnings("serial")
public class InvoicePanel extends EntityPanel<Invoice> {
	
	private JTextField 
		//idField,
		dateField, 
		nameField,
		lastnameField,
		addressField,
		cityField,
		provinceField,
		capField,
		codeField;
	private ProductTable productTable;
	private JButton okButton, cancelButton, printButton;

	public InvoicePanel(Invoice invoice, EntityManagerPanel<Invoice> manager, boolean editable){
		super(invoice, manager);
		
		this.setLayout(new BorderLayout());
		
		JPanel head = new JPanel(new GridBagLayout());
		GridBagConstraints gc = new GridBagConstraints();
		gc.gridx=0;
		gc.gridy=0;
		gc.insets= new Insets(4,4,4,4);
		
		//id field
		GUITool.createField(head, gc, 
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
		
		productTable = new ProductTable(invoice, manager);
		productTable.refresh();
		
		body.add(productTable, BorderLayout.CENTER);
		
		this.add(head, BorderLayout.NORTH);
		this.add(body, BorderLayout.CENTER);
		
		JPanel footer = new JPanel(new GridBagLayout());
		if(editable){
			okButton = GUITool.createButton(footer, gc, Language.string("Ok"), this);
			cancelButton = GUITool.createButton(footer, gc, Language.string("Cancel"), this);
		}
		printButton = GUITool.createButton(footer, gc, Language.string("Print"), this);
		this.add(footer, BorderLayout.SOUTH);
		
		//table.addMouseListener(new PopupListener<Product>(table, new TreatmentPopup(this.gui, this.gui.getApplication().getAccounting().getProductManager())));
	}
	
	public void showProduct(Product t){
		JDialog dialog = new ProductPanel(t, productTable, entity, manager).createDialog(getDialog());
		dialog.setVisible(true);
	}

	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		if(o==okButton){
			try {
				Date d = Person.birthdateFormat.parse(dateField.getText());
				entity.setDate(d);
			} catch (ParseException e1) {
				String msg = Language.string("Wrong date format for {0}, expected {1}",dateField.getText(),Person.birthdateFormat.toPattern());
				JOptionPane.showMessageDialog(this, msg, Language.string("Date format error"),JOptionPane.ERROR_MESSAGE);
				return;
			}
			entity.setName(nameField.getText());
			entity.setLastname(lastnameField.getText());
			entity.setAddress(addressField.getText());
			entity.setCity(cityField.getText());
			entity.setProvince(provinceField.getText());
			entity.setCap(capField.getText());
			entity.setCode(codeField.getText());
			controller.store(entity);
			for(Product p: entity.getProducts()){
				p.setInvoice(entity);
			}
			controller.store(entity);
			getDialog().dispose();
			manager.refresh();
		}else if(o==cancelButton){
			getDialog().dispose();
		}else if(o==printButton){
			ReportGenerator rg = new ReportGenerator();
			rg.setReport("/report1.jasper");
			rg.setHead(entity);
			rg.setData(entity.getProducts());
			try {
				rg.generatePdf(".", "invoice"+entity.getId()+".pdf");
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
	}
}
