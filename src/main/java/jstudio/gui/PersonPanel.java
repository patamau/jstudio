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
import javax.swing.JPanel;
import javax.swing.JTextField;

import jstudio.control.Controller;
import jstudio.model.Person;
import jstudio.util.GUITool;
import jstudio.util.Language;

@SuppressWarnings("serial")
public class PersonPanel extends JPanel implements ActionListener {
	
	private static JDialog dialog;
	private Person person;
	private Controller<Person> controller;
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

	public PersonPanel(Person person, Controller<Person> controller){
		this.person = person;
		this.controller = controller;
		boolean editable = controller!=null;
		
		this.setLayout(new GridBagLayout());
		GridBagConstraints gc = new GridBagConstraints();
		gc.gridx=0;
		gc.gridy=0;
		gc.insets= new Insets(4,4,4,4);
		
		nameField = GUITool.createField(this, gc, Language.string("Name"), this.person.getName(), editable);
		lastnameField = GUITool.createField(this, gc, Language.string("Lastname"), this.person.getLastname(), editable);
		birthdateField = GUITool.createDateField(this, gc, Language.string("Birthdate"), Person.birthdateFormat.format(this.person.getBirthdate()), editable, Person.birthdateFormat);
		addressField = GUITool.createField(this, gc, Language.string("Address"), this.person.getAddress(), editable);
		cityField = GUITool.createField(this, gc, Language.string("City"), this.person.getCity(), editable);
		capField = GUITool.createField(this, gc, Language.string("CAP"), this.person.getCap(), editable);
		codeField = GUITool.createField(this, gc, Language.string("Code"), this.person.getCode(), editable);
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
			person.setCity(cityField.getText());
			controller.store(person);
			dialog.dispose();
		}else if(o==cancelButton){
			dialog.dispose();
		}
	}
}
