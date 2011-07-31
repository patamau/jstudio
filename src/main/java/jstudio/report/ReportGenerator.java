package jstudio.report;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jstudio.JStudio;
import jstudio.control.Accounting;
import jstudio.model.Product;
import net.sf.jasperreports.engine.JasperRunManager;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
 
/**
 * Hello world!
 *
 */
public class ReportGenerator<Entry> {
	
	public static void main(String args[]){
		JStudio app = new JStudio();
		app.initialize();
		Accounting acc = app.getAccounting();
		
		ReportGenerator<Product> rg = new ReportGenerator<Product>();
		rg.setReport("/report1.jasper");
		rg.setData(acc.getProducts().getAll());
		try {
			rg.generatePdf(".","sampleReportInvoice.pdf");
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	}
	
	private String reportName;
	private Map<String,String> head;
	private List<Map<String,String>> data;
	
	public ReportGenerator(){
		reportName = null;
		this.data = new ArrayList<Map<String,String>>();
		head = new HashMap<String,String>();
		head.put("id","124");
		head.put("date","12/07/2011");
		head.put("name", "Matteo");
		head.put("lastname", "Pedrotti");
	}
	
	public void setReport(String name){
		reportName = name;
	}
	
	public void setData(Collection<Entry> data){
		Map<String,String> row = this.head;
		for(Entry e: data){	  
	        for(Field f: e.getClass().getDeclaredFields()){
	        	if(Modifier.isStatic(f.getModifiers())) continue;
	        	try{
		        	String fname = f.getName();
		        	String mname = "get"+fname.substring(0,1).toUpperCase()+fname.substring(1);
		        	Method m = e.getClass().getMethod(mname);
		        	Object o = m.invoke(e);
		        	if(o!=null){
		        		row.put(fname, o.toString());
		        	}
				} catch (SecurityException e1) {
					e1.printStackTrace();
				} catch (NoSuchMethodException e1) {
					e1.printStackTrace();
				} catch (IllegalArgumentException e1) {
					e1.printStackTrace();
				} catch (IllegalAccessException e1) {
					e1.printStackTrace();
				} catch (InvocationTargetException e1) {
					e1.printStackTrace();
				}
	        }
    		this.data.add(row);
    		row = new HashMap<String,String>();
		}
	}
 
    public void generatePdf(String outputDir, String outputFile) throws Exception {
        InputStream is = getClass().getResourceAsStream(reportName);
        File outDir = new File(outputDir);
        outDir.mkdirs();
        OutputStream os = new FileOutputStream(new File(outDir, outputFile));
 
        JRMapCollectionDataSource dataSource = new JRMapCollectionDataSource(data);
 
        JasperRunManager.runReportToPdfStream(is, os, null, dataSource);
    }
}

