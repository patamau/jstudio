package jstudio.gui;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import jstudio.gui.generic.EntityManagerPanel;
import jstudio.gui.generic.EntityPanel;
import jstudio.gui.generic.NicePanel;
import jstudio.model.Invoice;
import jstudio.model.Product;
import jstudio.util.GUITool;
import jstudio.util.Language;

@SuppressWarnings("serial")
public class ProductPanel extends EntityPanel<Product> {
	
	private Invoice invoice;
	private EntityManagerPanel<Invoice> accountingManager;
	
	private JTextField 
		descriptionField, 
		quantityField,
		costField;
	private JButton okButton, cancelButton, deleteButton;

	public ProductPanel(Product product, EntityManagerPanel<Product> manager, Invoice invoice, EntityManagerPanel<Invoice> accountingManager){
		super(product, manager);
		boolean editable = invoice!=null;
		this.invoice = invoice;
		this.accountingManager = accountingManager;
		
		NicePanel panel = new NicePanel(product.getDescription(),editable?Language.string("Edit details"):Language.string("View details"));
		panel.getBody().setLayout(new GridBagLayout());
		this.setLayout(new BorderLayout());
		this.add(panel, BorderLayout.CENTER);
		
		GridBagConstraints gc = new GridBagConstraints();
		gc.gridx=0;
		gc.gridy=0;
		gc.insets= new Insets(4,4,4,4);
		
		descriptionField = GUITool.createField(panel.getBody(), gc, Language.string("Description"), this.entity.getDescription(), editable);
		quantityField = GUITool.createField(panel.getBody(), gc, Language.string("Quantity"), Integer.toString(this.entity.getQuantity()), editable);
		costField = GUITool.createField(panel.getBody(), gc, Language.string("Cost"), Float.toString(this.entity.getCost()), editable);	
		
		if(editable){
			if(product.getId()>0l){
				deleteButton = new JButton(Language.string("Delete"));
				deleteButton.addActionListener(this);
				panel.addButton(deleteButton);
			}
			panel.addButtonsGlue();
			okButton = new JButton(Language.string("Ok"));
			okButton.addActionListener(this);
			panel.addButton(okButton);
			cancelButton = new JButton(Language.string("Cancel"));
			cancelButton.addActionListener(this);
			panel.addButton(cancelButton);
		}
	}

	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		if(o==okButton){
			try{
				entity.setQuantity(Integer.parseInt(quantityField.getText()));
				entity.setCost(Float.parseFloat(costField.getText()));
				entity.setDescription(descriptionField.getText());
				if(!invoice.getProducts().contains(entity)){
					invoice.getProducts().add(entity);
				}
				//TODO: check modified
				invoice.setModified(true);
				//accountingManager.getController().store(invoice);
				getDialog().dispose();
				//accountingManager.refresh();
				manager.refresh();
			}catch(NumberFormatException ex){
				String msg = Language.string("A number inserted has a bad format");
				JOptionPane.showMessageDialog(this, msg, Language.string("Wrong value"), JOptionPane.ERROR_MESSAGE);
			}
		}else if(o==cancelButton){
			getDialog().dispose();
		}else if(o==deleteButton){
			int ch = JOptionPane.showConfirmDialog(super.manager, 
					Language.string("Are you sure you want to remove {0}?", entity.getDescription()),
					Language.string("Remove product?"), 
					JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if(ch==JOptionPane.YES_OPTION){
				controller.delete(entity);
				getDialog().dispose();
				manager.refresh();
			}
		}
	}
}
