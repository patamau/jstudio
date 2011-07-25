package jstudio.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JDialog;
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
import jstudio.util.DatePicker;
import jstudio.util.Language;
import jstudio.util.PopupListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public class AgendaPanel 
		extends JPanel 
		implements ListSelectionListener, ActionListener {
	
	//time format for event entries
	public static final SimpleDateFormat 
		timeFormat = new SimpleDateFormat("hh:mm"),
		dateFormat = new SimpleDateFormat("EEEE dd MMMM yyyy");
	private static final Logger logger = LoggerFactory.getLogger(AgendaPanel.class);
	
	private DefaultTableModel model;
	private JTable table;
	private JButton dateButton;
	private JButton refreshButton;
	private JTextField filterField;
	private JStudioGUI gui;
	
	// internally used to catch a double click on the table
	private int lastSelectedRow = -1;
	private long lastSelectionTime = 0;

	public AgendaPanel(JStudioGUI gui){
		this.gui = gui;
		this.setLayout(new BorderLayout());

		table = new JTable();
		model = new AgendaTableModel(table);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.getSelectionModel().addListSelectionListener(this);

		JScrollPane scrollpane = new JScrollPane(table);
		//scrollpane.setPreferredSize(new Dimension(this.getWidth(),this.getHeight()));
		this.add(scrollpane, BorderLayout.CENTER);
		
		JPanel topPanel = new JPanel(new BorderLayout());
		
		JToolBar datePanel = new JToolBar(Language.string("Actions"));
		datePanel.setFloatable(false);
		dateButton = new JButton("");
		dateButton.addActionListener(this);
		datePanel.add(dateButton);
		setDate(new Date());
		topPanel.add(datePanel, BorderLayout.NORTH);

		JToolBar actionPanel = new JToolBar(Language.string("Actions"));
		actionPanel.setFloatable(false);
		refreshButton = new JButton(Language.string("Refresh"));
		refreshButton.addActionListener(this);
		actionPanel.add(refreshButton);
		filterField = new JTextField();
		filterField.addActionListener(this);
		actionPanel.add(filterField);
		topPanel.add(actionPanel, BorderLayout.CENTER);
		
		this.add(topPanel, BorderLayout.NORTH);
		
		table.addMouseListener(new PopupListener<Event>(table, new EventPopup(this.gui)));
	}
	
	public void setDate(Date date){
		dateButton.setText(dateFormat.format(date));
	}
	
	public Date getDate(){
		try {
			return dateFormat.parse(dateButton.getText());
		} catch (ParseException e) {
			return new Date();
		}
	}
	
	public void valueChanged(ListSelectionEvent event) {
        int viewRow = table.getSelectedRow();
        if (0<=viewRow){        
        	if(viewRow==lastSelectedRow&&
        			200>(System.currentTimeMillis()-lastSelectionTime)){
        		showEvent((Event)table.getValueAt(viewRow, 0));	
        		table.getSelectionModel().removeSelectionInterval(viewRow, viewRow);
        		lastSelectedRow = -1;
        	}else{
            	lastSelectedRow = viewRow;
            	lastSelectionTime = System.currentTimeMillis();
        		table.getSelectionModel().removeSelectionInterval(viewRow, viewRow);
        	}
        }
    }
	
	public void showEvent(Event e){
		JDialog dialog = EventPanel.createDialog(gui, e, false);
		dialog.setVisible(true);
	}
	
	public synchronized void clear(){
		while(model.getRowCount()>0) model.removeRow(0);
	}

	public synchronized void addEvent(Event e){
		model.addRow(new Object[]{
				e,
				e.getName()+" "+e.getLastname(),
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
			gui.loadEvents(getDate());
		}else if(o==dateButton){
			DatePicker dp = new DatePicker(this);
			dp.setDate(this.getDate());
			Date d = dp.getDate();
			this.setDate(d);
			gui.loadEvents(d);
		}else{
			logger.warn("Event source not mapped: "+o);
		}
	}
}
