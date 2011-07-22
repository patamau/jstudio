package jstudio.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import jstudio.model.Event;
import jstudio.model.Person;
import jstudio.util.Language;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public class AgendaPanel 
		extends JPanel 
		implements ListSelectionListener, ActionListener {
	
	//time format for event entries
	public static final SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm");
	private static final Logger logger = LoggerFactory.getLogger(AgendaPanel.class);
	
	private DefaultTableModel model;
	private JTable table;
	private JButton refreshButton;
	private JTextField filterField;
	private JStudioGUI gui;

	public AgendaPanel(JStudioGUI gui){
		this.gui = gui;
		this.setLayout(new BorderLayout());

		table = new JTable();
		model = new EventsTableModel(table);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.getSelectionModel().addListSelectionListener(this);

		JScrollPane scrollpane = new JScrollPane(table);
		//scrollpane.setPreferredSize(new Dimension(this.getWidth(),this.getHeight()));
		this.add(scrollpane, BorderLayout.CENTER);

		JToolBar actionPanel = new JToolBar(Language.string("Actions"));
		actionPanel.setFloatable(false);
		refreshButton = new JButton(Language.string("Refresh"));
		refreshButton.addActionListener(this);
		actionPanel.add(refreshButton);
		filterField = new JTextField();
		filterField.addActionListener(this);
		actionPanel.add(filterField);
		this.add(actionPanel, BorderLayout.NORTH);
		
		table.addMouseListener(new PopupListener<Event>(table, new EventPopup(this.gui)));
	}
	
	public void valueChanged(ListSelectionEvent event) {
        int viewRow = table.getSelectedRow();
        if (viewRow >= 0) {
        	//what to do when selected
        }
    }
	
	public void setSelectedEvent(Event e){
		//splitPane.setRightComponent(new EventPanel(e, false));
	}
	
	public synchronized void clear(){
		while(model.getRowCount()>0) model.removeRow(0);
	}

	public synchronized void addEvent(Event e){
		String p = e.getPerson()!=null?
				e.getPerson().getName()+" "+e.getPerson().getLastname():
				e.getAltPerson();
		model.addRow(new Object[]{
				e,
				p,
				e.getDescription()
		});
	}
	
	public synchronized void removeEvent(int id){
		Event e;
		int frow = -1;
		for(int i=0; i<model.getRowCount(); i++){
			e = (Event)model.getValueAt(i, 0);
			if(e.getId()==id){
				frow = i;
				break;
			}
		}
		if(frow>-1){
			model.removeRow(frow);
		}
	}

	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		if(o==refreshButton){
			gui.loadEvents(new Date());
		}else{
			logger.warn("Event source not mapped: "+o);
		}
	}
}
