package jstudio.report;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.MediaPrintableArea;
import javax.print.attribute.standard.MediaSize;
import javax.print.attribute.standard.MediaSizeName;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import jstudio.db.DatabaseObject;
import jstudio.model.DummyObject;
import jstudio.model.Invoice;
import jstudio.model.Product;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporter;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperPrintManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.JasperRunManager;
import net.sf.jasperreports.engine.base.JRBaseReport;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import net.sf.jasperreports.engine.export.JRPrintServiceExporter;
import net.sf.jasperreports.engine.export.JRPrintServiceExporterParameter;
import net.sf.jasperreports.engine.export.JRRtfExporter;
import net.sf.jasperreports.engine.export.JRTextExporter;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.engine.util.JRReportUtils;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
 
/**
 * Hello world!
 *
 */
public class ReportGenerator {
	
	private static final Logger logger = Logger.getLogger(ReportGenerator.class);
	
	public static void main(String args[]){		
		BasicConfigurator.configure();
		ReportGenerator rg = new ReportGenerator();
		rg.setReport("/reports/invoice.jasper");
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
		rg.setHeadValue("note", "Questa fattura e' conforme agli standard 675/10");
		//rg.setData(i.getProducts());
		for(Product pe: i.getProducts()){
			rg.addData(pe.getPrintData());
		}
		//rg.setHeadValue("stamp", Float.toString(1.80f));
		//rg.setHeadValue("totalcost", Float.toString(350f));
		rg.setHeadValue("totalcost", NumberFormat.getCurrencyInstance().format(350f));
		//rg.setHeadValue("stamp", Float.toString(1.80f));
		rg.setHeadValue("stamp", NumberFormat.getCurrencyInstance().format(1.8f));
		try {
			rg.generatePdf(".","sampleReportInvoice.pdf");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(800,600);
		frame.setLocationRelativeTo(null);
		
		final Image img;
		try {
			img = rg.getPreviewImage();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		
		final ImageIcon icon = new ImageIcon(img); //ReportGenerator.doResizeImage(img, 420, 594);
		final JLabel lbl = new JLabel(icon);
		frame.getContentPane().add(lbl);
		ImageMouseController imc = new ImageMouseController(img, lbl, img.getWidth(null), img.getHeight(null));
		frame.getContentPane().addMouseMotionListener(imc);
		frame.getContentPane().addMouseListener(imc);
		frame.setVisible(true);
		//System.exit(0);
	}
	
	private static class ImageMouseController implements MouseListener, MouseMotionListener{
		private int x=-1, y=-1, w, h ,dx, dy;
		private Image panimg;
		private JLabel lbl;
		
		public ImageMouseController(final Image img, final JLabel label, final int w, final int h){
			panimg = img;
			lbl = label;
			this.w = w;
			this.h = h;
		}
		
		@Override
		public void mouseDragged(MouseEvent e) {
			dx += e.getX()-x;
			dy += e.getY()-y;
			lbl.setIcon(new ImageIcon(doPanImage(panimg,dx,dy,w,h)));
			lbl.invalidate();
			lbl.revalidate();
			x = e.getX();
			y = e.getY();
		}
		@Override
		public void mouseMoved(MouseEvent e) {}
		@Override
		public void mouseClicked(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}
		@Override
		public void mousePressed(MouseEvent e) {
			x=e.getX();
			y=e.getY();
		}
		@Override
		public void mouseReleased(MouseEvent e) {
			x=y=0;
		}
		@Override
		public void mouseEntered(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}
		@Override
		public void mouseExited(MouseEvent e) {
			// TODO Auto-generated method stub
			
		} 
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
		head.putAll(entry.getPrintData());
	}
	
	public Map<String, String> getHead(){
		return head;
	}
	
	/**
	 * Custom method
	 * @param entry
	 */
	public void addData(Map<String, String> entry){
		if(this.head.size()>0){
			entry.putAll(this.head);
			this.head.clear();
		}
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
			row.putAll(e.getPrintData());
    		this.data.add(row);
    		row = new HashMap<String,String>();
		}
	}
	
	public static Image doPanImage(final Image image, final int offsetx, final int offsety, final int w, final int h){
		logger.debug("Panning image to "+offsetx+","+offsety);
		BufferedImage resizedImage = new BufferedImage(w, h, BufferedImage.TYPE_USHORT_GRAY);
		Graphics2D g = resizedImage.createGraphics();
		g.setComposite(AlphaComposite.Src);
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		g.drawImage(image, offsetx, offsety, w, h, null);
		g.dispose();		
		return resizedImage;
	}
	
	public static Image doResizeImage(final Image image, final int width, final int height){
		BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_USHORT_GRAY);
		Graphics2D g = resizedImage.createGraphics();
		g.setComposite(AlphaComposite.Src);
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		g.drawImage(image, 0, 0, width, height, null);
		g.dispose();		
		return resizedImage;
	}
	
	public Image getPreviewImage() throws Exception {
		InputStream is = getClass().getResourceAsStream(reportName);
        if(is==null){
        	throw new Exception("No such report "+reportName);
        }
		JasperPrint print = JasperFillManager.fillReport(is, null, getDataSource()); 
    	JasperReport report = (JasperReport) JRLoader.loadObject(getClass().getResourceAsStream(reportName));
		return JasperPrintManager.printPageToImage(print, 0, 1.0f);
	}
	
