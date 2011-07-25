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

import jstudio.model.Event;
import jstudio.util.GUITool;
import jstudio.util.Language;

@SuppressWarnings("serial")
public class EventPanel extends JPanel implements ActionListener {
	
	private static JDialog dialog;
	private Event event;
	private JTextField 
		dateField, 
		nameField,
		lastnameField,
		phoneField,
		descriptionField;

	public EventPanel(Event event, boolean editable){
		this.event = event;
		
		this.setLayout(new GridBagLayout());
		GridBagConstraints gc = new GridBagConstraints();
		gc.gridx=0;
		gc.gridy=0;
		gc.insets= new Insets(4,4,4,4);
		
		dateField = GUITool.createField(this, gc, 
				Language.string("Date"), 
				Event.timeFormat.format(this.event.getDate()), editable);
		//TODO: add persons browse button
		nameField = GUITool.createField(this, gc, 
				Language.string("Name"), 
				this.event.getName(), editable);
		lastnameField = GUITool.createField(this, gc,
				Language.string("Lastname"),
				this.event.getLastname(), editable);
		descriptionField = GUITool.createField(this, gc, 
				Language.string("Description"), 
				this.event.getDescription(), editable);
		phoneField = GUITool.createField(this, gc,
				Language.string("Phone"),
				this.event.getPhone(), editable);
	}
	
	/**
	 * Creates a specific dialog for the person,
	 * handling specifically changes, removal and additional links
	 * The dialog is modal if it is editable
	 * @param parent
	 * @return
	 */
	public static JDialog createDialog(JFrame parent, Event e, boolean editable){
		if(dialog==null){
			dialog = new JDialog(parent);
			dialog.setTitle(Language.string("Event dialog"));
			dialog.getContentPane().setLayout(new BorderLayout());
		}
		dialog.setLocationRelativeTo(parent);
		dialog.setModal(editable);
		dialog.getContentPane().removeAll();
		dialog.getContentPane().add(new EventPanel(e, editable),BorderLayout.CENTER);
		dialog.pack();
		return dialog;
	}

	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		//TODO: ok, cancel, edit
	}
}
