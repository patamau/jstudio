package jstudio.report;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import jstudio.util.Configuration;
import jstudio.util.Language;

public class ReportGeneratorGUI extends JPanel implements ActionListener {

	private ReportGenerator rg;
	private DefaultTableModel htable, dtable;
	private JButton browseButton, okButton, cancelButton;
	
	public ReportGeneratorGUI(ReportGenerator rg, String filename){
		this.rg = rg;
		String defaultPath = Configuration.getGlobal("print.path.last", ".");
		browseButton = new JButton(defaultPath+File.separator+filename);
		browseButton.addActionListener(this);
		this.add(browseButton);
		okButton = new JButton(Language.string("Ok"));
		okButton.addActionListener(this);
		this.add(okButton);
		cancelButton = new JButton(Language.string("Cancel"));
		cancelButton.addActionListener(this);
		this.add(cancelButton);
		//TODO: add output formats
		//TODO: tables
	}
	
	public void update(){
		//TODO: set head in htable
		//htable.addRow(rowData);
		//TODO: set data in dtable
	}
	
	public void showGUI(Window parent){
		JDialog dialog = new JDialog(parent,Language.string("Print"));
		dialog.add(this);
		dialog.pack();
		dialog.setModal(true);
		dialog.setLocationRelativeTo(parent);
		dialog.setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();
		if(src==browseButton){
			JFileChooser fc = new JFileChooser();
			File f = new File(browseButton.getText());
			fc.setSelectedFile(f);
			fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
			fc.setCurrentDirectory(f);
			fc.showSaveDialog(this);
			File sf = fc.getSelectedFile();
			if(f!=null){
				browseButton.setText(sf.getAbsolutePath());
			}
		}else if(src==cancelButton){
			((Window)SwingUtilities.getRoot(this)).dispose();
		}else if(src==okButton){
			File f = new File(browseButton.getText());
			if(f.exists()){
				int ch = JOptionPane.showConfirmDialog(this,
						Language.string("A file with the same name already exists: confirm overwrite?"),
						Language.string("Overwrite?"),
						JOptionPane.WARNING_MESSAGE);
				if(ch!=JOptionPane.OK_OPTION){
					return;
				}
			}
			try {
				rg.generatePdf(f.getParent(), f.getName());
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
	}
}
