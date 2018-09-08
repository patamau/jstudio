package jstudio.report;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.apache.log4j.Logger;

import jstudio.gui.generic.NicePanel;
import jstudio.util.Configuration;
import jstudio.util.Language;

@SuppressWarnings("serial")
public class ReportGeneratorGUI extends JPanel implements ActionListener {

	public static final String PRINTPATH_KEY = "print.path",
			PRINTPDF_KEY = "print.pdf",
			PRINTDOC_KEY = "print.doc",
			PRINTXLS_KEY = "print.xls",
			PRINTSYS_KEY = "print.sys";
	private static final int PRV_SIZX=210, PRV_SIZY=297; //A4 standard
	
	private static final Logger logger = Logger.getLogger(ReportGeneratorGUI.class);
	
	private final ReportGenerator rg;
	//private DefaultTableModel htable, dtable;
	private JTextField fileField;
	private JLabel preview;
	private JButton browseButton, printButton, cancelButton, sendToPrinterButton, openFolderButton;
	//private JComboBox formatBox;
	private JCheckBox pdfCheck, docCheck, xlsCheck, sysCheck;
	
	public enum PrintMode {
		PdfMode, DocMode, XlsMode, SystemMode
	}
	
	/**
	 * Creates a new window to configure output for the report.
	 * Call showGUI to actually review and print the report
	 * @param rg
	 * @param filename
	 */
	public ReportGeneratorGUI(final ReportGenerator rg, final String filename){
		this.rg = rg;
		
		NicePanel panel = new NicePanel(Language.string("Printing {0}", filename), Language.string("Configure printing"));
		panel.getBody().setLayout(new BorderLayout());
		this.setLayout(new BorderLayout());
		this.add(panel, BorderLayout.CENTER);
		
		final JSplitPane bodyPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, getBodyPanel(filename), getPreviewPanel());
		
		panel.getBody().add(bodyPane, BorderLayout.CENTER);
		
		panel.addButtonsGlue();
		sendToPrinterButton = new JButton(Language.string("Send To Printer..."));
		sendToPrinterButton.addActionListener(this);
		printButton = new JButton(Language.string("Print"));
		printButton.addActionListener(this);
		cancelButton = new JButton(Language.string("Cancel"));
		cancelButton.addActionListener(this);
		
