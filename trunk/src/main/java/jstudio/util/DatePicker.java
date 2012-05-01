package jstudio.util;

import java.awt.*;
import java.awt.event.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class DatePicker {
	private static final SimpleDateFormat 
		sdf = new SimpleDateFormat("MMMM yyyy"),
		timestamp = new SimpleDateFormat("yyyyMMddkkmm");
		
	
	public static Color 
		sundayColor = Color.RED,
		saturdayColor = Color.RED.darker();

	private boolean picked = false; //tell if the user clicked on the calendar or not
	private JLabel monthLabel;
	private Calendar calendar;
	private JDialog dialog;
	private JButton[] buttons;
	private JLabel[] labels;
	private int originalDayOfMonth, originalMonth, originalYear;
	
	public static String getTimestamp(Date date){
		return timestamp.format(date);
	}

	public DatePicker(Component parent) {
		calendar = Calendar.getInstance();
		calendar.setFirstDayOfWeek(Calendar.MONDAY);
		
		//LABEL
		monthLabel = new JLabel("", JLabel.CENTER);

		JPanel p1 = new JPanel(new GridLayout(7, 7));
		p1.setPreferredSize(new Dimension(300, 100));
		
		//WEEK DAYS
		String[] header = {  
				Language.string("Mon"), 
				Language.string("Tue"), 
				Language.string("Wed"), 
				Language.string("Thu"), 
				Language.string("Fri"), 
				Language.string("Sat"),
				Language.string("Sun")
			};		
		labels = new JLabel[7];
		for (int i=0; i<labels.length; i++){
			labels[i] = new JLabel(header[i],JLabel.CENTER);
			p1.add(labels[i]);
		}
		labels[5].setForeground(saturdayColor);
		labels[6].setForeground(sundayColor);

		//MONTH DAYS
		buttons = new JButton[42];
		for (int x = 0; x < buttons.length; x++) {
			final int selection = x;
			buttons[x] = new JButton();
			buttons[x].setBorder(new EmptyBorder(0, 0, 0, 0));
			buttons[x].setFocusPainted(false);
			buttons[x].setBackground(Color.white);
			buttons[x].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					String day = buttons[selection].getActionCommand();
					int dom = Integer.parseInt(day);
					calendar.set(Calendar.DAY_OF_MONTH, dom);
					dialog.dispose();
					picked=true;
				}
			});
			p1.add(buttons[x]);
		}
		
		JPanel p2 = new JPanel(new GridLayout(1, 3));
		JButton previousButton = new JButton(Language.string("<<"));
		previousButton.setBackground(Color.WHITE);
		previousButton.setBorder(new EmptyBorder(0, 0, 0, 0));
		previousButton.setToolTipText(Language.string("Previous month"));
		previousButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				calendar.add(Calendar.MONTH, -1);
				setDate(calendar.getTime());
			}
		});
		p2.add(previousButton);
		p2.add(monthLabel);
		JButton nextButton = new JButton(Language.string(">>"));
		nextButton.setBackground(Color.WHITE);
		nextButton.setBorder(new EmptyBorder(0, 0, 0, 0));
		nextButton.setToolTipText(Language.string("Next month"));
		nextButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				calendar.add(Calendar.MONTH, +1);
				setDate(calendar.getTime());
			}
		});
		p2.add(nextButton);
		
		dialog = new JDialog();
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.setTitle(Language.string("Date Picker"));
		dialog.setModal(true);
		dialog.add(p1, BorderLayout.CENTER);
		dialog.add(p2, BorderLayout.NORTH);
		dialog.pack();
		dialog.setResizable(false);
		dialog.setLocationRelativeTo(parent);
	}
	
	private void initOriginalDate(Date date){
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		this.originalDayOfMonth = c.get(Calendar.DAY_OF_MONTH)+1;
		this.originalMonth = c.get(Calendar.MONTH);
		this.originalYear = c.get(Calendar.YEAR);
	}

	public void setDate(Date date) {
		if(date==null) date = new Date();
		//record original data
		if(this.originalDayOfMonth==0) initOriginalDate(date);
		calendar.setTime(date);
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)-2;
		if(dayOfWeek<0) dayOfWeek+=7;
		int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
		System.err.println("DEBUG: "+dayOfWeek+" "+daysInMonth);
		int day = 1;
		for (int x=0;x<buttons.length; x++) {
			int dow = x%7;
			buttons[x].setBackground(Color.WHITE);
			if(dow==5){
				buttons[x].setForeground(saturdayColor);
			}else if(dow==6){
				buttons[x].setForeground(sundayColor);
			}
			if(x<dayOfWeek||day>daysInMonth){
				buttons[x].setText("");
				buttons[x].setEnabled(false);
			}else{
				buttons[x].setText(Integer.toString(day++));
				buttons[x].setEnabled(true);
				if(originalDayOfMonth==day&&
						originalMonth==calendar.get(Calendar.MONTH)&&
						originalYear==calendar.get(Calendar.YEAR)){
					buttons[x].setBackground(Color.LIGHT_GRAY);
				}
			}
		}
		monthLabel.setText(sdf.format(calendar.getTime()));
	}

	public Date getDate() {
		dialog.setVisible(true);
		if(picked) return calendar.getTime();
		else return null;
	}

	/**
	 * demo
	 * @param args
	 */
	public static void main(String[] args) {
		JLabel label = new JLabel("Selected Date:");
		final JTextField text = new JTextField(20);
		JButton b = new JButton("popup");
		JPanel p = new JPanel();
		p.add(label);
		p.add(text);
		p.add(b);
		final JFrame f = new JFrame();
		f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		f.getContentPane().add(p);
		f.pack();
		f.setVisible(true);
		final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
		b.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				DatePicker dp = new DatePicker(f);
				try {
					dp.setDate(dateFormat.parse(text.getText()));
				} catch (ParseException e) {
					dp.setDate(new Date());
				}
				Date d = dp.getDate();
				System.out.println("Date is " + d);
				if (d != null) {
					text.setText(dateFormat.format(d));
				}
			}
		});
	}
}
