package jstudio.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;
import java.util.Calendar;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import jstudio.control.Controller;
import jstudio.model.Person;
import jstudio.util.CodeGenerator;
import jstudio.util.GUITool;
import jstudio.util.Language;

@SuppressWarnings("serial")
public class PersonPanel extends JPanel implements ActionListener {
	
	private static JDialog dialog;
	private Person person;
	private Controller<Person> controller;
	private JComboBox 
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
		this.person = person;
		this.controller = _controller;
		boolean editable = _controller!=null;
		
		this.setLayout(new GridBagLayout());
		GridBagConstraints gc = new GridBagConstraints();
		gc.gridx=0;
		gc.gridy=0;
		gc.insets= new Insets(4,4,4,4);
		
		nameField = GUITool.createField(this, gc, Language.string("Name"), this.person.getName(), editable);
		lastnameField = GUITool.createField(this, gc, Language.string("Lastname"), this.person.getLastname(), editable);
		genderBox = GUITool.createCombo(this, gc, Language.string("Gender"), this.person.getGender(), Person.Gender.values(), editable);
		birthdateField = GUITool.createDateField(this, gc, Language.string("Birthdate"), Person.birthdateFormat.format(this.person.getBirthdate()), editable, Person.birthdateFormat);
		addressField = GUITool.createField(this, gc, Language.string("Address"), this.person.getAddress(), editable);
		cityField = GUITool.createField(this, gc, Language.string("City"), this.person.getCity(), editable);
		capField = GUITool.createField(this, gc, Language.string("CAP"), this.person.getCap(), editable);
		codeField = GUITool.createField(this, gc, Language.string("Code"), this.person.getCode(), editable);
		if(editable){
			String blabel = Language.string("Generate");
			JButton b = new JButton(blabel);
			int swidth = b.getFontMetrics(b.getFont()).stringWidth(blabel);
			b.setPreferredSize(new Dimension(swidth+40,20));
			b.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					boolean male = genderBox.getSelectedIndex()==Person.Gender.Male.getId();
					String pv = "TN"; //TODO: add province
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
		phoneField = GUITool.createField(this, gc, Language.string("Phone"), this.person.getPhone(), editable);
		
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
	public static JDialog createDialog(JFrame parent, Person p, Controller<Person> controller){
		if(dialog==null){
			dialog = new JDialog(parent);
			dialog.setTitle(Language.string("Person dialog"));
			dialog.getContentPane().setLayout(new BorderLayout());
		}
		dialog.setLocationRelativeTo(parent);
		dialog.setModal(controller!=null);
		dialog.getContentPane().removeAll();
		dialog.getContentPane().add(new PersonPanel(p, controller),BorderLayout.CENTER);
		dialog.pack();
		return dialog;
	}

	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		if(o==okButton){
			try {
				person.setBirthdate(Person.birthdateFormat.parse(birthdateField.getText()));
			} catch (ParseException e1) {
				String msg = Language.string("Wrong date format for {0}, expected {1}",birthdateField.getText(),Person.birthdateFormat.toPattern());
				JOptionPane.showMessageDialog(this, msg, Language.string("Date format error"),JOptionPane.ERROR_MESSAGE);
				return;
			}
			person.setName(nameField.getText());
			person.setLastname(lastnameField.getText());
			person.setAddress(addressField.getText());
			person.setCity(cityField.getText());
			person.setCap(capField.getText());		
			person.setCode(codeField.getText());
			person.setPhone(phoneField.getText());
			person.setGender(genderBox.getSelectedIndex());
			controller.store(person);
			dialog.dispose();
		}else if(o==cancelButton){
			dialog.dispose();
		}
	}
}