		//panel.addButton(sendToPrinterButton);
		panel.addButton(printButton);
		panel.addButton(cancelButton);
	}
	
	private Component getBodyPanel(final String filename){
		final JPanel body = new JPanel(new GridBagLayout());
		GridBagConstraints gc = new GridBagConstraints();
		gc.gridx=gc.gridy=0;
		gc.weightx=1.0f;
		gc.fill=GridBagConstraints.HORIZONTAL;
		body.add(getFilePanel(filename), gc);
		gc.gridy++;
		body.add(getPrinterPanel(), gc);
		return body;
	}
	
	private Component getPreviewPanel(){
		final JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), Language.string("Preview")));
		try {
			Image img = rg.getPreviewImage();
			preview = new JLabel();
			preview.setIcon(new ImageIcon(ReportGenerator.doResizeImage(img, PRV_SIZX, PRV_SIZY)));
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e);
			JOptionPane.showMessageDialog(this, e.getLocalizedMessage(), Language.string("Preview error"), JOptionPane.ERROR_MESSAGE);
			preview = new JLabel(Language.string("N/A"));
		}
		panel.add(preview, BorderLayout.CENTER);
		return panel;
	}
	
	private Component getFormatsPanel(){
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		//panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), Language.string("Select output format")));
		GridBagConstraints gc = new GridBagConstraints();
		gc.gridx=0;
		gc.gridy=0;
		gc.anchor=GridBagConstraints.LINE_START;
		gc.weightx=1.0f;
		gc.fill=GridBagConstraints.HORIZONTAL;
		
		pdfCheck = new JCheckBox(Language.string("Pdf (Portable Document Format)"));
		pdfCheck.setSelected(Configuration.getGlobal(PRINTPDF_KEY, false));
		gc.gridy++;
		panel.add(pdfCheck, gc);
		docCheck = new JCheckBox(Language.string("Rtf (Rich Text Format)"));
		docCheck.setSelected(Configuration.getGlobal(PRINTDOC_KEY, false));
		gc.gridy++;
		panel.add(docCheck, gc);
		xlsCheck = new JCheckBox(Language.string("Xls (eXeL Spreadsheet)"));
		//xlsCheck.setSelected(Configuration.getGlobal(PRINTXLS_KEY, false));
		//gc.gridy++;
		//panel.add(xlsCheck, gc);

		JLabel note = new JLabel(Language.string("Extensions added automatically"));
		note.setFont(note.getFont().deriveFont(Font.PLAIN));
		gc.gridy++;
		panel.add(note, gc);
		return panel;
	}
	
	private Component getPrinterPanel(){
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), Language.string("Print to system")));
		GridBagConstraints gc = new GridBagConstraints();
		gc.gridx=0;
		gc.gridy=0;
		gc.insets = new Insets(5,5,5,5);
		gc.fill=GridBagConstraints.HORIZONTAL;
		gc.weightx=1.0;
		sysCheck = new JCheckBox(Language.string("Send to printer"));
		sysCheck.setSelected(Configuration.getGlobal(PRINTSYS_KEY, false));
		panel.add(sysCheck, gc);
		gc.gridy++;
		JLabel printerLabel = new JLabel(Language.string("Will open the system printer configuration window"));
		printerLabel.setFont(printerLabel.getFont().deriveFont(Font.PLAIN));
		panel.add(printerLabel,gc);
		return panel;
	}
	
	private Component getFilePanel(final String filename){
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), Language.string("Print to file")));
		GridBagConstraints gc = new GridBagConstraints();
		String defaultPath = Configuration.getGlobal(PRINTPATH_KEY, ".");
		fileField = new JTextField();
		fileField.setText(defaultPath+File.separator+filename);
		fileField.setColumns(30);
		gc.gridx=0;
		gc.gridy=0;
		gc.insets = new Insets(5,5,5,5);
		gc.fill=GridBagConstraints.HORIZONTAL;
		gc.weightx=1.0;
		panel.add(fileField,gc);
		gc.gridx++;
		gc.weightx=0.0;
		browseButton = new JButton("...");
		browseButton.setToolTipText(Language.string("Browse destination"));
		browseButton.addActionListener(this);
		panel.add(browseButton,gc);
		gc.gridx++;
		gc.weightx=0.0;
		openFolderButton = new JButton(UIManager.getIcon("FileView.directoryIcon"));
		openFolderButton.setToolTipText(Language.string("Open destination folder"));
		openFolderButton.addActionListener(this);
		panel.add(openFolderButton, gc);
		gc.gridx=0;
		gc.gridy++;
		gc.gridwidth=2;
		gc.fill=GridBagConstraints.HORIZONTAL;
		gc.weightx=1.0f;
		panel.add(getFormatsPanel(), gc);
		return panel;
	}
	
	private boolean checkOverwrite(File destination){
		if(!destination.exists()) return true;
		int ch = JOptionPane.showConfirmDialog(this,
				Language.string("A file with the same name already exists ({0}): confirm overwrite?",destination.getName()),
				Language.string("Overwrite?"),
				JOptionPane.YES_NO_OPTION,
				JOptionPane.WARNING_MESSAGE);
		if(ch!=JOptionPane.YES_OPTION){
			return false;
		}else{
			return true;
		}
	}
	
	private String checkExtension(String src, String ext){
		ext = "."+ext;
		int last = src.lastIndexOf(ext);
		if(last<0) return src+ext;
		else return src;
	}
	
	public boolean doPrint(String destination, final PrintMode mode){
		File f;
		boolean printed = false;
		try{
			switch(mode){
			case PdfMode:
				f = new File(checkExtension(destination, "pdf"));		
				if(checkOverwrite(f)){
					rg.generatePdf(f.getParent(), f.getName());
					printed=true;
				}
				break;
			case DocMode:
				f = new File(checkExtension(destination, "rtf"));
				if(checkOverwrite(f)){
					rg.generateRtf(f.getParent(), f.getName());
					printed=true;
				}
				break;
			case XlsMode:
				f = new File(checkExtension(destination, "xls"));
				if(checkOverwrite(f)){
					rg.generateXls(f.getParent(), f.getName());
					printed=true;
				}
				break;
			case SystemMode:
				rg.sendToPrinter();
				printed=true;
				break;
			default:
				logger.error("Unrecognized print mode "+mode);
				break;
			}
		}catch(Exception e){
			File fd = new File(destination);
			logger.error("Cannot print ("+mode+") "+destination,e);
			JOptionPane.showMessageDialog(this, 
					Language.string("While printing {0}: {1}",fd.getName(),e.getMessage()), 
					Language.string("Print error"),
					JOptionPane.ERROR_MESSAGE);
		}
		return printed;
	}
	
	public void showGUI(Window parent){
		JDialog dialog = new JDialog(parent,Language.string("Print"));
		dialog.add(this);
		dialog.pack();
		dialog.setModal(true);
		dialog.setLocationRelativeTo(parent);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();
		if(src==browseButton){
			JFileChooser fc = new JFileChooser(new File("."));
			File f = new File(this.fileField.getText());
			fc.setSelectedFile(f);
			fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
			int ch = fc.showSaveDialog(this);
			File sf = fc.getSelectedFile();
			if(ch==JFileChooser.APPROVE_OPTION&&f!=null){
				fileField.setText(sf.getAbsolutePath());
			}
		}else if(src==cancelButton){
			((Window)SwingUtilities.getRoot(this)).dispose();
		}else if(src==sendToPrinterButton){
			String lastPath;
			int pos = fileField.getText().lastIndexOf(File.separator);
			if(pos<0) {
				lastPath = ".";
			} else {
				lastPath = fileField.getText().substring(0, pos);
			}
			Configuration.getGlobalConfiguration().setProperty(PRINTPATH_KEY, lastPath);
			try {
				rg.sendToPrinter();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}else if(src==printButton){
			String lastPath;
			int pos = fileField.getText().lastIndexOf(File.separator);
			if(pos<0) lastPath = ".";
			else lastPath = fileField.getText().substring(0, pos);
			Configuration.getGlobalConfiguration().setProperty(PRINTPATH_KEY, lastPath);
			boolean pres = false, sel = false;
			Configuration.getGlobalConfiguration().setProperty(PRINTPDF_KEY, pdfCheck.isSelected());
			Configuration.getGlobalConfiguration().setProperty(PRINTDOC_KEY, docCheck.isSelected());
			Configuration.getGlobalConfiguration().setProperty(PRINTXLS_KEY, xlsCheck.isSelected());
			Configuration.getGlobalConfiguration().setProperty(PRINTSYS_KEY, sysCheck.isSelected());
			if(pdfCheck.isSelected()){
				pres = doPrint(fileField.getText(), PrintMode.PdfMode);
				sel = true;
			}
			if(docCheck.isSelected()){
				pres = doPrint(fileField.getText(), PrintMode.DocMode);
				sel = true;
			}
			if(xlsCheck.isSelected()){
				pres = doPrint(fileField.getText(), PrintMode.XlsMode);
				sel = true;
			}
			if(sysCheck.isSelected()){
				pres = doPrint(fileField.getText(), PrintMode.SystemMode);
				sel = true;
			}
			if(pres){
				((Window)SwingUtilities.getRoot(this)).dispose();
			}else if(!sel){
				JOptionPane.showMessageDialog(this, Language.string("Select at least one print option to print the report"), Language.string("Select print options"), JOptionPane.WARNING_MESSAGE);
			}
		}else if(src==openFolderButton) {
			File file = new File (fileField.getText());
			if(!file.exists()) {
				file = file.getParentFile();
			}
			Desktop desktop = Desktop.getDesktop();
			try {
				desktop.open(file);
			} catch (IOException e1) {
				logger.error(e1);
			}
		}
	}
}
