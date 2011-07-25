package jstudio.util;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class GUITool {
	
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
	
	public static JButton createButton(Container c, GridBagConstraints gc, String label){
		JButton f = new JButton(label);
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
	
	public static JTextArea createArea(Container c, GridBagConstraints gc, String label, String value){
		JTextArea f = new JTextArea(value);
		f.setColumns(0);
		f.setRows(0);
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
		c.add(new JScrollPane(f),gc);
		gc.fill=GridBagConstraints.NONE;
		gc.weightx=0.0f;
		gc.weighty=0.0f;
		gc.gridx=px;
		return f;
	}

	public static JTextField createField(Container c, GridBagConstraints gc, String label, String value, boolean editable){
		JTextField f = new JTextField(value);
		f.setEditable(editable);
		f.setColumns(0);
		gc.gridy++;
		JLabel l = new JLabel(label, JLabel.RIGHT);
		c.add(l,gc);
		gc.fill=GridBagConstraints.HORIZONTAL;
		gc.weightx=1.0f;
		int px = gc.gridx;
		gc.gridx++;
		c.add(f,gc);
		gc.fill=GridBagConstraints.NONE;
		gc.weightx=0.0f;
		gc.gridx=px;
		return f;
	}
	
	public static JComboBox createCombo(Container c, GridBagConstraints gc, String label, String[] values, boolean editable){
		JComboBox f = new JComboBox(values);
		f.setEditable(editable);
		gc.gridy++;
		c.add(new JLabel(label, JLabel.RIGHT),gc);
		gc.fill=GridBagConstraints.HORIZONTAL;
		gc.weightx=1.0f;
		int px = gc.gridx;
		gc.gridx++;
		c.add(f,gc);
		gc.fill=GridBagConstraints.NONE;
		gc.weightx=0.0f;
		gc.gridx=px;
		return f;
	}
	
	public static JTextField createDateField(final Container c, GridBagConstraints gc, String label, String value, boolean editable, final SimpleDateFormat dateFormat){
		final JTextField f = new JTextField(value);
		f.setEditable(editable);
		f.setColumns(0);
		gc.gridy++;
		JLabel l = new JLabel(label, JLabel.RIGHT);
		c.add(l,gc);
		gc.fill=GridBagConstraints.HORIZONTAL;
		gc.weightx=1.0f;
		int px = gc.gridx;
		gc.gridx++;
		c.add(f,gc);
		if(editable){
			JButton b = new JButton("...");
			b.setPreferredSize(new Dimension(25,20));
			b.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					DatePicker d = new DatePicker(c);
					try {
						d.setDate(dateFormat.parse(f.getText()));
					} catch (ParseException e1) {
						//ignore errors here!
					}
					f.setText(dateFormat.format(d.getDate()));
				}
			});
			gc.gridx++;
			c.add(b,gc);
		}
		gc.fill=GridBagConstraints.NONE;
		gc.weightx=0.0f;
		gc.gridx=px;
		return f;
	}
}