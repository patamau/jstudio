package jstudio.util;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerModel;

import jstudio.gui.generic.TimeSpinnerModel;
import jstudio.model.Event;

public class GUITool {
	
	public static void main(String args[]){
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		GridBagConstraints gc = new GridBagConstraints();
		frame.getContentPane().setLayout(new GridBagLayout());		
		frame.getContentPane().add(GUITool.createTimeSpinner(frame.getContentPane(), gc, "Time", new Date(), true));
		
		//Add the third label-spinner pair.
		Calendar calendar = Calendar.getInstance();
        Date initDate = calendar.getTime();
        //calendar.add(Calendar.YEAR, -100);
        calendar.add(Calendar.YEAR, -24);
        Date earliestDate = calendar.getTime();
        //calendar.add(Calendar.YEAR, 200);
        calendar.add(Calendar.YEAR, +24);
        Date latestDate = calendar.getTime();
        SpinnerModel dateModel = new SpinnerDateModel(initDate,
                                     null,
                                     null,
                                     Calendar.MINUTE);//ignored for user input
        JSpinner spinner = new JSpinner(dateModel);
        spinner.setEditor(new JSpinner.DateEditor(spinner, "kk:mm dd/MM/yyyy"));
        gc.gridwidth=2;
        gc.gridy++;
        frame.getContentPane().add(spinner, gc);
		
        frame.pack();
        frame.setLocationRelativeTo(null);
		
		frame.setVisible(true);
	}
	
	public static final DateFormat dateFormat = new SimpleDateFormat("dd/mm/yyyy");
	
	public static void appendContainer(Container c, GridBagConstraints gc, Container f){
		int ow = gc.gridwidth; //store old weight
		gc.gridwidth=2;
		gc.gridy++;
		gc.fill=GridBagConstraints.HORIZONTAL;
		gc.weightx=1.0f;
		int px = gc.gridx; //store old gridx
		gc.gridx++;
		c.add(f,gc);
		gc.fill=GridBagConstraints.NONE;
		gc.weightx=0.0f;
		gc.gridx=px;
		gc.gridwidth=ow;
	}
	
	public static JButton createButton(Container c, GridBagConstraints gc, String label, ActionListener listener){
		JButton f = new JButton(label);
		if(listener!=null) f.addActionListener(listener);
		int ow = gc.gridwidth; //store old weight
		gc.gridwidth=2;
		gc.gridy++;
		gc.fill=GridBagConstraints.HORIZONTAL;
		gc.weightx=1.0f;
		int px = gc.gridx; //store old gridx
		gc.gridx++;
		c.add(f,gc);
		gc.fill=GridBagConstraints.NONE;
		gc.weightx=0.0f;
		gc.gridx=px;
		gc.gridwidth=ow;
		return f;
	}
	
