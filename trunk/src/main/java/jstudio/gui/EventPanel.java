package jstudio.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;

import javax.swing.JTextField;

import jstudio.control.Controller;
import jstudio.gui.generic.EntityPanel;
import jstudio.model.Event;
import jstudio.util.GUITool;
import jstudio.util.Language;

@SuppressWarnings("serial")
public class EventPanel extends EntityPanel<Event> {
	
	private JTextField 
		dateField, 
		nameField,
		lastnameField,
		phoneField,
		descriptionField;

	public EventPanel(Event event, Controller<Event> controller){
		super(event, controller);
		boolean editable = controller!=null;
		
		this.setLayout(new GridBagLayout());
		GridBagConstraints gc = new GridBagConstraints();
		gc.gridx=0;
		gc.gridy=0;
		gc.insets= new Insets(4,4,4,4);
		
		dateField = GUITool.createField(this, gc, 
				Language.string("Date"), 
				Event.timeFormat.format(this.entity.getDate()), editable);
		//TODO: add persons browse button
		nameField = GUITool.createField(this, gc, 
				Language.string("Name"), 
				this.entity.getName(), editable);
		lastnameField = GUITool.createField(this, gc,
				Language.string("Lastname"),
				this.entity.getLastname(), editable);
		descriptionField = GUITool.createField(this, gc, 
				Language.string("Description"), 
				this.entity.getDescription(), editable);
		phoneField = GUITool.createField(this, gc,
				Language.string("Phone"),
				this.entity.getPhone(), editable);
	}

	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		//TODO: ok, cancel, edit
	}
}
