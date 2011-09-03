package jstudio.gui;

import java.awt.Window;
import java.awt.event.MouseEvent;

import javax.swing.JDialog;
import javax.swing.SwingUtilities;

import jstudio.control.Controller;
import jstudio.model.Person;

@SuppressWarnings("serial")
public class PersonSelectionPanel extends AddressBookPanel {

	private Person selectedPerson;
	
	public PersonSelectionPanel(Controller<Person> controller) {
		super(controller);
	}
	
	public Person getSelected(){
		return selectedPerson;
	}
	
	public void mouseClicked(MouseEvent e){
		int row = table.rowAtPoint(e.getPoint());
		if(row>=0){
			selectedPerson = (Person)table.getValueAt(row, 0);
			if(e.getClickCount()==2){
				Window w = (Window)SwingUtilities.getRoot(this);
				w.dispose();
			}
		}
	}

	public void mouseReleased(MouseEvent e){
		//override default behavior with the empty one
	}
	
	public void showDialog(String filterText){
		JDialog ppdialog = new JDialog();
		ppdialog.setModal(true);
		if(filterText==null){
			refresh();
		}else{
			this.filterField.setText(filterText);
			filter(filterText);
		}
		ppdialog.add(this);
		ppdialog.pack();
		ppdialog.setLocationRelativeTo(this);
		ppdialog.setVisible(true);
	}
}