	public static JTextArea createArea(Container c, GridBagConstraints gc, String label, String value, boolean editable){
		JTextArea f = new JTextArea(value);
		f.setEditable(editable);
		f.setColumns(20);
		f.setRows(5);
		f.setLineWrap(true);
		f.setWrapStyleWord(true);
		gc.gridy++;
		gc.anchor=GridBagConstraints.NORTH;
		JLabel jlabel = new JLabel(label, JLabel.RIGHT);
		jlabel.setAlignmentY(JLabel.TOP_ALIGNMENT);
		jlabel.setVerticalAlignment(JLabel.TOP);
		c.add(jlabel,gc);
		gc.fill=GridBagConstraints.BOTH;
		gc.weightx=1.0f;
		gc.weighty=1.0f;
		int px = gc.gridx;
		gc.gridx++;
		int ow = gc.gridwidth; //store old weight
		gc.gridwidth=2;
		if(!editable){
			f.setBackground((Color)Toolkit.getDefaultToolkit().getDesktopProperty("control"));
			f.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
		}
		JScrollPane fScroll = new JScrollPane(f, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		fScroll.setBorder(BorderFactory.createLineBorder(Color.gray));
		c.add(fScroll,gc);
		gc.fill=GridBagConstraints.NONE;
		gc.weightx=0.0f;
		gc.weighty=0.0f;
		gc.gridx=px;
		gc.gridwidth=ow;
		return f;
	}
	
	public static JSpinner createTimeSpinner(Container c, GridBagConstraints gc, String label, Date value, boolean editable){
		final TimeSpinnerModel m = new TimeSpinnerModel(value);
		//final SpinnerModel m = new CyclingSpinnerListModel(new String[]{"ciao","come","va?"}); 
		final JSpinner s = new JSpinner(m);
		JFormattedTextField ftf = ((JSpinner.DefaultEditor)s.getEditor()).getTextField();
		ftf.setHorizontalAlignment(JTextField.LEFT);
		gc.gridy++;
		gc.anchor=GridBagConstraints.EAST;
		JLabel l = new JLabel(label, JLabel.RIGHT);
		c.add(l,gc);
		gc.anchor=GridBagConstraints.WEST;
		gc.fill=GridBagConstraints.HORIZONTAL;
		gc.weightx=1.0f;
		int px = gc.gridx;
		gc.gridx++;
		int ow = gc.gridwidth; //store old weight
		gc.gridwidth=2;
		if(editable){
			c.add(s,gc);
		}else{
			JTextField field = new JTextField(Event.timeFormat.format(value));
			field.setEditable(false);
			c.add(field, gc);
		}
		gc.fill=GridBagConstraints.NONE;
		gc.gridwidth=1;
		gc.weightx=0.0f;
		gc.gridx=px;
		gc.gridwidth=ow;
		return s;
	}
	
	public static JTextField createCodeField(Container c, GridBagConstraints gc, String label, String value, boolean editable){
		final JTextField f = new JTextField(value);
		f.setEditable(editable);
		f.setColumns(16);
		if(editable){
			f.addKeyListener(new CodeKeyListener(f));	
		}
		gc.gridy++;
		gc.anchor=GridBagConstraints.EAST;
		JLabel l = new JLabel(label, JLabel.RIGHT);
		c.add(l,gc);
		gc.anchor=GridBagConstraints.WEST;
		gc.fill=GridBagConstraints.HORIZONTAL;
		gc.weightx=1.0f;
		int px = gc.gridx;
		gc.gridx++;
		int ow = gc.gridwidth; //store old weight
		gc.gridwidth=2;
		c.add(f,gc);
		gc.fill=GridBagConstraints.NONE;
		gc.gridwidth=1;
		gc.weightx=0.0f;
		gc.gridx=px;
		gc.gridwidth=ow;
		return f;
	}
	
	public static JCheckBox createCheck(Container c, GridBagConstraints gc, String label, String value, boolean editable){
		JCheckBox f = new JCheckBox(label);
		f.setSelected(value.length()>0);
		gc.gridy++;
		gc.anchor=GridBagConstraints.WEST;
		gc.fill=GridBagConstraints.HORIZONTAL;
		gc.weightx=1.0f;
		int px = gc.gridx;
		gc.gridx++;		
		int ow = gc.gridwidth; //store old weight
		gc.gridwidth=2;
		c.add(editable?f:new JLabel(value),gc);
		gc.fill=GridBagConstraints.NONE;
		gc.weightx=0.0f;
		gc.gridx=px;
		gc.gridwidth=ow;
		return f;
	}
	

	public static JTextField createField(Container c, GridBagConstraints gc, String label, String value, boolean editable){
		JTextField f = new JTextField(value);
		f.setEditable(editable);
		f.setColumns(0);
		gc.gridy++;
		gc.anchor=GridBagConstraints.EAST;
		JLabel l = new JLabel(label, JLabel.RIGHT);
		c.add(l,gc);
		gc.anchor=GridBagConstraints.WEST;
		gc.fill=GridBagConstraints.HORIZONTAL;
		gc.weightx=1.0f;
		int px = gc.gridx;
		gc.gridx++;		
		int ow = gc.gridwidth; //store old weight
		gc.gridwidth=2;
		c.add(f,gc);
		gc.fill=GridBagConstraints.NONE;
		gc.weightx=0.0f;
		gc.gridx=px;
		gc.gridwidth=ow;
		return f;
	}
	
	public static JComboBox createCombo(Container c, GridBagConstraints gc, String label, int selected, Object[] values, boolean editable){
		JComboBox f = new JComboBox(values);
		f.setEditable(false);
		f.setEnabled(editable);
		f.setPreferredSize(new Dimension(0,20));
		if(values.length>0){
			f.setSelectedIndex(selected);
		}
		gc.gridy++;
		gc.anchor=GridBagConstraints.EAST;
		c.add(new JLabel(label, JLabel.RIGHT),gc);
		gc.fill=GridBagConstraints.HORIZONTAL;
		gc.anchor=GridBagConstraints.WEST;
		gc.weightx=1.0f;
		int px = gc.gridx;
		gc.gridx++;
		int ow = gc.gridwidth; //store old weight
		gc.gridwidth=2;
		if(!editable){
			if(values.length>0&&values[selected]!=null){
				JLabel l = new JLabel(values[selected].toString(), JLabel.LEFT);
				l.setFont(l.getFont().deriveFont(Font.PLAIN));
				c.add(l,gc);
			}else{
				c.add(new JLabel(), gc);
			}
		}else{
			c.add(f,gc);
		}
		gc.fill=GridBagConstraints.NONE;
		gc.weightx=0.0f;
		gc.gridx=px;
		gc.gridwidth=ow;
		return f;
	}
	
	private static class CodeKeyListener implements KeyListener {
		final JTextField field;
		public CodeKeyListener(final JTextField f){
			field = f;
		}
		@Override
		public void keyTyped(KeyEvent e) {
			e.consume();
			char ch = e.getKeyChar();
			int pos = field.getCaretPosition();
			if(pos>15) return;
			if(KeyEvent.VK_BACK_SPACE==ch) return;
			if(pos<6 || pos==8 || pos==11 || pos==15){
				if(Character.isLetter(ch)){
					field.setText(field.getText().substring(0,pos)+Character.toUpperCase(ch));
					++pos;
				}
			}else {
				if(Character.isDigit(ch)){
					field.setText(field.getText().substring(0,pos)+Character.toUpperCase(ch));
					++pos;
				}
			}
		}
		@Override
		public void keyPressed(KeyEvent e) {}
		@Override
		public void keyReleased(KeyEvent e) {}
	}
	
	private static class DateKeyListener implements KeyListener {
		final JTextField field;
		public DateKeyListener(final JTextField f){
			field = f;
		}
		@Override
		public void keyTyped(KeyEvent e) {
			e.consume();
			char ch = e.getKeyChar();
			int pos = field.getCaretPosition();
			if(KeyEvent.VK_BACK_SPACE==ch){
				field.setText(field.getText().substring(0,pos));
				return;
			}
			if(pos==2||pos==5){
				if(Character.isDigit(ch)){
					field.setText(field.getText().substring(0,pos)+'/'+ch);
					pos+=2;
				}else{
					field.setText(field.getText().substring(0,pos)+'/');
					pos++;
				}
				field.setCaretPosition(pos);
			}else if(pos==8||pos==9){				
				if(Character.isDigit(ch)){
					field.setText(field.getText().substring(0,pos)+ch);
				}
			}else if(pos<8){
				if(Character.isDigit(ch)){
					field.setText(field.getText().substring(0,pos)+ch);
				}else{
					if(pos==1){
						field.setText('0'+field.getText().substring(0,pos)+'/');
						field.setCaretPosition(3);
					}else if(pos==4){
						field.setText(field.getText().substring(0,3)+'0'+field.getText().charAt(3)+'/');
						field.setCaretPosition(6);
					}
				}
			}
		}
		@Override
		public void keyPressed(KeyEvent e) {}
		@Override
		public void keyReleased(KeyEvent e) {}
	}	
	
	private static class ProvinceKeyListener implements KeyListener {
		final JTextField field;
		public ProvinceKeyListener(final JTextField f){
			field = f;
		}
		@Override
		public void keyTyped(KeyEvent e) {
			e.consume();
			char ch = e.getKeyChar();
			int pos = field.getCaretPosition();
			if(KeyEvent.VK_BACK_SPACE==ch){
				field.setText(field.getText().substring(0,pos));
				return;
			}
			if(pos<2){
				if(Character.isLetter(ch)){
					field.setText(field.getText().substring(0,pos)+Character.toUpperCase(ch));
					++pos;
				}
			}
		}
		@Override
		public void keyPressed(KeyEvent e) {}
		@Override
		public void keyReleased(KeyEvent e) {}
	}
	
	private static class CAPKeyListener implements KeyListener {
		final JTextField field;
		public CAPKeyListener(final JTextField f){
			field = f;
		}
		@Override
		public void keyTyped(KeyEvent e) {
			e.consume();
			char ch = e.getKeyChar();
			int pos = field.getCaretPosition();
			if(KeyEvent.VK_BACK_SPACE==ch){
				field.setText(field.getText().substring(0,pos));
				return;
			}
			if(pos<5){
				if(Character.isDigit(ch)){
					field.setText(field.getText().substring(0,pos)+ch);
					++pos;
				}
			}
		}
		@Override
		public void keyPressed(KeyEvent e) {}
		@Override
		public void keyReleased(KeyEvent e) {}
	}
	
	public static JTextField createCAPField(Container c, GridBagConstraints gc, String label, String value, boolean editable){
		final JTextField f = new JTextField(value);
		f.setEditable(editable);
		f.setColumns(5);
		if(editable){
			f.addKeyListener(new CAPKeyListener(f));		
			f.addFocusListener(new FocusListener(){
				@Override
				public void focusGained(FocusEvent e) {
					f.setCaretPosition(0);
				}
	
				@Override
				public void focusLost(FocusEvent e) { }
			});
		}
		gc.gridy++;
		gc.anchor=GridBagConstraints.EAST;
		JLabel l = new JLabel(label, JLabel.RIGHT);
		c.add(l,gc);
		gc.anchor=GridBagConstraints.WEST;
		gc.fill=GridBagConstraints.HORIZONTAL;
		gc.weightx=1.0f;
		int px = gc.gridx;
		gc.gridx++;
		int ow = gc.gridwidth; //store old weight
		gc.gridwidth=2;
		c.add(f,gc);
		gc.fill=GridBagConstraints.NONE;
		gc.gridwidth=1;
		gc.weightx=0.0f;
		gc.gridx=px;
		gc.gridwidth=ow;
		return f;
	}
	
	public static JTextField createProvinceField(Container c, GridBagConstraints gc, String label, String value, boolean editable){
		final JTextField f = new JTextField(value);
		f.setEditable(editable);
		f.setColumns(2);
		if(editable){
			f.addKeyListener(new ProvinceKeyListener(f));		
			f.addFocusListener(new FocusListener(){
				@Override
				public void focusGained(FocusEvent e) {
					f.setCaretPosition(0);
				}
	
				@Override
				public void focusLost(FocusEvent e) {}
			});
		}
		gc.gridy++;
		gc.anchor=GridBagConstraints.EAST;
		JLabel l = new JLabel(label, JLabel.RIGHT);
		c.add(l,gc);
		gc.anchor=GridBagConstraints.WEST;
		gc.fill=GridBagConstraints.HORIZONTAL;
		gc.weightx=1.0f;
		int px = gc.gridx;
		gc.gridx++;
		int ow = gc.gridwidth; //store old weight
		gc.gridwidth=2;
		c.add(f,gc);
		gc.fill=GridBagConstraints.NONE;
		gc.weightx=0.0f;
		gc.gridx=px;
		gc.gridwidth=ow;
		return f;
	}
	
	public static JTextField createDateField(final Container c, GridBagConstraints gc, String label, Date value, boolean editable, final SimpleDateFormat dateFormat){
		final JTextField f = new JTextField(dateFormat.format(value));
		if(editable){
			f.addKeyListener(new DateKeyListener(f));		
			f.addFocusListener(new FocusListener(){
				@Override
				public void focusGained(FocusEvent e) {
					f.setCaretPosition(0);
					f.setBackground(Color.white);
				}
	
				@Override
				public void focusLost(FocusEvent e) {
					try {
						Date d = dateFormat.parse(f.getText());
						Calendar c = Calendar.getInstance();
						c.setTime(d);
						int year = c.get(Calendar.YEAR);
						if(year<100){
							c.set(Calendar.YEAR, year+1900);
						}else if(year<1000){
							c.set(Calendar.YEAR, year+1000);
						}
						f.setText(dateFormat.format(c.getTime()));
					} catch (ParseException e1) {
						f.setBackground(Color.orange);
					}
				}
			});
		}
		//final JFormattedTextField f = new JFormattedTextField(DateFormat.getDateInstance(DateFormat.MEDIUM));
		f.setEditable(editable);
		f.setColumns(10);
		gc.gridy++;
		gc.anchor=GridBagConstraints.EAST;
		JLabel l = new JLabel(label, JLabel.RIGHT);
		c.add(l,gc);
		gc.anchor=GridBagConstraints.WEST;
		gc.fill=GridBagConstraints.HORIZONTAL;
		gc.weightx=1.0f;
		int px = gc.gridx;
		gc.gridx++;
		c.add(f,gc);
		if(editable){
			String blabel = Language.string("Pick");
			JButton b = new JButton(blabel);
			int swidth = b.getFontMetrics(b.getFont()).stringWidth(blabel);
			b.setPreferredSize(new Dimension(swidth+40,20));
			b.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					DatePicker d = new DatePicker(c);
					try {
						d.setDate(dateFormat.parse(f.getText()));
					} catch (ParseException e1) {
						//ignore errors here!
					}
					Date pd = d.getDate();
					if(pd!=null){
						f.setText(dateFormat.format(pd));
					}
				}
			});
			gc.weightx=0.0f;
			gc.fill=GridBagConstraints.NONE;
			gc.gridx++;
			c.add(b,gc);
		}
		gc.fill=GridBagConstraints.NONE;
		gc.weightx=0.0f;
		gc.gridx=px;
		return f;
	}
}
