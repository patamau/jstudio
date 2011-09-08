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
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import jstudio.util.Configuration;
import jstudio.util.GUITool;
import jstudio.util.Language;

@SuppressWarnings("serial")
public class ReportGeneratorGUI extends JPanel implements ActionListener {

	public static final String PRINT_PATH_LAST_KEY = "print.path.last";
	
	private ReportGenerator rg;
	private DefaultTableModel htable, dtable;
	private JTextField fileField;
	private JButton browseButton, okButton, cancelButton;
	
	/**
	 * Creates a new window to configure output for the report.
	 * Call showGUI to actually review and print the report
	 * @param rg
	 * @param filename
	 */
	public ReportGeneratorGUI(ReportGenerator rg, String filename){
		this.rg = rg;
		String defaultPath = Configuration.getGlobal(PRINT_PATH_LAST_KEY, ".");
		fileField = new JTextField(defaultPath+File.separator+filename);
		fileField
		browseButton = new JButton("...");
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
			Configuration.getGlobalConfiguration().setProperty(PRINT_PATH_LAST_KEY, browseButton.getText());
			File f = new File(browseButton.getText());
			if(f.exists()){
				int ch = JOptionPane.showConfirmDialog(this,
						Language.string("A file with the same name already exists: confirm overwrite?"),
						Language.string("Overwrite?"),
						JOptionPane.YES_NO_OPTION,
						JOptionPane.WARNING_MESSAGE);
				if(ch!=JOptionPane.YES_OPTION){
					return;
				}
			}else if(!f.getParentFile().exists()){
				JOptionPane.showMessageDialog(this, Language.string("Wrong destination path",Language.string("Print error"), JOptionPane.ERROR_MESSAGE));
				return;
			}
			try {
				rg.generatePdf(f.getParent(), f.getName());
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
	}
}
