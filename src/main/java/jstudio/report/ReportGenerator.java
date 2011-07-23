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

import javax.swing.JOptionPane;

import jstudio.JStudio;
import jstudio.control.Agenda;
import jstudio.control.Contacts;
import jstudio.db.DatabaseInterface;
import jstudio.db.HibernateDB;
import jstudio.model.Person;
import jstudio.util.Configuration;
import jstudio.util.Language;
 
import net.sf.jasperreports.engine.JasperRunManager;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
 
/**
 * Hello world!
 *
 */
public class ReportGenerator<Entry> {
	
	public static void main(String args[]){
		initializeConfiguration();
		ReportGenerator<Person> rg = new ReportGenerator<Person>();
		rg.setReport("/report1.jasper");
		Contacts cts = new Contacts(initializeData());
		rg.setData(cts.getAll());
		try {
			rg.generatePdf(".","sampleReportPerson.pdf");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void initializeConfiguration(){
		//initialize configuration loading the configuration file
		Configuration configuration = new Configuration();
		configuration.load(JStudio.class.getSimpleName()+Configuration.FILE_SUFFIX);
		Language.setCurrentLanguage(
				configuration.getProperty(
						"language",
						Language.getCurrentLanguage().getCode()
					)
				);
		//configuration is set globally
		Configuration.setGlobalConfiguration(configuration);
	}
	
	private static DatabaseInterface initializeData(){
		DatabaseInterface database;
		//initialize database manager
		String protocol = Configuration.getGlobal(DatabaseInterface.KEY_PROTOCOL,DatabaseInterface.DEF_PROTOCOL);
		String driver = Configuration.getGlobal(DatabaseInterface.KEY_DRIVER,DatabaseInterface.DEF_DRIVER);
		database = new HibernateDB(protocol, driver);
		String hostname = Configuration.getGlobal(DatabaseInterface.KEY_HOST, DatabaseInterface.DEF_HOST);
		String dbname = Configuration.getGlobal(DatabaseInterface.KEY_NAME, DatabaseInterface.DEF_NAME);
		String user = Configuration.getGlobal(DatabaseInterface.KEY_USER, DatabaseInterface.DEF_USER);
		String password = Configuration.getGlobal(DatabaseInterface.KEY_PASS, DatabaseInterface.DEF_PASS);
		try {
			database.connect(hostname, dbname, user, password);
		} catch (Throwable e) {
			JOptionPane.showMessageDialog(null, Language.string("Database connection error")+": "+e.getLocalizedMessage(), Language.string("Data initialization"), JOptionPane.ERROR_MESSAGE);
		}
		return database;
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

