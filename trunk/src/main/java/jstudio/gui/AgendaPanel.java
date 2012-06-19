package jstudio.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
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
		dawFormat = new SimpleDateFormat("EEEE"),
		dayFormat = new SimpleDateFormat("dd/MM/yy");
	private static final Logger logger = LoggerFactory.getLogger(AgendaPanel.class);
	
	public static final String 
		PIC_AGENDA="eventicon.png",
		AGENDA_REPORT = "report.agenda",
		AGENDA_REPORT_DEF = "/reports/day.jasper";
	
	private JButton[] weekButtons = new JButton[7];
	private JButton printButton, nextWeekButton, prevWeekButton;
	private Calendar calendar = Calendar.getInstance();

	public AgendaPanel(Controller<Event> controller){
		super(controller);
		this.setLayout(new BorderLayout());
		model = new AgendaTableModel(table);

		JScrollPane scrollpane = new JScrollPane(table);
		scrollpane.addMouseListener(this);
		//scrollpane.setPreferredSize(new Dimension(this.getWidth(),this.getHeight()));
		this.add(scrollpane, BorderLayout.CENTER);
		
		JPanel topPanel = new JPanel(new BorderLayout());
		JPanel weekPanel = new JPanel(new GridBagLayout());
		GridBagConstraints gc = new GridBagConstraints();
		gc.gridx=0;
		gc.gridy=0;
		gc.weightx=1.0f;
		gc.weighty=1.0f;
		gc.fill=GridBagConstraints.BOTH;
		prevWeekButton = new JButton("<<");
		prevWeekButton.addActionListener(this);
		prevWeekButton.setMnemonic(KeyEvent.VK_LEFT);
		weekPanel.add(prevWeekButton,gc);
		gc.gridx++;
		for(int i=0; i<weekButtons.length; ++i){
			weekButtons[i] = new JButton("");
			weekButtons[i].addActionListener(this);
			weekPanel.add(weekButtons[i],gc);
			gc.gridx++;
		}
		nextWeekButton = new JButton(">>");
		nextWeekButton.addActionListener(this);
		nextWeekButton.setMnemonic(KeyEvent.VK_RIGHT);
		weekPanel.add(nextWeekButton,gc);
		gc.fill=GridBagConstraints.NONE;
		setDate(new Date());
		topPanel.add(weekPanel,BorderLayout.NORTH);
		
		JToolBar actionPanel = new JToolBar(Language.string("Actions"));
		actionPanel.setFloatable(false);
		actionPanel.add(newButton);
		actionPanel.addSeparator();
		actionPanel.add(viewButton);
		actionPanel.add(editButton);
		actionPanel.add(deleteButton);
		actionPanel.add(Box.createHorizontalGlue());
		actionPanel.add(filterField);
		actionPanel.setPreferredSize(new Dimension(0,25));
		actionPanel.add(refreshButton);
		printButton = new JButton(Language.string("Print"));
		printButton.addActionListener(this);
		actionPanel.add(printButton);
		topPanel.add(actionPanel, BorderLayout.CENTER);
		this.add(topPanel, BorderLayout.NORTH);
		
		this.popup = new EventPopup(this);
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
		if(date==null) {
			date = new Date();
		}
		Date today = new Date();
		calendar.setTime(today);
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH);
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		calendar.clear();
		calendar.set(year, month, day);
		today = calendar.getTime();
		logger.debug("Setting date to "+date);
		calendar.setTime(date);
		int d = calendar.get(Calendar.DAY_OF_WEEK)%7;
		//logger.debug("Dow is "+d);
		for(int i=0; i<weekButtons.length; ++i){
			calendar.set(Calendar.DAY_OF_WEEK,i+2);
			String daylabel = "<html>"+dawFormat.format(calendar.getTime())+
				"<br/>"+dayFormat.format(calendar.getTime())+"</html>";
			weekButtons[i].setText(daylabel);
			int _d = (i+2)%7;
			//logger.debug("_Dow is "+_d);
			if(_d==d){
				if(calendar.getTime().equals(today)){
					weekButtons[i].setBackground(Color.BLACK);
				}else{
					weekButtons[i].setBackground(Color.DARK_GRAY);
				}
				weekButtons[i].setForeground(Color.WHITE);
				//weekButtons[i].setEnabled(false);
			}else{
				if(calendar.getTime().equals(today)){
					weekButtons[i].setBackground(Color.LIGHT_GRAY);
				}else{
					weekButtons[i].setBackground(Color.WHITE);
				}
				weekButtons[i].setForeground(Color.BLACK);
				//weekButtons[i].setEnabled(true);
			}
			weekButtons[i].setActionCommand(timestampFormat.format(calendar.getTime()));
		}
		calendar.setTime(date);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		this.refresh();
	}
	
	public Date getDate(){
		return calendar.getTime();
	}
	
	public void showEntity(Event e, boolean edit){
		JDialog dialog = new EventPanel(e,this,edit).createDialog(this.getTopLevelAncestor());
		dialog.setVisible(true);
	}

	public synchronized void addEntity(Event e){
		model.addRow(new Object[]{
				e,
				e.getLastname()+" "+e.getName(),
				e.getDescription(),
				e.getPhone(),
		});
	}

	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		String ac = e.getActionCommand();
		if(o==refreshButton){
			refresh();
		}else if(o==newButton){
			Date d = this.getDate();
			Event ev = new Event(0l);
			ev.setDate(d);
			showEntity(ev, true);
			this.refresh();
		}else if(o==editButton){
			int row = table.convertRowIndexToModel(table.getSelectedRow());
			if(row>=0){
				showEntity((Event)model.getValueAt(row, 0), false);
				this.refresh();
			}
		}else if(o==viewButton){
			int row = table.convertRowIndexToModel(table.getSelectedRow());
			if(row>=0){
				showEntity((Event)model.getValueAt(row, 0), false);
				this.refresh();
			}
		}else if(o==deleteButton){
			int row = table.convertRowIndexToModel(table.getSelectedRow());
			if(row>=0){
				Event context = (Event)model.getValueAt(row, 0);
				int ch = JOptionPane.showConfirmDialog(this, 
						Language.string("Are you sure you want to remove the event {0} at {1}?", context.getDescription(), Event.timeFormat.format(context.getDate())),
						Language.string("Romove event?"), 
						JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
				if(ch==JOptionPane.YES_OPTION){
					controller.delete(context);
					this.refresh();
				}
			}
		}else if(o==prevWeekButton){
			calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR)-1);
			setDate(getDate());
		}else if(o==nextWeekButton){
			calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR)+1);
			setDate(getDate());
		}else if(o==printButton){
			ReportGenerator rg = new ReportGenerator();
			rg.setReport(Configuration.getGlobal(AGENDA_REPORT,AGENDA_REPORT_DEF));
			rg.setHeadValue("day", dawFormat.format(getDate())+" "+dayFormat.format(getDate()));
			for(int i=0; i<model.getRowCount(); i++){
				Map<String,String> map = new HashMap<String,String>();
				Event ev = (Event)model.getValueAt(i, 0);
				String date_s = Event.timeFormat.format(ev.getDate());
				logger.debug("Printing event at "+date_s+" was "+ev.getDate());
				map.put("date",date_s);
				map.put("name",ev.getName());
				map.put("lastname",ev.getLastname());
				map.put("phone", ev.getPhone());
				map.put("description",ev.getDescription());
				rg.addData(map);
			}			
			ReportGeneratorGUI rggui = new ReportGeneratorGUI(rg,"day_"+timestampFormat.format(getDate()));
			rggui.showGUI((Window)SwingUtilities.getRoot(this));
		}else{
			try{
				Date d = timestampFormat.parse(ac);
				if(d.equals(getDate())){
					DatePicker dp = new DatePicker(this);
					dp.setDate(d);
					d = dp.getDate();
					if(d!=null){
						setDate(d);
					}
				}else{
					setDate(d);
				}
			}catch(Exception ex){
				logger.debug("Action command "+ac+" not a valid date!");
			}
		}
	}

	@Override
	public void refresh() {
		this.clear();
		if(isFilterValid()&&super.filterField.getText().trim().length()>0){
			this.filter(super.filterField.getText());
			return;
		}
		Collection<Event> list = ((Agenda)controller).getByDate(getDate());
		if(list!=null){
			for(Event e: list){
				this.addEntity(e);
			}
			controller.getApplication().getGUI().setStatusLabel(Language.string("Loaded {0} entities from {1}", list.size(), controller.getSource()));
		}else{
			controller.getApplication().getGUI().setStatusLabel(Language.string("No {0} data loaded", controller.getSource()));
			JOptionPane.showMessageDialog(this, Language.string("Unable to load events"),Language.string("Database error"),JOptionPane.ERROR_MESSAGE);
		}
	}
	
	public synchronized void filter(String text){
		text = text.trim();
		if(text.length()==0){
			this.refresh();
			return;
		}
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
