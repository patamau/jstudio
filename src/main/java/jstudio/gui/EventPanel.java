package jstudio.gui;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import jstudio.gui.generic.EntityManagerPanel;
import jstudio.gui.generic.EntityPanel;
import jstudio.gui.generic.NicePanel;
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
		phoneField;
	private JTextArea descriptionArea;
	private JButton okButton, cancelButton, closeButton, deleteButton, editButton, viewButton;;
	private JButton pickPersonButton;

	public EventPanel(Event event, EntityManagerPanel<Event> manager, boolean editable){
		super(event, manager);
		
		NicePanel panel = new NicePanel(Event.timeFormat.format(event.getDate()),editable?Language.string("Edit details"):Language.string("View details"));
		panel.getBody().setLayout(new GridBagLayout());
		this.setLayout(new BorderLayout());
		this.add(panel, BorderLayout.CENTER);
		
		GridBagConstraints gc = new GridBagConstraints();
		gc.gridx=0;
		gc.gridy=0;
		gc.insets= new Insets(4,4,4,4);
		dateField = GUITool.createDateField(panel.getBody(), gc, 
				Language.string("Date"), 
				this.entity.getDate(), editable,
				Person.birthdateFormat);
		timeField = GUITool.createField(panel.getBody(), gc, 
				Language.string("Time"), 
				Event.timeFormat.format(this.entity.getDate()), editable);
		if(editable){
			pickPersonButton = GUITool.createButton(panel.getBody(), gc, 
				Language.string("Pick"), this);
		}
		nameField = GUITool.createField(panel.getBody(), gc, 
				Language.string("Name"), 
				this.entity.getName(), editable);
		lastnameField = GUITool.createField(panel.getBody(), gc,
				Language.string("Lastname"),
				this.entity.getLastname(), editable);
		phoneField = GUITool.createField(panel.getBody(), gc,
				Language.string("Phone"),
				this.entity.getPhone(), editable);
		descriptionArea = GUITool.createArea(panel.getBody(), gc, 
				Language.string("Description"), 
				this.entity.getDescription(), editable);
		
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
		Calendar cd = Calendar.getInstance();
		Calendar ct = Calendar.getInstance();
		try {
			Date d = Person.birthdateFormat.parse(dateField.getText());
			cd.setTime(d);
		} catch (ParseException e1) {
			String msg = Language.string("Wrong date format for {0}, expected {1}",dateField.getText(),Person.birthdateFormat.toPattern());
			JOptionPane.showMessageDialog(this, msg, Language.string("Date format error"),JOptionPane.ERROR_MESSAGE);
			return false;
		}
		try {
			Date t = Event.timeFormat.parse(timeField.getText());
			ct.setTime(t);
		} catch (ParseException e1) {
			String msg = Language.string("Wrong date format for {0}, expected {1}",timeField.getText(),Event.timeFormat.toPattern());
			JOptionPane.showMessageDialog(this, msg, Language.string("Date format error"),JOptionPane.ERROR_MESSAGE);
			return false;
		}
		cd.set(Calendar.HOUR_OF_DAY, ct.get(Calendar.HOUR_OF_DAY));
		cd.set(Calendar.MINUTE, ct.get(Calendar.MINUTE));
		cd.set(Calendar.SECOND, 0);
		if(entity.getId()==0) entity.setId(controller.getNextId());
		entity.setDate(cd.getTime());
		entity.setName(nameField.getText());
		entity.setLastname(lastnameField.getText());
		entity.setPhone(phoneField.getText());
		entity.setDescription(descriptionArea.getText());
		controller.store(entity);
		return true;
	}
	
	private boolean checkModified(){
		if(!entity.getName().equals(nameField.getText())) return true;
		if(!entity.getLastname().equals(lastnameField.getText())) return true;
		if(!entity.getDescription().equals(descriptionArea.getText())) return true;
		if(!Person.birthdateFormat.format(entity.getDate()).equals(dateField.getText())) return true;
		if(!Event.timeFormat.format(entity.getDate()).equals(timeField.getText())) return true;
		if(!entity.getPhone().equals(phoneField.getText())) return true;
		return false;
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
			if(applyChanges()){
				getDialog().dispose();
				manager.refresh();
			}
		}else if(o==cancelButton||o==closeButton){
			getDialog().dispose();
		}else if(o==editButton){
			getDialog().dispose();
			JDialog dialog = new EventPanel(super.entity, super.manager, true).createDialog(super.manager.getTopLevelAncestor());
			dialog.setVisible(true);
		}else if(o==deleteButton){
			String msg = entity.getDescription();
			if(msg.length()>50){
				msg = msg.substring(0, 50)+"...";
			}
			int ch = JOptionPane.showConfirmDialog(super.manager, 
					Language.string("Are you sure you want to remove \"{0}\" at {1}?",
							msg, Event.timeFormat.format(entity.getDate())),
					Language.string("Remove invoice?"), 
					JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if(ch==JOptionPane.YES_OPTION){
				controller.delete(entity);
				getDialog().dispose();
				manager.refresh();
			}
		}else if(o==viewButton){
			if(checkModified()){
				int ch = JOptionPane.showConfirmDialog(this, 
						Language.string("Apply changes to {0}?",Event.timeFormat.format(entity.getDate())), 
						Language.string("Changes made"), JOptionPane.YES_NO_CANCEL_OPTION);
				if(ch==JOptionPane.CANCEL_OPTION) return;
				if(ch==JOptionPane.YES_OPTION){
					if(!applyChanges()) return;
				}
			}
			getDialog().dispose();
			JDialog dialog = new EventPanel(super.entity, super.manager, false).createDialog(super.manager.getTopLevelAncestor());
			dialog.setVisible(true);
		}
	}
}
