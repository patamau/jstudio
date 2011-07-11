package jstudio.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JPanel;
import javax.swing.JTextField;

import jstudio.model.Person;
import jstudio.util.GUITool;
import jstudio.util.Language;

public class PersonPanel extends JPanel {
	
	private Person person;
	
	private JTextField nameField, surnameField;

	public PersonPanel(Person person, boolean editable){
		this.person = person;
		
		this.setLayout(new GridBagLayout());
		GridBagConstraints gc = new GridBagConstraints();
		gc.gridx=0;
		gc.gridy=0;
		gc.insets= new Insets(4,4,4,4);
		
		nameField = GUITool.createField(this, gc, Language.string("Name"), this.person.getName(), editable);
		surnameField = GUITool.createField(this, gc, Language.string("Surname"), this.person.getLastname(), editable);
	}
}
