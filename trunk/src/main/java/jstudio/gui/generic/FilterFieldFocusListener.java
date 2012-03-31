package jstudio.gui.generic;

import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JTextField;

import com.lowagie.text.Font;

import jstudio.util.Language;

public class FilterFieldFocusListener implements FocusListener {
	
	private final JTextField field;
	
	public FilterFieldFocusListener(final JTextField field){
		this.field = field;
		this.focusLost(null);
	}

	@Override
	public void focusGained(FocusEvent e) {
		this.field.setText("");
		this.field.setFont(this.field.getFont().deriveFont(Font.NORMAL));
		this.field.setForeground(Color.BLACK);
	}

	@Override
	public void focusLost(FocusEvent e) {
		if(this.field.getText().length()==0){
			this.field.setForeground(Color.GRAY);
			this.field.setFont(this.field.getFont().deriveFont(Font.ITALIC));
			this.field.setText(Language.string("Filter name and lastname"));
		}
	}

}
