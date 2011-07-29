package jstudio.gui;

import java.awt.BorderLayout;
import java.awt.Component;
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
import jstudio.model.Treatment;
import jstudio.util.GUITool;
import jstudio.util.Language;

@SuppressWarnings("serial")
public class TreatmentPanel extends JPanel implements ActionListener {
	
	private static JDialog dialog;
	private Treatment treatment;
	private Controller<Treatment> controller;
	private JTextField 
		descriptionField, 
		quantityField,
		costField;
	private JButton okButton, cancelButton;

	public TreatmentPanel(Treatment treatment, Controller<Treatment> _controller){
		this.treatment = treatment;
		this.controller = _controller;
		boolean editable = _controller!=null;
		
		this.setLayout(new GridBagLayout());
		GridBagConstraints gc = new GridBagConstraints();
		gc.gridx=0;
		gc.gridy=0;
		gc.insets= new Insets(4,4,4,4);
		
		descriptionField = GUITool.createField(this, gc, Language.string("Description"), this.treatment.getDescription(), editable);
		quantityField = GUITool.createField(this, gc, Language.string("Quantity"), Integer.toString(this.treatment.getQuantity()), editable);
		costField = GUITool.createField(this, gc, Language.string("Cost"), Float.toString(this.treatment.getCost()), editable);		
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
	public static JDialog createDialog(JFrame parent, Treatment p, Controller<Treatment> controller){
		if(dialog==null){
			dialog = new JDialog(parent);
			dialog.setTitle(Language.string("Treatment dialog"));
			dialog.getContentPane().setLayout(new BorderLayout());
		}
		dialog.setLocationRelativeTo(parent);
		dialog.setModal(controller!=null);
		dialog.getContentPane().removeAll();
		dialog.getContentPane().add(new TreatmentPanel(p, controller),BorderLayout.CENTER);
		dialog.pack();
		return dialog;
	}

	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		if(o==okButton){
			try{
				treatment.setQuantity(Integer.parseInt(quantityField.getText()));
				treatment.setCost(Float.parseFloat(costField.getText()));
				treatment.setDescription(descriptionField.getText());
				controller.store(treatment);
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
