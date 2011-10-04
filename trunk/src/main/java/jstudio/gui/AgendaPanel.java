package jstudio.gui;

import java.awt.BorderLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

import jstudio.control.Agenda;
import jstudio.control.Controller;
import jstudio.gui.generic.EntityManagerPanel;
import jstudio.gui.generic.PopupListener;
import jstudio.model.Event;
import jstudio.report.ReportGenerator;
import jstudio.report.ReportGeneratorGUI;
import jstudio.util.Configuration;
import jstudio.util.DatePicker;
import jstudio.util.Language;
import jstudio.util.Resources;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public class AgendaPanel 
		extends EntityManagerPanel<Event> {
	
	//time format for event entries
	public static final SimpleDateFormat 
		timestampFormat = new SimpleDateFormat("yyyyMMdd"),
		dateFormat = new SimpleDateFormat("EEEE dd MMMM yyyy");
	private static final Logger logger = LoggerFactory.getLogger(AgendaPanel.class);
	
	public static final String 
		PIC_AGENDA="eventicon.png",
		AGENDA_REPORT = "report.agenda",
		AGENDA_REPORT_DEF = "/reports/day.jasper";
	
	private JButton dateButton, refreshButton, printButton;

	public AgendaPanel(Controller<Event> controller){
		super(controller);
		this.setLayout(new BorderLayout());

		table = new JTable();
		model = new AgendaTableModel(table);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

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
		//TODO: add other days of the week

		JToolBar actionPanel = new JToolBar(Language.string("Actions"));
		actionPanel.setFloatable(false);
		refreshButton = new JButton(Language.string("Refresh"));
		refreshButton.addActionListener(this);
		actionPanel.add(refreshButton);
		printButton = new JButton(Language.string("Print"));
		printButton.addActionListener(this);
		actionPanel.add(printButton);
		filterField = new JTextField();
		filterField.addKeyListener(this);
		//filterField.addActionListener(this);
		actionPanel.add(filterField);
		topPanel.add(actionPanel, BorderLayout.CENTER);
		
		this.add(topPanel, BorderLayout.NORTH);
		
		this.popup = new EventPopup(this);
		scrollpane.addMouseListener(this);
		table.addMouseListener(this);
		table.addMouseListener(new PopupListener<Event>(table, popup));
	}
	
	public String getLabel(){
		return Language.string("Agenda");
	}
	
	public ImageIcon getIcon(){
		return Resources.getImage(PIC_AGENDA);
	}
	
	public void setDate(Date date){
		if(date==null) date = new Date();
		dateButton.setText(dateFormat.format(date));
	}
	
	public Date getDate(){
		try {
			return dateFormat.parse(dateButton.getText());
		} catch (ParseException e) {
			return new Date();
		}
	}
	
	public void showEntity(Event e){
		JDialog dialog = new EventPanel(e,this,false).createDialog(this.getTopLevelAncestor());
		dialog.setVisible(true);
	}

	public synchronized void addEntity(Event e){
		model.addRow(new Object[]{
				e,
				e.getLastname()+" "+e.getName(),
				e.getDescription()
		});
	}

	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		if(o==refreshButton){
			refresh();
		}else if(o==dateButton){
			DatePicker dp = new DatePicker(this);
			dp.setDate(this.getDate());
			Date d = dp.getDate();
			if(d!=null){
				this.setDate(d);
			}
			refresh();
		}else if(o==printButton){
			ReportGenerator rg = new ReportGenerator();
			rg.setReport(Configuration.getGlobal(AGENDA_REPORT,AGENDA_REPORT_DEF));
			rg.setHeadValue("day", dateFormat.format(getDate()));
			Set<Event> events = new HashSet<Event>();
			for(int i=0; i<model.getRowCount(); i++){
				events.add((Event)model.getValueAt(i, 0));
			}
			rg.setData(events);
			ReportGeneratorGUI rggui = new ReportGeneratorGUI(rg,"day_"+timestampFormat.format(getDate()));
			rggui.showGUI((Window)SwingUtilities.getRoot(this));
		}else{
			logger.warn("Event source not mapped: "+o);
		}
	}

	@Override
	public void refresh() {
		this.clear();
		Collection<Event> list = ((Agenda)controller).getByDate(getDate());
		if(list!=null){
			for(Event e: list){
				this.addEntity(e);
			}
		}else{
			JOptionPane.showMessageDialog(this, Language.string("Unable to load events"),Language.string("Database error"),JOptionPane.ERROR_MESSAGE);
		}
	}
	
	public void filter(String text){
		text = text.trim();
		this.clear();
		String[] vals = text.split(" ");
		String[] cols = new String[]{
				"name",
				"lastname"};
		Map<String,String> constraints = new HashMap<String,String>();
		constraints.put("date", Agenda.dayDateFormat.format(getDate()));
		Collection<Event> ts = ((Agenda)controller).findAll(vals, cols, constraints);
		logger.debug("Filtering by "+text+" returned "+ts);
		if(ts!=null){
			for(Event t: ts){
				this.addEntity(t);
			}
		}else{
			JOptionPane.showMessageDialog(this, Language.string("Unable to load data"),Language.string("Database error"),JOptionPane.ERROR_MESSAGE);
		}
	}
}
