package jstudio.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import jstudio.gui.generic.EntityManagerPanel;
import jstudio.gui.generic.EntityPanel;
import jstudio.model.Event;
import jstudio.model.Person;
import jstudio.util.GUITool;
import jstudio.util.Language;

@SuppressWarnings("serial")
public class EventPanel extends EntityPanel<Event> {
	
	private JTextField 
		dateField, 
		timeField,
		nameField,
		lastnameField,
		phoneField,
		descriptionField;
	private JButton okButton, cancelButton;
	private JButton pickPersonButton;

	public EventPanel(Event event, EntityManagerPanel<Event> manager, boolean editable){
		super(event, manager);
		
		this.setLayout(new GridBagLayout());
		GridBagConstraints gc = new GridBagConstraints();
		gc.gridx=0;
		gc.gridy=0;
		gc.insets= new Insets(4,4,4,4);
		dateField = GUITool.createDateField(this, gc, 
				Language.string("Date"), 
				Person.birthdateFormat.format(this.entity.getDate()), editable,
				Person.birthdateFormat);
		timeField = GUITool.createField(this, gc, 
				Language.string("Time"), 
				Event.timeFormat.format(this.entity.getDate()), editable);
		if(editable){
			pickPersonButton = GUITool.createButton(this, gc, 
				Language.string("Pick"), this);
		}
		nameField = GUITool.createField(this, gc, 
				Language.string("Name"), 
				this.entity.getName(), editable);
		lastnameField = GUITool.createField(this, gc,
				Language.string("Lastname"),
				this.entity.getLastname(), editable);
		phoneField = GUITool.createField(this, gc,
				Language.string("Phone"),
				this.entity.getPhone(), editable);
		descriptionField = GUITool.createField(this, gc, 
				Language.string("Description"), 
				this.entity.getDescription(), editable);
		
		if(editable){
			okButton = GUITool.createButton(this, gc, Language.string("Ok"), this);
			cancelButton = GUITool.createButton(this, gc, Language.string("Cancel"), this);
		}
	}

	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		if(o==pickPersonButton){
			PersonSelectionPanel psp = new PersonSelectionPanel(controller.getApplication().getAddressBook());
			psp.showDialog(this, nameField.getText()+" "+lastnameField.getText());
			Person p = psp.getSelected();
			if(p!=null){
				nameField.setText(p.getName());
				lastnameField.setText(p.getLastname());
				phoneField.setText(p.getPhone());
			}
		}else if(o==okButton){
			Calendar cd = Calendar.getInstance();
			Calendar ct = Calendar.getInstance();
			try {
				Date d = Person.birthdateFormat.parse(dateField.getText());
				cd.setTime(d);
			} catch (ParseException e1) {
				String msg = Language.string("Wrong date format for {0}, expected {1}",dateField.getText(),Person.birthdateFormat.toPattern());
				JOptionPane.showMessageDialog(this, msg, Language.string("Date format error"),JOptionPane.ERROR_MESSAGE);
				return;
			}
			try {
				Date t = Event.timeFormat.parse(timeField.getText());
				ct.setTime(t);
			} catch (ParseException e1) {
				String msg = Language.string("Wrong date format for {0}, expected {1}",timeField.getText(),Event.timeFormat.toPattern());
				JOptionPane.showMessageDialog(this, msg, Language.string("Date format error"),JOptionPane.ERROR_MESSAGE);
				return;
			}
			cd.set(Calendar.HOUR_OF_DAY, ct.get(Calendar.HOUR_OF_DAY));
			cd.set(Calendar.MINUTE, ct.get(Calendar.MINUTE));
			cd.set(Calendar.SECOND, 0);
			if(entity.getId()==0) entity.setId(controller.getNextId());
			entity.setDate(cd.getTime());
			entity.setName(nameField.getText());
			entity.setLastname(lastnameField.getText());
			entity.setPhone(phoneField.getText());
			entity.setDescription(descriptionField.getText());
			controller.store(entity);
			getDialog().dispose();
			manager.refresh();
		}else if(o==cancelButton){
			getDialog().dispose();
		}
	}
}
