package jstudio.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;
import java.util.Calendar;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import jstudio.control.Controller;
import jstudio.gui.generic.EntityPanel;
import jstudio.model.Person;
import jstudio.util.CodeGenerator;
import jstudio.util.GUITool;
import jstudio.util.Language;

@SuppressWarnings("serial")
public class PersonPanel extends EntityPanel<Person> {
	
	private JComboBox
		provinceBox,
		genderBox;
	private JTextField 
		nameField, 
		lastnameField,
		birthdateField,
		addressField,
		cityField,
		capField,
		codeField,
		phoneField;
	private JButton okButton, cancelButton;

	public PersonPanel(Person person, Controller<Person> _controller){
		super(person, _controller);
		boolean editable = _controller!=null;
		
		this.setLayout(new GridBagLayout());
		GridBagConstraints gc = new GridBagConstraints();
		gc.gridx=0;
		gc.gridy=0;
		gc.insets= new Insets(4,4,4,4);
		
		nameField = GUITool.createField(this, gc, Language.string("Name"), this.entity.getName(), editable);
		lastnameField = GUITool.createField(this, gc, Language.string("Lastname"), this.entity.getLastname(), editable);
		genderBox = GUITool.createCombo(this, gc, Language.string("Gender"), this.entity.getGender(), Person.Gender.values(), editable);
		birthdateField = GUITool.createDateField(this, gc, Language.string("Birthdate"), Person.birthdateFormat.format(this.entity.getBirthdate()), editable, Person.birthdateFormat);
		addressField = GUITool.createField(this, gc, Language.string("Address"), this.entity.getAddress(), editable);
		cityField = GUITool.createField(this, gc, Language.string("City"), this.entity.getCity(), editable);
		if(controller!=null){
			List<String> pvs = controller.getApplication().getComuni().getProvinces();
			int sel = pvs==null?0:pvs.indexOf(this.entity.getProvince());
			if(sel<0) sel=0;
			provinceBox = GUITool.createCombo(this, gc, Language.string("Province"), sel, pvs.toArray(), editable);
		}else{
			provinceBox = GUITool.createCombo(this, gc, Language.string("Province"), 0, new Object[]{this.entity.getProvince()}, editable);
		}
		capField = GUITool.createField(this, gc, Language.string("CAP"), this.entity.getCap(), editable);
		codeField = GUITool.createField(this, gc, Language.string("Code"), this.entity.getCode(), editable);
		if(editable){
			String blabel = Language.string("Generate");
			JButton b = new JButton(blabel);
			int swidth = b.getFontMetrics(b.getFont()).stringWidth(blabel);
			b.setPreferredSize(new Dimension(swidth+40,20));
			b.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					boolean male = genderBox.getSelectedIndex()==Person.Gender.Male.getId();
					String pv = (String)provinceBox.getSelectedItem();
					String loc = controller.getApplication().getComuni().getCode(pv, cityField.getText());
					Calendar c = Calendar.getInstance();
					try{
						c.setTime(Person.birthdateFormat.parse(birthdateField.getText()));
					}catch(ParseException pe){
						c.set(Calendar.YEAR, 1);
						c.set(Calendar.MONTH, 1);
						c.set(Calendar.DAY_OF_YEAR, 1);
					}
					String code = CodeGenerator.generate(
							nameField.getText(), 
							lastnameField.getText(), 
							male, 
							c.get(Calendar.YEAR), 
							c.get(Calendar.MONTH)+1, 
							c.get(Calendar.DAY_OF_MONTH),
							loc
							);
					codeField.setText(code);
				}
			});
			gc.gridx+=2;
			this.add(b,gc);
			gc.gridx=0;
			gc.gridy++;
		}
		phoneField = GUITool.createField(this, gc, Language.string("Phone"), this.entity.getPhone(), editable);
		
		if(editable){
			okButton = GUITool.createButton(this, gc, Language.string("Ok"),this);
			cancelButton = GUITool.createButton(this, gc, Language.string("Cancel"),this);
		}
	}

	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		if(o==okButton){
			try {
				entity.setBirthdate(Person.birthdateFormat.parse(birthdateField.getText()));
			} catch (ParseException e1) {
				String msg = Language.string("Wrong date format for {0}, expected {1}",birthdateField.getText(),Person.birthdateFormat.toPattern());
				JOptionPane.showMessageDialog(this, msg, Language.string("Date format error"),JOptionPane.ERROR_MESSAGE);
				return;
			}
			entity.setName(nameField.getText());
			entity.setLastname(lastnameField.getText());
			entity.setAddress(addressField.getText());
			entity.setCity(cityField.getText());
			entity.setProvince((String)provinceBox.getSelectedItem());
			entity.setCap(capField.getText());		
			entity.setCode(codeField.getText());
			entity.setPhone(phoneField.getText());
			entity.setGender(genderBox.getSelectedIndex());
			controller.store(entity);
			getDialog().dispose();
		}else if(o==cancelButton){
			getDialog().dispose();
		}
	}
}
