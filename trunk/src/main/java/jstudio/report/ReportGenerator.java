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
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jstudio.db.DatabaseObject;
import jstudio.model.Invoice;
import jstudio.model.Product;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperRunManager;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import net.sf.jasperreports.engine.export.JRRtfExporter;
import net.sf.jasperreports.engine.export.JRTextExporter;
 
/**
 * Hello world!
 *
 */
public class ReportGenerator {
	
	public static void main(String args[]){		
		ReportGenerator rg = new ReportGenerator();
		rg.setReport("/reports/report1.jasper");
		Invoice i = new Invoice(1l);
		i.setId(23l);
		i.setDate(new Date());
		i.setName("Matteo");
		i.setLastname("Pedrotti");
		i.setAddress("Via del Bomport, 20");
		i.setCity("Trento");
		i.setCap("38123");
		i.setProvince("TN");
		Set<Product> set = new HashSet<Product>();
		Product p = new Product(1l);
		p.setId(0l);
		p.setDescription("Estrazione dente del giudizio");
		p.setCost(150f);
		p.setQuantity(1);
		set.add(p);
		Product p2 = new Product(2l);
		p2.setId(1l);
		p2.setDescription("Pulizia dentale e sbiancamento");
		p.setCost(200f);
		p.setQuantity(1);
		set.add(p2);
		i.setProducts(set);
		rg.setHead(i);
		rg.setData(i.getProducts());
		rg.setHeadValue("total", Float.toString(350f));
		try {
			rg.generatePdf(".","sampleReportInvoice.pdf");
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	}
	
	private String reportName;
	private Map<String,String> head; //static header data
	private List<Map<String,String>> data; //set of contents
	
	public ReportGenerator(){
		reportName = null;
		this.data = new ArrayList<Map<String,String>>();
		this.head = new HashMap<String,String>();
	}
	
	public void setReport(String name){
		reportName = name;
	}
	
	public void setHeadValue(String key, String value){
		if(this.data.size()==0){
			this.head.put(key, value);
		}else{
			this.data.get(this.data.size()-1).put(key, value);
		}
	}
	
	public void setHead(DatabaseObject entry){
		if(head.size()>0) head.clear();
        for(Field f: entry.getClass().getDeclaredFields()){
        	if(Modifier.isStatic(f.getModifiers())) continue;
        	try{
	        	String fname = f.getName();
	        	String mname = "get"+fname.substring(0,1).toUpperCase()+fname.substring(1);
	        	Method m = entry.getClass().getMethod(mname);
	        	Object o = m.invoke(entry);
	        	if(o!=null){
	        		head.put(fname, o.toString());
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
	}
	
	/**
	 * Custom method
	 * @param entry
	 */
	public void addData(Map<String, String> entry){
		this.data.add(entry);
	}
	
	/**
	 * Retrieve all the data being printed
	 * @return
	 */
	public List<Map<String,String>> getData(){
		return this.data;
	}
	
	/**
	 * Uses reflection to quickly take out variables of class
	 * @param data
	 */
	public void setData(Collection<? extends DatabaseObject> data){
		if(this.data.size()>0) this.data.clear();
		Map<String,String> row = this.head;
		for(DatabaseObject e: data){	  
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
        if(is==null){
        	throw new Exception("No such report "+reportName);
        }
        File outDir = new File(outputDir);
        outDir.mkdirs();
        OutputStream os = new FileOutputStream(new File(outDir, outputFile)); 
        JRMapCollectionDataSource dataSource = new JRMapCollectionDataSource(data);
        JasperRunManager.runReportToPdfStream(is, os, null, dataSource);
        os.close();
        is.close();
    }
    
    public void generateRtf(String outputDir, String outputFile) throws Exception {
        InputStream is = getClass().getResourceAsStream(reportName);
        if(is==null){
        	throw new Exception("No such report "+reportName);
        }
        File outDir = new File(outputDir);
        outDir.mkdirs();
        OutputStream os = new FileOutputStream(new File(outDir, outputFile)); 
        JRMapCollectionDataSource dataSource = new JRMapCollectionDataSource(data); 
        runReportToRtfStream(is, os, null, dataSource);
        os.close();
        is.close();
    }
    
    public void generateText(String outputDir, String outputFile) throws Exception {
        InputStream is = getClass().getResourceAsStream(reportName);
        if(is==null){
        	throw new Exception("No such report "+reportName);
        }
        File outDir = new File(outputDir);
        outDir.mkdirs();
        OutputStream os = new FileOutputStream(new File(outDir, outputFile)); 
        JRMapCollectionDataSource dataSource = new JRMapCollectionDataSource(data); 
        runReportToRtfStream(is, os, null, dataSource);
        os.close();
        is.close();
    }
    
    public static void runReportToRtfStream(InputStream inputStream, 
    		OutputStream outputStream, 
    		Map parameters, 
    		JRDataSource jrDataSource
    		) throws JRException{
    	JasperPrint jasperPrint = JasperFillManager.fillReport(inputStream, parameters, jrDataSource);

		JRRtfExporter exporter = new JRRtfExporter();
		
		exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
		exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, outputStream);
		
		exporter.exportReport();
    }
    
    public static void runReportToTextStream(InputStream inputStream, 
    		OutputStream outputStream, 
    		Map parameters, 
    		JRDataSource jrDataSource
    		) throws JRException{
    	JasperPrint jasperPrint = JasperFillManager.fillReport(inputStream, parameters, jrDataSource);

		JRTextExporter exporter = new JRTextExporter();
		
		exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
		exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, outputStream);
		
		exporter.exportReport();
    }
}

