package jstudio.report;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import jstudio.gui.generic.NicePanel;
import jstudio.model.Person;
import jstudio.util.Configuration;
import jstudio.util.Language;
import jstudio.util.Resources;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;

public class ReportChooser extends JPanel implements ActionListener {
	
	private static final long serialVersionUID = 5667769305998774452L;
	
	private static final Logger logger = Logger.getLogger(ReportChooser.class);

	public static void main(String args[]){
		BasicConfigurator.configure();
		Object o = Resources.getFile("reports/report1.jasper");
		System.out.println(o);
		for(File f: Resources.getFiles("reports", ".jasper")){
			System.out.println(f.getName());
		}

		JFrame frame = new JFrame("Ciao");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(400,400);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		ReportGenerator rg = new ReportGenerator();
		rg.setHead(new Person());
		ReportChooser rc = new ReportChooser(rg);
		rc.showGUI(frame);
	}
	
	public static final String 
		REPORTS_PATH_KEY = "reports.path",
		REPORTS_SUFFIX_KEY = "reports.suffix";
		
	public static final String
		REPORTS_PATH_DEF = "reports",
		REPORTS_SUFFIX_DEF = ".jasper";
	
	private final JButton printButton, cancelButton, rrefreshButton;	
	private JComboBox reportsBox;
	private JTable table;
	private DefaultTableModel tmodel;
	private final ArrayList<String> filenames;
	
	private final ReportGenerator rg;

	public ReportChooser(final ReportGenerator rg){
		this.rg = rg;
		this.filenames = new ArrayList<String>();
		
		NicePanel panel = new NicePanel(Language.string("Custom Report"), Language.string("Print any report with custom data"));
		panel.getBody().setLayout(new BorderLayout());
		this.setLayout(new BorderLayout());
		this.add(panel, BorderLayout.CENTER);
		
		rrefreshButton = new JButton(Language.string("Refresh"));
		rrefreshButton.addActionListener(this);
		
		panel.getBody().add(getBody(), BorderLayout.CENTER);
		
		panel.addButtonsGlue();
		printButton = new JButton(Language.string("Print"));
		printButton.addActionListener(this);
		cancelButton = new JButton(Language.string("Cancel"));
		cancelButton.addActionListener(this);
		
		panel.addButton(printButton);
		panel.addButton(cancelButton);
	}
	
	private void updateReports(){
		DefaultComboBoxModel model = (DefaultComboBoxModel)reportsBox.getModel();
		model.removeAllElements();
		filenames.clear();
		
		String reportsPath = Configuration.getGlobal(REPORTS_PATH_KEY, REPORTS_PATH_DEF);
		String reportsSuffix = Configuration.getGlobal(REPORTS_SUFFIX_KEY, REPORTS_SUFFIX_DEF);
		File[] files = Resources.getFiles(reportsPath, reportsSuffix);
		for(File f:files){
			filenames.add(f.getName());
			model.addElement(getReportName(f.getName())+" ("+f.getName()+")");
		}
	}
	
	private JPanel getBody(){
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints gc = new GridBagConstraints();
		gc.gridx=gc.gridy=0;
		gc.gridheight=gc.gridwidth=1;
		gc.fill=GridBagConstraints.HORIZONTAL;
		gc.weightx=0f;
		gc.insets=new Insets(4,4,4,4);
		
		panel.add(new JLabel(Language.string("Report")), gc);
		
		reportsBox = new JComboBox();
		reportsBox.addActionListener(this);
		
		gc.gridx++;
		gc.weightx=1f;
		panel.add(reportsBox, gc);
		gc.gridx++;
		gc.weightx=0f;
		gc.fill=GridBagConstraints.NONE;
		panel.add(rrefreshButton, gc);
		
		gc.gridy++;
		gc.gridx=0;
		gc.weightx=1f;
		gc.fill=GridBagConstraints.HORIZONTAL;
		gc.gridwidth=3;
		tmodel = new ReportDataModel(new String[]{Language.string("Key"),Language.string("Value")}, 2);
		table = new JTable(tmodel);
		final JScrollPane scrollpane = new JScrollPane(table);
		scrollpane.setPreferredSize(new Dimension(400,250));
		panel.add(scrollpane, gc);		

		updateTable();
		updateReports();
		
		return panel;
	}
	
	private JasperReport getReport(final String filename) throws JRException {
		if(filename==null) return null;
		String reportsPath = Configuration.getGlobal(REPORTS_PATH_KEY, REPORTS_PATH_DEF);
		String reportsName = "/"+reportsPath+"/"+filename;
		InputStream is = getClass().getResourceAsStream(reportsName);
        if(is==null){
        	throw new RuntimeException("No such report "+reportsName);
        }
    	return (JasperReport)JRLoader.loadObject(is);
	}
	
	private void updateTable(){
		try {
			if(filenames.size()==0||reportsBox.getSelectedIndex()<0) return;
			logger.debug(filenames.get(reportsBox.getSelectedIndex()));
			JasperReport jr = getReport(filenames.get(reportsBox.getSelectedIndex()));
			if(jr==null) return;
			while(tmodel.getRowCount()>0){
				tmodel.removeRow(0);
			}
			//System.err.println(jr.getName());
			if(jr.getFields()!=null){
		    	for(JRField f : jr.getFields()){
		    		//System.out.println(f.getName());
		    		String val = rg.getHead().get(f.getName());
		    		if(val==null) val = "";
		    		tmodel.addRow(new String[]{f.getName(), val});
		    	}
			}
		} catch (JRException e) {
			e.printStackTrace();
		}
	}
	
	public void showGUI(final Window parent){
		JDialog dialog = new JDialog(parent,Language.string("Report Chooser"));
		dialog.add(this);
		dialog.pack();
		dialog.setModal(true);
		dialog.setLocationRelativeTo(parent);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.setVisible(true);
	}
	
	private String getReportName(final String report){
		try{
			return getReport(report).getName();
		}catch(JRException e){
			throw new RuntimeException(e);
		}
	}
	
	private String getPrintName(final String report){
		final int p = report.lastIndexOf('.');
		if(p<0) return report;
		else return report.substring(0, p);
	}
	
	private void updateReportValues(){
		TableCellEditor editor = table.getCellEditor();
		if (editor != null) {
		  editor.stopCellEditing();
		}
		
		for(int i=0; i<tmodel.getRowCount(); ++i){
			String k = (String)tmodel.getValueAt(i, 0);
			String v = (String)tmodel.getValueAt(i, 1);
			rg.setHeadValue(k, v);
		}
	}

	@Override
	public void actionPerformed(ActionEvent ev) {
		Object src = ev.getSource();
		if(src==printButton){
			String reportsPath = Configuration.getGlobal(REPORTS_PATH_KEY, REPORTS_PATH_DEF);
			rg.setReport("/"+reportsPath+"/"+filenames.get(reportsBox.getSelectedIndex()));
			updateReportValues();
			ReportGeneratorGUI rggui = new ReportGeneratorGUI(rg, getPrintName(filenames.get(reportsBox.getSelectedIndex())));
			rggui.showGUI(((Window)SwingUtilities.getRoot(this)));
		}else if(src==cancelButton){
			((Window)SwingUtilities.getRoot(this)).dispose();
		}else if(src==reportsBox){
			updateReportValues();
			updateTable();
		}else if(src==rrefreshButton){
			updateReportValues();
			updateReports();
		}
	}
}
