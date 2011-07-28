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
import java.util.Map;

import jstudio.JStudio;
import jstudio.control.AddressBook;
import jstudio.model.Person;
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
		AddressBook cts = app.getAddressBook();
		
		ReportGenerator<Person> rg = new ReportGenerator<Person>();
		rg.setReport("/report1.jasper");
		rg.setData(cts.getAll());
		try {
			rg.generatePdf(".","sampleReportPerson.pdf");
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	}
	
	private String reportName;
	private Collection<Map<String,String>> data;
	
	public ReportGenerator(){
		reportName = null;
	}
	
	public void setReport(String name){
		reportName = name;
	}
	
	public void setData(Collection<Entry> data){
		this.data = new ArrayList<Map<String,String>>();
		Map<String,String> row;
		for(Entry e: data){
	        row = new HashMap<String,String>();
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

