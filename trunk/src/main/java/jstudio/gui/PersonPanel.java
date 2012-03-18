package jstudio.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.text.ParseException;
import java.util.Calendar;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import jstudio.gui.generic.EntityManagerPanel;
import jstudio.gui.generic.EntityPanel;
import jstudio.gui.generic.NicePanel;
import jstudio.model.Person;
import jstudio.util.CodeGenerator;
import jstudio.util.GUITool;
import jstudio.util.Language;

@SuppressWarnings("serial")
public class PersonPanel extends EntityPanel<Person> {
	
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
	private JButton generateButton;

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
		
		nameField = GUITool.createField(panel.getBody(), gc, Language.string("Name"), this.entity.getName(), editable);
		lastnameField = GUITool.createField(panel.getBody(), gc, Language.string("Lastname"), this.entity.getLastname(), editable);
		genderBox = GUITool.createCombo(panel.getBody(), gc, Language.string("Gender"), this.entity.getGender(), Person.Gender.values(), editable);
		birthdateField = GUITool.createDateField(panel.getBody(), gc, Language.string("Birthdate"), this.entity.getBirthdate(), editable, Person.birthdateFormat);
		addressField = GUITool.createField(panel.getBody(), gc, Language.string("Address"), this.entity.getAddress(), editable);
		cityField = GUITool.createField(panel.getBody(), gc, Language.string("City"), this.entity.getCity(), editable);
		/*
		if(controller!=null){
			List<String> pvs = controller.getApplication().getComuni().getProvinces();
			int sel = pvs==null?0:pvs.indexOf(this.entity.getProvince());
			if(sel<0) sel=0;
			provinceBox = GUITool.createCombo(panel.getBody(), gc, Language.string("Province"), sel, pvs.toArray(), editable);
		}else{
			provinceBox = GUITool.createCombo(panel.getBody(), gc, Language.string("Province"), 0, new Object[]{this.entity.getProvince()}, editable);
		}
		*/
		provinceField = GUITool.createProvinceField(panel.getBody(), gc, Language.string("Province"), this.entity.getProvince(), editable);
		capField = GUITool.createCAPField(panel.getBody(), gc, Language.string("CAP"), this.entity.getCap(), editable);
		codeField = GUITool.createField(panel.getBody(), gc, Language.string("Code"), this.entity.getCode(), editable);
		if(editable){
			String blabel = Language.string("Generate");
			generateButton = new JButton(blabel);
			int swidth = generateButton.getFontMetrics(generateButton.getFont()).stringWidth(blabel);
			generateButton.setPreferredSize(new Dimension(swidth+40,20));
			generateButton.addActionListener(this);
			gc.gridx+=2;
			panel.getBody().add(generateButton,gc);
			gc.gridx=0;
			gc.gridy++;
		}
		phoneField = GUITool.createField(panel.getBody(), gc, Language.string("Phone"), this.entity.getPhone(), editable);
		
		if(editable){
			deleteButton = new JButton(Language.string("Delete"));
			deleteButton.addActionListener(this);
			panel.addButton(deleteButton);
			viewButton = new JButton(Language.string("View"));
			viewButton.addActionListener(this);
			panel.addButton(viewButton);
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
	
	public String getCode(){
		boolean male = genderBox.getSelectedIndex()==Person.Gender.Male.getId();
		//String pv = (String)provinceBox.getSelectedItem();
		String pv = provinceField.getText();
		String loc = controller.getApplication().getComuni().getCode(pv, cityField.getText());
		Calendar c = Calendar.getInstance();
		try{
			c.setTime(Person.birthdateFormat.parse(birthdateField.getText()));
		}catch(ParseException pe){
			c.set(Calendar.YEAR, 1);
			c.set(Calendar.MONTH, 1);
			c.set(Calendar.DAY_OF_YEAR, 1);
		}
		return CodeGenerator.generate(
				nameField.getText(), 
				lastnameField.getText(), 
				male, 
				c.get(Calendar.YEAR), 
				c.get(Calendar.MONTH)+1, 
				c.get(Calendar.DAY_OF_MONTH),
				loc
				);
	}

	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		if(o==closeButton){
			getDialog().dispose();
		}else if(o==viewButton){
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
		}else if(o==generateButton){
			codeField.setText(getCode());
		}else if(o==okButton){
			try {
				entity.setBirthdate(Person.birthdateFormat.parse(birthdateField.getText()));
			} catch (ParseException e1) {
				String msg = Language.string("Wrong date format for {0}, expected {1}",birthdateField.getText(),Person.birthdateFormat.toPattern());
				JOptionPane.showMessageDialog(this, msg, Language.string("Date format error"),JOptionPane.ERROR_MESSAGE);
				return;
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
			getDialog().dispose();
			manager.refresh();
		}else if(o==cancelButton){
			getDialog().dispose();
		}
	}
}
