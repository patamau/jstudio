package jstudio.gui;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import jstudio.control.Accounting;
import jstudio.gui.generic.EntityManagerPanel;
import jstudio.gui.generic.EntityPanel;
import jstudio.gui.generic.NicePanel;
import jstudio.model.Invoice;
import jstudio.model.Person;
import jstudio.model.Product;
import jstudio.report.ReportGenerator;
import jstudio.report.ReportGeneratorGUI;
import jstudio.util.Configuration;
import jstudio.util.GUITool;
import jstudio.util.Language;

@SuppressWarnings("serial")
public class InvoicePanel extends EntityPanel<Invoice> {
	
	public static final String 
		INVOICE_REPORT = "report.invoice",
		INVOICE_REPORT_DEF = "/reports/invoice.jasper";
	
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
	private JButton okButton, cancelButton, printButton, closeButton;
	private JButton pickPersonButton;

	public InvoicePanel(Invoice invoice, EntityManagerPanel<Invoice> manager, boolean editable){
		super(invoice, manager);
		
		Calendar c = Calendar.getInstance();
		c.setTime(invoice.getDate());
		NicePanel panel = new NicePanel(entity.getFullNumber(),editable?Language.string("Edit details"):Language.string("View details"));
		panel.getBody().setLayout(new BorderLayout());
		this.setLayout(new BorderLayout());
		this.add(panel, BorderLayout.CENTER);
		
		JPanel head = new JPanel(new GridBagLayout());
		GridBagConstraints gc = new GridBagConstraints();
		gc.gridx=0;
		gc.gridy=0;
		gc.insets= new Insets(4,4,4,4);
		
		//id field
		GUITool.createField(head, gc, 
				Language.string("Number"), 
				Long.toString(this.entity.getNumber()), false);
		
		dateField = GUITool.createField(head, gc, 
				Language.string("Date"), 
				Invoice.dateFormat.format(this.entity.getDate()), editable);
		if(editable){
			pickPersonButton = GUITool.createButton(head, gc, 
				Language.string("Pick"), this);
		}
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
		provinceField = GUITool.createProvinceField(head, gc,
				Language.string("Province"),
				this.entity.getProvince(), editable);
		capField = GUITool.createCAPField(head, gc, 
				Language.string("CAP"), 
				this.entity.getCap(), editable);
		codeField = GUITool.createField(head, gc,
				Language.string("Code"),
				this.entity.getCode(), editable);
		
		JPanel body = new JPanel(new BorderLayout());
		
		productTable = new ProductTable(invoice, manager, editable);
		productTable.refresh();
		
		body.add(productTable, BorderLayout.CENTER);
		
		panel.getBody().add(head, BorderLayout.NORTH);
		panel.getBody().add(body, BorderLayout.CENTER);
		
		printButton = new JButton(Language.string("Print"));
		printButton.addActionListener(this);
		panel.addButton(printButton);
		panel.addButtonsGlue();
		
		if(editable){
			okButton = new JButton(Language.string("Ok"));
			okButton.addActionListener(this);
			panel.addButton(okButton);
			cancelButton = new JButton(Language.string("Cancel"));
			cancelButton.addActionListener(this);
			panel.addButton(cancelButton);
		}else{
			closeButton = new JButton(Language.string("Close"));
			closeButton.addActionListener(this);
			panel.addButton(closeButton);
		}
		
		//table.addMouseListener(new PopupListener<Product>(table, new TreatmentPopup(this.gui, this.gui.getApplication().getAccounting().getProductManager())));
	}
	
	public void showProduct(Product t){
		JDialog dialog = new ProductPanel(t, productTable, entity, manager).createDialog(getDialog());
		dialog.setVisible(true);
	}

	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		if(o==pickPersonButton){
			PersonSelectionPanel psp = new PersonSelectionPanel(controller.getApplication().getAddressBook());
			psp.showDialog(this, nameField.getText()+" "+lastnameField.getText());
			Person p = psp.getSelected();
			if(p!=null){
				nameField.setText(p.getName());
				lastnameField.setText(p.getLastname());
				addressField.setText(p.getAddress());
				capField.setText(p.getCap());
				cityField.setText(p.getCity());
				codeField.setText(p.getCode());
				provinceField.setText(p.getProvince());
			}
		}else if(o==okButton){
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
			if(entity.getId()==0) entity.setId(controller.getNextId());
			if(entity.getNumber()==0) entity.setNumber(((Accounting)controller).getNextInvoiceNumber());
			//controller.store(entity);
			long pid = ((Accounting)controller).getProducts().getNextId();
			for(Product p: entity.getProducts()){
				if(p.getId()==0) p.setId(++pid);
				p.setInvoice(entity);
			}
			controller.store(entity);
			getDialog().dispose();
			manager.refresh();
		}else if(o==cancelButton||o==closeButton){
			getDialog().dispose();
		}else if(o==printButton){
			ReportGenerator rg = new ReportGenerator();
			rg.setReport(Configuration.getGlobal(INVOICE_REPORT, INVOICE_REPORT_DEF));
			rg.setHead(entity);
			rg.setHeadValue("date", Person.birthdateFormat.format(entity.getDate()));
			rg.setData(entity.getProducts());
			rg.setHeadValue("totalcost", Float.toString(productTable.getTotal()));
			ReportGeneratorGUI rgui = new ReportGeneratorGUI(rg,"invoice"+entity.getId());
			rgui.showGUI((Window)SwingUtilities.getRoot(this));
		}
	}
}
