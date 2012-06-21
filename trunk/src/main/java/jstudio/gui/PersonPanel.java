package jstudio.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.text.ParseException;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import jstudio.gui.generic.EntityManagerPanel;
import jstudio.gui.generic.EntityPanel;
import jstudio.gui.generic.NicePanel;
import jstudio.model.Person;
import jstudio.util.GUITool;
import jstudio.util.Language;

@SuppressWarnings("serial")
public class PersonPanel extends EntityPanel<Person> {
	
	//private static final Logger logger = Logger.getLogger(PersonPanel.class);
	
	private JComboBox
		genderBox;
	private JTextField 
		nameField, 
		lastnameField,
		birthdateField,
		addressField,
		cityField,
		provinceField,
		capField,
		codeField,
		phoneField;
	private JButton okButton, cancelButton;
	private JButton viewButton, editButton, deleteButton;
	private JButton closeButton;

	public PersonPanel(Person person, EntityManagerPanel<Person> manager, boolean editable){
		super(person, manager);
		
		NicePanel panel = new NicePanel(person.getName()+" "+person.getLastname(),editable?Language.string("Edit details"):Language.string("View details"));
		panel.getBody().setLayout(new GridBagLayout());
		this.setLayout(new BorderLayout());
		this.add(panel, BorderLayout.CENTER);
		
		GridBagConstraints gc = new GridBagConstraints();
		gc.gridx=0;
		gc.gridy=0;
		gc.insets= new Insets(4,4,4,4);

		lastnameField = GUITool.createField(panel.getBody(), gc, Language.string("Lastname"), this.entity.getLastname(), editable);
		lastnameField.addActionListener(this);
		nameField = GUITool.createField(panel.getBody(), gc, Language.string("Name"), this.entity.getName(), editable);
		nameField.addActionListener(this);
		genderBox = GUITool.createCombo(panel.getBody(), gc, Language.string("Gender"), this.entity.getGender(), Person.Gender.values(), editable);
		genderBox.addActionListener(this);
		birthdateField = GUITool.createDateField(panel.getBody(), gc, Language.string("Birthdate"), this.entity.getBirthdate(), editable, Person.birthdateFormat);
		birthdateField.addActionListener(this);
		addressField = GUITool.createField(panel.getBody(), gc, Language.string("Address"), this.entity.getAddress(), editable);
		addressField.addActionListener(this);
		cityField = GUITool.createField(panel.getBody(), gc, Language.string("City"), this.entity.getCity(), editable);
		cityField.addActionListener(this);
		provinceField = GUITool.createProvinceField(panel.getBody(), gc, Language.string("Province"), this.entity.getProvince(), editable);
		provinceField.addActionListener(this);
		capField = GUITool.createCAPField(panel.getBody(), gc, Language.string("CAP"), this.entity.getCap(), editable);
		capField.addActionListener(this);
		codeField = GUITool.createCodeField(panel.getBody(), gc, Language.string("Code"), this.entity.getCode(), editable);
		codeField.addActionListener(this);
		phoneField = GUITool.createField(panel.getBody(), gc, Language.string("Phone"), this.entity.getPhone(), editable);
		phoneField.addActionListener(this);
		
		if(editable){
			if(entity.getId()>0){
				deleteButton = new JButton(Language.string("Delete"));
				deleteButton.addActionListener(this);
				panel.addButton(deleteButton);
				viewButton = new JButton(Language.string("View"));
				viewButton.addActionListener(this);
				panel.addButton(viewButton);
			}
			panel.addButtonsGlue();
			okButton = new JButton(Language.string("Ok"));
			okButton.addActionListener(this);
			panel.addButton(okButton);
			cancelButton = new JButton(Language.string("Cancel"));
			cancelButton.addActionListener(this);
			panel.addButton(cancelButton);
		}else{
			deleteButton = new JButton(Language.string("Delete"));
			deleteButton.addActionListener(this);
			panel.addButton(deleteButton);
			editButton = new JButton(Language.string("Edit"));
			editButton.addActionListener(this);
			panel.addButton(editButton);
			panel.addButtonsGlue();
			closeButton = new JButton(Language.string("Close"));
			closeButton.addActionListener(this);
			panel.addButton(closeButton);
		}
	}
	
	private boolean applyChanges(){
		try {
			entity.setBirthdate(Person.birthdateFormat.parse(birthdateField.getText()));
		} catch (ParseException e1) {
			String msg = Language.string("Wrong date format for {0}, expected {1}",birthdateField.getText(),Person.birthdateFormat.toPattern());
			JOptionPane.showMessageDialog(this, msg, Language.string("Date format error"),JOptionPane.ERROR_MESSAGE);
			return false;
		}
		if(entity.getId()==0) entity.setId(controller.getNextId());
		entity.setName(nameField.getText());
		entity.setLastname(lastnameField.getText());
		entity.setAddress(addressField.getText());
		entity.setCity(cityField.getText());
		//entity.setProvince((String)provinceBox.getSelectedItem());
		entity.setProvince(provinceField.getText());
		entity.setCap(capField.getText());		
		entity.setCode(codeField.getText());
		entity.setPhone(phoneField.getText());
		entity.setGender(genderBox.getSelectedIndex());
		controller.store(entity);
		return true;
	}
	
	private boolean checkModified(){
		if(!entity.getName().equals(nameField.getText())) return true;
		if(!entity.getLastname().equals(lastnameField.getText())) return true;
		if(!entity.getAddress().equals(addressField.getText())) return true;
		if(!Person.birthdateFormat.format(entity.getBirthdate()).equals(birthdateField.getText())) return true;
		if(!entity.getCap().equals(capField.getText())) return true;
		if(!entity.getCity().equals(cityField.getText())) return true;
		if(!entity.getCode().equals(codeField.getText())) return true;
		if(entity.getGender()!=genderBox.getSelectedIndex()) return true;
		if(!entity.getPhone().equals(phoneField.getText())) return true;
		if(!entity.getProvince().equals(provinceField.getText())) return true;
		return false;
	}

	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		if(o==closeButton){
			getDialog().dispose();
		}else if(o==viewButton){
			if(checkModified()){
				int ch = JOptionPane.showConfirmDialog(this, 
						Language.string("Apply changes to {0}?",entity.getName()+" "+entity.getLastname()), 
						Language.string("Changes made"), JOptionPane.YES_NO_CANCEL_OPTION);
				if(ch==JOptionPane.CANCEL_OPTION) return;
				if(ch==JOptionPane.YES_OPTION) applyChanges();
			}
			getDialog().dispose();
			JDialog dialog = new PersonPanel(super.entity, super.manager, false).createDialog(super.manager.getTopLevelAncestor());
			dialog.setVisible(true);
		}else if(o==editButton){
			getDialog().dispose();
			JDialog dialog = new PersonPanel(super.entity, super.manager, true).createDialog(super.manager.getTopLevelAncestor());
			dialog.setVisible(true);
		}else if(o==deleteButton){
			int ch = JOptionPane.showConfirmDialog(super.manager, 
					Language.string("Are you sure you want to remove {0} {1}?",
							entity.getName(),entity.getLastname()),
					Language.string("Remove person?"), 
					JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if(ch==JOptionPane.YES_OPTION){
				controller.delete(entity);
				getDialog().dispose();
				manager.refresh();
			}
		}else if(o==okButton){
			if(applyChanges()){
				getDialog().dispose();
				manager.refresh();
			}
		}else if(o==cancelButton){
			getDialog().dispose();
		}else{
			((Component)o).transferFocus();
		}
	}
}
