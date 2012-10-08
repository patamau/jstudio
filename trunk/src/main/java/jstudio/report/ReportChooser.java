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
	
	public static void main(String args[]){
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
	
	private final JButton printButton, cancelButton;	
	private JComboBox reportsBox;
	private DefaultTableModel tmodel;
	
	private final ReportGenerator rg;

	public ReportChooser(final ReportGenerator rg){
		this.rg = rg;
		NicePanel panel = new NicePanel(Language.string("Custom Report"), Language.string("Print any report with custom data"));
		panel.getBody().setLayout(new BorderLayout());
		this.setLayout(new BorderLayout());
		this.add(panel, BorderLayout.CENTER);
		
		panel.getBody().add(getBody(), BorderLayout.CENTER);
		
		panel.addButtonsGlue();
		printButton = new JButton(Language.string("Print"));
		printButton.addActionListener(this);
		cancelButton = new JButton(Language.string("Cancel"));
		cancelButton.addActionListener(this);
		
		panel.addButton(printButton);
		panel.addButton(cancelButton);
	}
	
	private JPanel getBody(){
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints gc = new GridBagConstraints();
		gc.gridx=gc.gridy=0;
		gc.gridheight=gc.gridwidth=1;
		gc.fill=GridBagConstraints.HORIZONTAL;
		gc.insets=new Insets(4,4,4,4);
		
		panel.add(new JLabel(Language.string("Report")), gc);
		
		String reportsPath = Configuration.getGlobal(REPORTS_PATH_KEY, REPORTS_PATH_DEF);
		String reportsSuffix = Configuration.getGlobal(REPORTS_SUFFIX_KEY, REPORTS_SUFFIX_DEF);
		
		File[] files = Resources.getFiles(reportsPath, reportsSuffix);
		String[] filenames = new String[files.length];
		int i=0;
		for(File f:files){
			filenames[i++]=f.getName();
		}
		reportsBox = new JComboBox(filenames);
		reportsBox.addActionListener(this);
		
		gc.gridx++;
		panel.add(reportsBox, gc);
		
		gc.gridy++;
		gc.gridx=0;
		gc.gridwidth=2;
		tmodel = new DefaultTableModel(new String[]{Language.string("Key"),Language.string("Value")}, 2);
		final JTable t = new JTable(tmodel);
		final JScrollPane scrollpane = new JScrollPane(t);
		scrollpane.setPreferredSize(new Dimension(400,250));
		panel.add(scrollpane, gc);		

		updateTable();
		
		return panel;
	}
	
	private void updateTable(){
		String reportsPath = Configuration.getGlobal(REPORTS_PATH_KEY, REPORTS_PATH_DEF);
		String reportsName = "/"+reportsPath+"/"+reportsBox.getSelectedItem();
		InputStream is = getClass().getResourceAsStream(reportsName);
        if(is==null){
        	throw new RuntimeException("No such report "+reportsName);
        }
    	JasperReport jr;
		try {
			jr = (JasperReport)JRLoader.loadObject(is);
			while(tmodel.getRowCount()>0){
				tmodel.removeRow(0);
			}
	    	for(JRField f : jr.getFields()){
	    		//System.out.println(f.getName());
	    		tmodel.addRow(new String[]{f.getName(), rg.getHead().get(f.getName())});
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
	
	private String getReportName(final String file){
		final int p = file.lastIndexOf('.');
		if(p<0) return file;
		else return file.substring(0, p);
	}
	
	private void updateReportValues(){
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
			rg.setReport("/"+reportsPath+"/"+reportsBox.getSelectedItem());
			updateReportValues();
			ReportGeneratorGUI rggui = new ReportGeneratorGUI(rg, getReportName(reportsBox.getSelectedItem().toString()));
			rggui.showGUI(((Window)SwingUtilities.getRoot(this)));
		}else if(src==cancelButton){
			((Window)SwingUtilities.getRoot(this)).dispose();
		}else if(src==reportsBox){
			updateReportValues();
			updateTable();
		}
	}
}
