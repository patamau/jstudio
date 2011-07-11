package jstudio.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

import jstudio.util.Resources;

public class IconPanel extends JPanel{
	
	public static int BORDER = 10;
	public static float HEAD_SIZE = 40f, TEXT_SIZE = 15f, FOOT_SIZE = 10f;
	
	//graphics
	private ImageIcon _icon; //icon image
	private String _head, _text, _foot;
	private int _limitx, _limity;
	private Font _headFont, _textFont, _footFont;
	private boolean _alignRight;
	
	public IconPanel(String iconname){
		this.setLayout(new BorderLayout());
		_icon = Resources.getImage("splash.png"); 
		if(_icon!=null){
			this.setPreferredSize(new Dimension(_icon.getIconWidth(),_icon.getIconHeight()));
			_limitx = _icon.getIconWidth()-BORDER;
			_limity = _icon.getIconHeight()-BORDER;
		}
	}
	
	public void setHead(String head){
		_head = head;
		this.repaint();
	}
	
	public void setText(String text){
		_text = text;
		this.repaint();
	}
	
	public void setFoot(String foot){
		_foot = foot;
		this.repaint();
	}
	
	private void applyHeadFont(Graphics g){
		if(_headFont==null){
			_headFont = g.getFont().deriveFont(Font.BOLD).deriveFont(HEAD_SIZE);
		}
		g.setFont(_headFont);
	}
	
	private void applyTextFont(Graphics g){
		if(_textFont==null){
			_textFont = g.getFont().deriveFont(TEXT_SIZE);
		}
		g.setFont(_textFont);
	}
	
	private void applyFootFont(Graphics g){
		if(_footFont==null){
			_footFont = g.getFont().deriveFont(FOOT_SIZE);
		}
		g.setFont(_footFont);
	}
	
	private int getAlignment(Graphics g, String txt){
		if(_alignRight){
			return BORDER;
		}else{
			return _limitx-g.getFontMetrics().stringWidth(txt);
		}
	}
	
	public void paint(Graphics g){
		if(_icon!=null){
			_icon.paintIcon(this,g,0,0);
		}
		//backup original font
		Font f = g.getFont();
		if(_head!=null){
			applyHeadFont(g);			
			g.drawString(_head, getAlignment(g, _head), g.getFontMetrics().getHeight());
			g.setFont(f);
		}
		if(_text!=null){
			applyTextFont(g);
			g.drawString(_text, getAlignment(g, _text), _limity/2);
			g.setFont(f);
		}
		if(_foot!=null){
			applyFootFont(g);
			g.drawString(_foot, getAlignment(g, _foot), _limity);
			g.setFont(f);
		}
	}
}
