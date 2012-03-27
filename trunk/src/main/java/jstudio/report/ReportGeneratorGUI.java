package jstudio.report;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;

import org.apache.log4j.Logger;

import jstudio.gui.generic.NicePanel;
import jstudio.model.Event;
import jstudio.util.Configuration;
import jstudio.util.Language;

@SuppressWarnings("serial")
public class ReportGeneratorGUI extends JPanel implements ActionListener {

	public static final String PRINTPATH_KEY = "print.path";
	private static final int PRV_SIZX=210, PRV_SIZY=297; //A4 standard
	
	private static final Logger logger = Logger.getLogger(ReportGeneratorGUI.class);
	
	private final ReportGenerator rg;
	//private DefaultTableModel htable, dtable;
	private JTextField fileField;
	private JLabel preview;
	private JButton browseButton, printButton, cancelButton;
	private JComboBox formatBox;
	
	public enum PrintMode {
		PdfMode, DocMode, XlsMode
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
		panel.getBody().setLayout(new GridBagLayout());
		this.setLayout(new BorderLayout());
		this.add(panel, BorderLayout.CENTER);
		
		GridBagConstraints gc = new GridBagConstraints();
		gc.gridx=gc.gridy=0;
		panel.getBody().add(getBodyPanel(filename), gc);
		++gc.gridx;
		panel.getBody().add(getPreviewPanel(), gc);
		
		panel.addButtonsGlue();
		printButton = new JButton(Language.string("Print"));
		printButton.addActionListener(this);
		cancelButton = new JButton(Language.string("Cancel"));
		cancelButton.addActionListener(this);
		panel.addButton(printButton);
		panel.addButton(cancelButton);
	}
	
	private Component getBodyPanel(final String filename){
		final JPanel body = new JPanel(new BorderLayout());
		body.add(getFilePanel(filename),BorderLayout.CENTER);
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
		String[] formats = {
				Language.string("Pdf (Portable Document Format)"),
				Language.string("Rtf (Rich Text Format)")
		};
		formatBox = new JComboBox(formats);
		panel.add(formatBox, gc);

		JLabel note = new JLabel(Language.string("Extension added automatically"));
		note.setFont(note.getFont().deriveFont(Font.PLAIN));
		gc.gridy++;
		panel.add(note, gc);
		return panel;
	}
	
	private Component getFilePanel(final String filename){
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), Language.string("Destination")));
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
		browseButton.addActionListener(this);
		panel.add(browseButton,gc);
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
	
	public void doPrint(String destination, final PrintMode mode){
		File f;
		try{
		switch(mode){
		case PdfMode:
			f = new File(checkExtension(destination, "pdf"));		
			if(checkOverwrite(f)){
				rg.generatePdf(f.getParent(), f.getName());
			}
			break;
		case DocMode:
			f = new File(checkExtension(destination, "rtf"));
			if(checkOverwrite(f)){
				rg.generateRtf(f.getParent(), f.getName());
			}
			break;
		case XlsMode:
			f = new File(checkExtension(destination, "xls"));
			if(checkOverwrite(f)){
				//rg.generateRtf(f.getParent(), f.getName());
				JOptionPane.showMessageDialog(this,"INTERNAL: XLS NOT IMPLEMENTED");
			}
			break;
		default:
			logger.error("Unrecognized print mode "+mode);
			break;
		}
		}catch(Exception e){
			logger.error("Cannot print ("+mode+") "+destination,e);
			JOptionPane.showMessageDialog(this, 
					Language.string("While printing {0}: {1}",destination,e.getMessage()), 
					Language.string("Print error"),
					JOptionPane.ERROR_MESSAGE);
		}
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
				fileField.setText(sf.getAbsolutePath());
			}
		}else if(src==cancelButton){
			((Window)SwingUtilities.getRoot(this)).dispose();
		}else if(src==printButton){
			String lastPath;
			int pos = fileField.getText().lastIndexOf(File.separator);
			if(pos<0) lastPath = ".";
			else lastPath = fileField.getText().substring(0, pos);
			Configuration.getGlobalConfiguration().setProperty(PRINTPATH_KEY, lastPath);
			int idx = formatBox.getSelectedIndex();
			switch(idx){
			case 0:
				doPrint(fileField.getText(), PrintMode.PdfMode);
				break;
			case 1:
				doPrint(fileField.getText(), PrintMode.DocMode);
				break;
			default:
				logger.error("Unmapped print mode "+idx);
			}
			((Window)SwingUtilities.getRoot(this)).dispose();
		}
	}
}
