package jstudio.gui.generic;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;

public class NicePanel extends JPanel {

	public static void main(String args[]){
		NicePanel p = new NicePanel("Create new Contact", "Fill the form to create a new contact");
		p.addButton(new JButton("OK"));
		p.addButton(new JButton("Cancel"));
		JFrame f = new JFrame();
		f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		f.getContentPane().add(p);
		f.pack();
		f.setVisible(true);
	}
	
	private JPanel header, body, footer;
	private GridBagConstraints footerConstraints;
	
	/**
	 * No subtitle
	 * @param title
	 */
	public NicePanel(final String title){
		this(title, null);
	}
	
	public NicePanel(final String title, final String subtitle){
		this.setLayout(new BorderLayout());
		this.add(getHeader(title, subtitle), BorderLayout.NORTH);
		this.add(new JScrollPane(getBody()), BorderLayout.CENTER);
		this.add(getFooter(), BorderLayout.SOUTH);
	}
	
	protected JPanel getHeader(final String title, final String subtitle){
		if(header==null){
			JPanel panel = new JPanel(new GridBagLayout());
			panel.setBackground(Color.WHITE);
			
			GridBagConstraints gc = new GridBagConstraints();
			gc.gridx=gc.gridy=0;
			gc.weightx=1.0;
			gc.weighty=0.0;
			gc.insets=new Insets(2,15,2,2);
			gc.fill=GridBagConstraints.HORIZONTAL;
			
			if(title!=null){
				JLabel titleLabel = new JLabel(title);
				titleLabel.setFont(titleLabel.getFont().deriveFont(24f).deriveFont(Font.PLAIN));
				panel.add(titleLabel, gc);
				gc.gridy++;
			}
			if(subtitle!=null){
				JLabel subtitleLabel = new JLabel(subtitle);
				subtitleLabel.setFont(subtitleLabel.getFont().deriveFont(Font.PLAIN));
				panel.add(subtitleLabel, gc);
			}			
			
			header = panel;
		}
		
		return header;
	}
	
	/**
	 * Retrieve the naked body panel
	 * @return
	 */
	public JPanel getBody(){
		if(body==null){
			body = new JPanel();		
		}
		
		return body;
	}
	
	/**
	 * Add a JButton to the footer.
	 * Listeners are set externally
	 * All the buttons are aligned right,
	 * and the last button is rightmost
	 * @param button
	 */
	public void addButton(final JButton button){
		footer.add(button, footerConstraints);
		footerConstraints.gridx++;
	}
	
	protected JPanel getFooter(){
		if(footer==null){
			JPanel panel = new JPanel(new GridBagLayout());
			
			GridBagConstraints gc = new GridBagConstraints();
			gc.gridx=gc.gridy=0;
			gc.weightx=1.0;
			gc.weighty=0.0;
			gc.fill=GridBagConstraints.HORIZONTAL;
			gc.insets=new Insets(5,5,5,5);
			panel.add(new JPanel(), gc);
			gc.weightx=0.0f;
			++gc.gridx;
			gc.fill=GridBagConstraints.NONE;
			footerConstraints = gc;
			
			footer = panel;
		}
		
		return footer;
	}
}
