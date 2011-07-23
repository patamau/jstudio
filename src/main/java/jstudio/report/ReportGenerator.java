package jstudio.report;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
 
import net.sf.jasperreports.engine.JasperRunManager;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
 
/**
 * Hello world!
 *
 */
public class ReportGenerator {
	
	public static void main(String args[]){
		//TODO
	}
 
    public void sampleReport(String outputDir) throws Exception {
        // carica il report compilato
        InputStream is = getClass().getResourceAsStream("/report1.jasper");
 
        // imposta directory e file di output
        File outDir = new File(outputDir);
        outDir.mkdirs();
        OutputStream os = new FileOutputStream(new File(outDir, "report1.pdf"));
 
        // crea una java.util.Collection contenente dei dati
        Collection data = createSampleData();
        // la avvolge in un'implementazione di JRDataSource
        JRMapCollectionDataSource dataSource = new JRMapCollectionDataSource(data);
 
        JasperRunManager.runReportToPdfStream(is, os, null, dataSource);
    }
 
    private Collection createSampleData() {
        Map row;
        Collection data = new ArrayList();
 
        row = new HashMap();
        row.put("name", "Lorem");
        data.add(row);
 
        row = new HashMap();
        row.put("name", "Ipsum");
        data.add(row);
 
        row = new HashMap();
        row.put("name", "Docet");
        data.add(row);
 
        return data;
    }
}

