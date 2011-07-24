package jstudio.util;

import java.awt.*;
import java.awt.event.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.swing.*;

public class DatePicker {
	private static final SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy");
	
	public static Color 
		sundayColor = Color.RED,
		saturdayColor = Color.RED.darker();

	private int month = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH);
	private int year = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);;
	private JLabel monthLabel = new JLabel("", JLabel.CENTER);
	private Calendar calendar;
	private JDialog dialog;
	private JButton[] buttons = new JButton[42];
	private JLabel[] labels = new JLabel[7];

	public DatePicker(Component parent) {
		calendar = Calendar.getInstance();
		calendar.setFirstDayOfWeek(Calendar.MONDAY);

		JPanel p1 = new JPanel(new GridLayout(7, 7));
		p1.setPreferredSize(new Dimension(430, 120));
		
		String[] header = {  
				Language.string("Mon"), 
				Language.string("Tue"), 
				Language.string("Wed"), 
				Language.string("Thu"), 
				Language.string("Fri"), 
				Language.string("Sat"),
				Language.string("Sun")
			};		
		for (int i=0; i<labels.length; i++){
			labels[i] = new JLabel(header[i],JLabel.CENTER);
			p1.add(labels[i]);
		}
		labels[5].setForeground(saturdayColor);
		labels[6].setForeground(sundayColor);

		for (int x = 0; x < buttons.length; x++) {
			final int selection = x;
			buttons[x] = new JButton();
			buttons[x].setFocusPainted(false);
			buttons[x].setBackground(Color.white);
			buttons[x].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					String day = buttons[selection].getActionCommand();
					int dom = Integer.parseInt(day);
					calendar.set(Calendar.DAY_OF_MONTH, dom);
					dialog.dispose();
				}
			});
			p1.add(buttons[x]);
		}
		
		JPanel p2 = new JPanel(new GridLayout(1, 3));
		JButton previousButton = new JButton("<< Previous");
		previousButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				calendar.add(Calendar.MONTH, -1);
				setDate(calendar.getTime());
			}
		});
		p2.add(previousButton);
		p2.add(monthLabel);
		JButton nextButton = new JButton("Next >>");
		nextButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				calendar.add(Calendar.MONTH, +1);
				setDate(calendar.getTime());
			}
		});
		p2.add(nextButton);
		
		dialog = new JDialog();
		dialog.setTitle("Date Picker");
		dialog.setModal(true);
		dialog.add(p1, BorderLayout.CENTER);
		dialog.add(p2, BorderLayout.NORTH);
		dialog.pack();
		dialog.setResizable(false);
		dialog.setLocationRelativeTo(parent);
	}

	public void setDate(Date date) {
		calendar.setTime(date);
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)-2;
		int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
		int day = 1;
		for (int x=0;x<buttons.length; x++) {
			int dow = x%7;
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
			}
		}
		monthLabel.setText(sdf.format(calendar.getTime()));
	}

	public Date getDate() {
		dialog.setVisible(true);
		return calendar.getTime();
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
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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
					e.printStackTrace();
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
