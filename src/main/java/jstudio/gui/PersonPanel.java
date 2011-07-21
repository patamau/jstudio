package jstudio.gui;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

import jstudio.model.Person;
import jstudio.util.GUITool;
import jstudio.util.Language;

public class PersonPanel extends JPanel implements ActionListener {
	
	private static JDialog dialog;
	private Person person;
	private JTextField 
		nameField, 
		lastnameField,
		birthdateField,
		addressField,
		phoneField;

	public PersonPanel(Person person, boolean editable){
		this.person = person;
		
		this.setLayout(new GridBagLayout());
		GridBagConstraints gc = new GridBagConstraints();
		gc.gridx=0;
		gc.gridy=0;
		gc.insets= new Insets(4,4,4,4);
		
		nameField = GUITool.createField(this, gc, Language.string("Name"), this.person.getName(), editable);
		lastnameField = GUITool.createField(this, gc, Language.string("Lastname"), this.person.getLastname(), editable);
		birthdateField = GUITool.createField(this, gc, Language.string("Birthdate"), Person.birthdateFormat.format(this.person.getBirthdate()), editable);
		addressField = GUITool.createField(this, gc, Language.string("Address"), this.person.getAddress(), editable);
		phoneField = GUITool.createField(this, gc, Language.string("Phone"), this.person.getPhone(), editable);
	}
	
	/**
	 * Creates a specific dialog for the person,
	 * handling specifically changes, removal and additional links
	 * The dialog is modal if it is editable
	 * @param parent
	 * @return
	 */
	public static JDialog createDialog(JFrame parent, Person p, boolean editable){
		if(dialog==null){
			dialog = new JDialog(parent);
			dialog.setTitle(Language.string("Person dialog"));
			dialog.getContentPane().setLayout(new BorderLayout());
		}
		dialog.setModal(editable);
		dialog.getContentPane().removeAll();
		dialog.getContentPane().add(new PersonPanel(p, editable),BorderLayout.CENTER);
		dialog.pack();
		return dialog;
	}

	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		//TODO: ok, cancel, edit
	}
}
