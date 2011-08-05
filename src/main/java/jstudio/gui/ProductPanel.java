package jstudio.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import jstudio.control.Controller;
import jstudio.gui.generic.EntityPanel;
import jstudio.model.Invoice;
import jstudio.model.Product;
import jstudio.util.GUITool;
import jstudio.util.Language;

@SuppressWarnings("serial")
public class ProductPanel extends EntityPanel<Product> {
	
	private Invoice invoice;
	
	private JTextField 
		descriptionField, 
		quantityField,
		costField;
	private JButton okButton, cancelButton;

	public ProductPanel(Product product, Invoice invoice, Controller<Product> controller){
		super(product, controller);
		boolean editable = invoice!=null;
		this.invoice = invoice;
		
		this.setLayout(new GridBagLayout());
		GridBagConstraints gc = new GridBagConstraints();
		gc.gridx=0;
		gc.gridy=0;
		gc.insets= new Insets(4,4,4,4);
		
		descriptionField = GUITool.createField(this, gc, Language.string("Description"), this.entity.getDescription(), editable);
		quantityField = GUITool.createField(this, gc, Language.string("Quantity"), Integer.toString(this.entity.getQuantity()), editable);
		costField = GUITool.createField(this, gc, Language.string("Cost"), Float.toString(this.entity.getCost()), editable);	
		
		if(editable){
			okButton = GUITool.createButton(this, gc, Language.string("Ok"),this);
			cancelButton = GUITool.createButton(this, gc, Language.string("Cancel"),this);
		}
	}

	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		if(o==okButton){
			try{
				entity.setQuantity(Integer.parseInt(quantityField.getText()));
				entity.setCost(Float.parseFloat(costField.getText()));
				entity.setDescription(descriptionField.getText());
				if(entity.getId()==0){
					invoice.getProducts().add(entity);
				}
				getDialog().dispose();
			}catch(NumberFormatException ex){
				String msg = Language.string("A number inserted has a bad format");
				JOptionPane.showMessageDialog(this, msg, Language.string("Wrong value"), JOptionPane.ERROR_MESSAGE);
			}
		}else if(o==cancelButton){
			getDialog().dispose();
		}
	}
}
