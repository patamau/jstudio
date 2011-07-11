package jstudio.util;

import java.util.HashMap;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

public class Resources {
	
	public static String PIX_PATH = "pix";
	
	private static HashMap<String, ImageIcon> cache = new HashMap<String, ImageIcon>();

	public static final ImageIcon getImage(String name){
		ImageIcon i = cache.get(name);
		if(i==null){
			String url = PIX_PATH+'/'+name;
			try {
				i = new ImageIcon(ImageIO.read(ClassLoader.getSystemClassLoader().getResourceAsStream(url)));
				cache.put(name, i);
			} catch (Exception e) {
				System.err.println("Cannot locate "+name+" at "+url);
				e.printStackTrace();
			}
		}
		return i;
	}
}
