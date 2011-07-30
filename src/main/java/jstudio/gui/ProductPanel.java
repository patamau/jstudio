package jstudio.gui;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import jstudio.control.Controller;
import jstudio.model.Product;
import jstudio.util.GUITool;
import jstudio.util.Language;

@SuppressWarnings("serial")
public class ProductPanel extends JPanel implements ActionListener {
	
	private static JDialog dialog;
	private Product product;
	private Controller<Product> controller;
	private JTextField 
		descriptionField, 
		quantityField,
		costField;
	private JButton okButton, cancelButton;

	public ProductPanel(Product product, Controller<Product> _controller){
		this.product = product;
		this.controller = _controller;
		boolean editable = _controller!=null;
		
		this.setLayout(new GridBagLayout());
		GridBagConstraints gc = new GridBagConstraints();
		gc.gridx=0;
		gc.gridy=0;
		gc.insets= new Insets(4,4,4,4);
		
		descriptionField = GUITool.createField(this, gc, Language.string("Description"), this.product.getDescription(), editable);
		quantityField = GUITool.createField(this, gc, Language.string("Quantity"), Integer.toString(this.product.getQuantity()), editable);
		costField = GUITool.createField(this, gc, Language.string("Cost"), Float.toString(this.product.getCost()), editable);		
		if(editable){
			okButton = GUITool.createButton(this, gc, Language.string("Ok"),this);
			cancelButton = GUITool.createButton(this, gc, Language.string("Cancel"),this);
		}
	}
	
	/**
	 * Creates a specific dialog for the person,
	 * handling specifically changes, removal and additional links
	 * The dialog is modal if it is editable
	 * @param parent
	 * @return
	 */
	public static JDialog createDialog(JFrame parent, Product p, Controller<Product> controller){
		if(dialog==null){
			dialog = new JDialog(parent);
			dialog.setTitle(Language.string("Product dialog"));
			dialog.getContentPane().setLayout(new BorderLayout());
		}
		dialog.setLocationRelativeTo(parent);
		dialog.setModal(controller!=null);
		dialog.getContentPane().removeAll();
		dialog.getContentPane().add(new ProductPanel(p, controller),BorderLayout.CENTER);
		dialog.pack();
		return dialog;
	}

	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		if(o==okButton){
			try{
				product.setQuantity(Integer.parseInt(quantityField.getText()));
				product.setCost(Float.parseFloat(costField.getText()));
				product.setDescription(descriptionField.getText());
				controller.store(product);
				dialog.dispose();
			}catch(NumberFormatException ex){
				String msg = Language.string("An number inserted has a bad format");
				JOptionPane.showMessageDialog(this, msg, Language.string("Wrong value"), JOptionPane.ERROR_MESSAGE);
			}
		}else if(o==cancelButton){
			dialog.dispose();
		}
	}
}
