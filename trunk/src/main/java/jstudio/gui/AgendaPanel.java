package jstudio.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

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

import jstudio.control.Agenda;
import jstudio.control.Controller;
import jstudio.gui.generic.EntityManagerPanel;
import jstudio.gui.generic.PopupListener;
import jstudio.model.Event;
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
		dateFormat = new SimpleDateFormat("EEEE dd MMMM yyyy");
	private static final Logger logger = LoggerFactory.getLogger(AgendaPanel.class);
	
	public static final String PIC_AGENDA="eventicon.png";
	
	private JButton dateButton;
	private JButton refreshButton;

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

		JToolBar actionPanel = new JToolBar(Language.string("Actions"));
		actionPanel.setFloatable(false);
		refreshButton = new JButton(Language.string("Refresh"));
		refreshButton.addActionListener(this);
		actionPanel.add(refreshButton);
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
}
