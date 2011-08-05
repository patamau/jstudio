package jstudio.util;

import java.io.InputStream;
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
	
	public static final InputStream getFile(String path){
		try{
			return ClassLoader.getSystemResourceAsStream(path);
		}catch(Exception e){
			System.err.println("Cannot locate "+path);
			e.printStackTrace();
		}
		return null;
	}
}