	private JRDataSource getDataSource(){
		if(data.size()==0){
			List<DummyObject> dummyData = new ArrayList<DummyObject>(1);
			dummyData.add(new DummyObject());
			setData(dummyData);
		}
		return new JRMapCollectionDataSource(data);
	}
 
    public void generatePdf(String outputDir, String outputFile) throws Exception {
        InputStream is = getClass().getResourceAsStream(reportName);
        if(is==null){
        	throw new Exception("No such report "+reportName);
        }
        File outDir = new File(outputDir);
        if(!outDir.exists()){
        	logger.warn("Creating output dir "+outputDir);
        	outDir.mkdirs();
        }
        File f = new File(outDir, outputFile);
        OutputStream os = new FileOutputStream(f); 
        JasperRunManager.runReportToPdfStream(is, os, null, getDataSource());
        os.close();
        is.close();
    }
    
    public void generateRtf(String outputDir, String outputFile) throws Exception {
        InputStream is = getClass().getResourceAsStream(reportName);
        if(is==null){
        	throw new Exception("No such report "+reportName);
        }
        File outDir = new File(outputDir);
        if(!outDir.exists()){
        	logger.warn("Creating output dir "+outputDir);
        	outDir.mkdirs();
        }
        OutputStream os = new FileOutputStream(new File(outDir, outputFile)); 
        runReportToRtfStream(is, os, null, getDataSource());
        os.close();
        is.close();
    }
    
    public void generateXls(String outputDir, String outputFile) throws Exception {
        InputStream is = getClass().getResourceAsStream(reportName);
        if(is==null){
        	throw new Exception("No such report "+reportName);
        }
        File outDir = new File(outputDir);
        if(!outDir.exists()){
        	logger.warn("Creating output dir "+outputDir);
        	outDir.mkdirs();
        }
        OutputStream os = new FileOutputStream(new File(outDir, outputFile)); 
        //runReportToRtfStream(is, os, null, getDataSource());
        runReportToXlsStream(is, os, null, getDataSource());
        os.close();
        is.close();
    }
    
    public void generateText(String outputDir, String outputFile) throws Exception {
        InputStream is = getClass().getResourceAsStream(reportName);
        if(is==null){
        	throw new Exception("No such report "+reportName);
        }
        File outDir = new File(outputDir);
        if(!outDir.exists()){
        	logger.warn("Creating output dir "+outputDir);
        	outDir.mkdirs();
        }
        OutputStream os = new FileOutputStream(new File(outDir, outputFile)); 
        JRMapCollectionDataSource dataSource = new JRMapCollectionDataSource(data); 
        runReportToRtfStream(is, os, null, dataSource);
        os.close();
        is.close();
    }
    
    public void sendToPrinter() throws Exception {
        InputStream is = getClass().getResourceAsStream(reportName);
        if(is==null){
        	throw new Exception("No such report "+reportName);
        }
        JRMapCollectionDataSource dataSource = new JRMapCollectionDataSource(data); 
        runReportToPrinter(is, null, dataSource);
        is.close();
    }   
    
    public static void runReportToXlsStream(InputStream inputStream, 
    		OutputStream outputStream, 
    		Map parameters, 
    		JRDataSource jrDataSource
    		) throws JRException{
    	JasperPrint jasperPrint = JasperFillManager.fillReport(inputStream, parameters, jrDataSource);
		JRXlsExporter exporter = new JRXlsExporter();
		exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
		exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, outputStream);
		
		exporter.exportReport();
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
    
    public static void runReportToPrinter(InputStream inputStream, 
    		Map parameters, 
    		JRDataSource jrDataSource
    		) throws JRException{
    	JasperPrint jasperPrint = JasperFillManager.fillReport(inputStream, parameters, jrDataSource);
    	
    	PrinterJob job = PrinterJob.getPrinterJob();
    	/* Create an array of PrintServices */
    	PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
    	int selectedService = 0;
    	/* Scan found services to see if anyone suits our needs */
    	for(int i = 0; i < services.length;i++){
	    	if(services[i].getName().toUpperCase().contains("Your printer's name")){
		    	/*If the service is named as what we are querying we select it */
		    	selectedService = i;
	    	}
    	}
    	try {
			job.setPrintService(services[selectedService]);
		} catch (PrinterException e) {
			e.printStackTrace();
			return;
		}
    	PrintRequestAttributeSet printRequestAttributeSet = new HashPrintRequestAttributeSet();
    	MediaSizeName mediaSizeName = MediaSize.findMedia(4,4,MediaPrintableArea.INCH);
    	printRequestAttributeSet.add(mediaSizeName);
    	printRequestAttributeSet.add(new Copies(1));
    	JRPrintServiceExporter exporter;
    	exporter = new JRPrintServiceExporter();
    	exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
    	/* We set the selected service and pass it as a paramenter */
    	exporter.setParameter(JRPrintServiceExporterParameter.PRINT_SERVICE, services[selectedService]);
    	exporter.setParameter(JRPrintServiceExporterParameter.PRINT_SERVICE_ATTRIBUTE_SET, services[selectedService].getAttributes());
    	exporter.setParameter(JRPrintServiceExporterParameter.PRINT_REQUEST_ATTRIBUTE_SET, printRequestAttributeSet);
    	exporter.setParameter(JRPrintServiceExporterParameter.DISPLAY_PAGE_DIALOG, Boolean.FALSE);
    	exporter.setParameter(JRPrintServiceExporterParameter.DISPLAY_PRINT_DIALOG, Boolean.TRUE);
    	exporter.exportReport();
    }
}

