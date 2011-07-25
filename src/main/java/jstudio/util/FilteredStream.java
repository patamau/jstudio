package jstudio.util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

/**
 * The filtered stream offers three levels of filtering:<br>
 * The first is a debug console that the user can read even if the classic console is not visible<br>
 * The second is a warning message that allows user to be notified when an exception is thrown<br>
 * The third is the ability to store the linked stream to a log file<br>
 * <br>
 * Here's a sample code that shows how to use it to override standard output<br>
 * <pre>
 * PrintStream filteredPrintStream  =  new PrintStream(new FilteredStream(new ByteArrayOutputStream(),log_file,debug));
 * System.setErr(filteredPrintStream);
 * System.setOut(filteredPrintStream);
 * </pre>
 * @author Matteo Pedrotti
 *
 */
public class FilteredStream extends FilterOutputStream implements ActionListener, UncaughtExceptionHandler{

	public static final String 
		FRAME_TITLE = "Console",
		LOGLOCK_KEY = "log.lock",
		LOGSIZE_KEY = "log.size";
	public static final int LOGSIZE_DEF = 10000;
	public static final boolean LOGLOCK_DEF = false;
	private static final String NEW_LINE_STR = "\n";
	private static int rowsLimit = 1000;

	//GUI related stuff
	private JFrame frame;
	private JTextField inputRowLimit = new JTextField();
	private JTextArea textArea = new JTextArea();
	private JButton clearButton;
	private JToggleButton lockButton;
	
	//file related stuff
	private FileWriter aWriter;
	private StringBuffer buffer; //the buffered string when messages are not carriage returned
	private long rowsCounter=0;
	
	/**
	 * Creates a standard filtered stream that will only popup a window when an exception is thrown
	 * @param aStream
	 */
	public FilteredStream(OutputStream aStream){
		this(aStream,false);
	}
	
	/**
	 * Creates a filtered stream that will create a console to show standard output and error if the debug flag is set to true<br>
	 * No log file is created
	 * @param aStream
	 * @param debug
	 */
	public FilteredStream(OutputStream aStream, boolean showgui){
		this(aStream,null,showgui);
	}
	
	/**
	 * Creates a filtered stream for the given stream<br>
	 * If filename is not null, a log file will be registered at the given path<br>
	 * If showgui is true, a console window will be shown
	 * @param aStream the stream to use
	 * @param filename the filename to write the log into (null ignores)
	 * @param debug show debug window
	 */
    public FilteredStream(OutputStream aStream, String filename, boolean showgui) {
       super(aStream);
       buffer = new StringBuffer();
       if(filename!=null){
    	   initStream(filename);
       }       
       if(showgui){
    	   showGUI();
       }
    }
    
    public boolean isGUIVisible(){
    	if(frame!=null){
    		return frame.isVisible();
    	}else{
    		return false;
    	}
    }
    
    public void showGUI(){
    	synchronized(this){
    		if(frame==null){
    			initFrame(FRAME_TITLE);
    		}
    		//set frame visible
    		frame.setVisible(true);
    	}
    }
    
    /**
     * If frame was initialized 
     * and was visible
     * hides it (frame methods still work)
     */
    public void hideGUI(){
    	synchronized(this){
	    	if(frame!=null){
	    		frame.setVisible(false);
	    	}
    	}
    }
    
    private void initStream(String filename){
       try {
    	   aWriter = new FileWriter(filename,false);
       } catch (IOException e) {
    	   JOptionPane.showMessageDialog(null, "Unable to write log file "+filename+": "+e, "Log file error",JOptionPane.WARNING_MESSAGE);
    	   e.printStackTrace();
       }
    }
    
    private void initFrame(final String frame_title){
    	//synchronize!
    	//wait for frame to be created before start using it
    	synchronized(this){
	    	frame = new JFrame(frame_title);
	    	frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	    	Vector<String> cols = new Vector<String>();
	    	cols.add("Message");
	    	frame.getContentPane().setLayout(new BorderLayout());
	    	textArea.setEditable(false);
	    	textArea.setLineWrap(true);
	    	frame.getContentPane().setBackground(new Color(0,0,0,0));
	    	JScrollPane spane = new JScrollPane(textArea);
	    	frame.getContentPane().add(spane,BorderLayout.CENTER);
	    	JToolBar toolBar = new JToolBar();
	    	toolBar.setLayout(new GridBagLayout());
	    	GridBagConstraints gc = new GridBagConstraints();
	    	gc.gridx=0;
	    	gc.gridy=0;
	    	gc.fill=GridBagConstraints.HORIZONTAL;
	    	gc.insets = new Insets(0,4,0,4);
	    	toolBar.setFloatable(false);
	
	    	inputRowLimit = new JTextField();
	    	inputRowLimit.setText(Integer.toString(rowsLimit));
	    	inputRowLimit.setColumns(6);
	    	inputRowLimit.setToolTipText("Input the chars limit and press <enter>");
	    	inputRowLimit.addActionListener(this);
	    	gc.weightx=0.0;
	    	toolBar.add(inputRowLimit,gc);
	    	clearButton = new JButton("Clear");
	    	clearButton.addActionListener(this);
	    	gc.gridx++;
	    	toolBar.add(clearButton,gc);
	    	lockButton = new JToggleButton("Lock");
	    	lockButton.setSelected(Configuration.getGlobal(LOGLOCK_KEY,LOGLOCK_DEF));
	    	lockButton.addActionListener(this);
	    	gc.gridx++;
	    	toolBar.add(lockButton,gc);
	    	frame.getContentPane().add(toolBar, BorderLayout.SOUTH);
	    	frame.setSize(500, 400);
	    	frame.setLocationRelativeTo(null);
    	}
    }

    public void write(byte b[]) throws IOException {
       String aString = new String(b);
       filter(aString);
    }

    public void write(byte b[], int off, int len) throws IOException {
       String aString = new String(b , off , len);
       filter(aString);
    }
    
    /**
     * This thread is used to keep the message showing even if the program has blocked<br>
     * Nothing more unpredictable than an exception!<br>
     * It's not nice to show an exception and the user cannot interact<br>
     * or even worse user is not able to quit the program leaving a zombie process!
     * @author Matteo Pedrotti
     *
     */
    private static class ShowExceptionThread implements Runnable{
    	
    	private String aString;
    	private static final String PANE_TITLE = "Exception";
    	private static Thread runningThread = null;
    	private static String lastMessage = null;
    	
    	/**
    	 * Prepares a new runnable that will show the given message as soon as the thread is scheduled
    	 * @param message
    	 */
    	private ShowExceptionThread(String message){
    		this.aString=message;
    	}
    	
    	public void run(){
    		JOptionPane.showMessageDialog(null,aString,PANE_TITLE,JOptionPane.ERROR_MESSAGE);
    	}
    	
    	/**
    	 * A message will be shown in a separate window
    	 * @param message
    	 */
    	public static synchronized void showException(String message){
    		if(runningThread==null){
    			runningThread = new Thread(new ShowExceptionThread(message));
    			runningThread.start();
    		}else if(!lastMessage.equals(message)){
    			//check to ignore new window (it's a duplicated message)
    			runningThread = new Thread(new ShowExceptionThread(message));
    			runningThread.start();
    		}
    		lastMessage = message;
    	}
    }
    
	private void appendArea(final String msg, final JTextArea area){
		SwingUtilities.invokeLater(new Runnable(){
			public void run(){
				int cp = area.getCaretPosition();
				area.append(msg);
		
		        // Keep the text area down to a certain character size
		        int idealSize = Configuration.getGlobal(LOGSIZE_KEY, LOGSIZE_DEF);
		        int maxExcess = 10;
		        int excess = area.getDocument().getLength() - idealSize;
		        if (excess >= maxExcess) {
		            area.replaceRange(null, 0, excess);
		        }
		        
				// Make sure the last line is always visible
				if(!Configuration.getGlobal(LOGLOCK_KEY, LOGLOCK_DEF)){
					area.setCaretPosition(area.getDocument().getLength());
				}else{
					area.setCaretPosition(cp);
				}
			}
		});
	}
    
    private void insertMessage(String message){
    	synchronized(this){
    		//check frame is being used
    		if(frame==null) return;
    		//message = message.trim();
    		if(message.length()==0) return;
    		rowsCounter++;
    		appendArea(message,textArea);
    	}
    }
    
    /**
     * Decide where to send the given string.
     * @param aString
     * @throws IOException
     */
    private void filter(String aString) throws IOException{
    	/*if(buffer.length()==0){
    		buffer.append("[");
    		buffer.append(new Date());
    		buffer.append("] ");
    	}*/
		buffer.append(aString);
		//wait for new line
    	if(!aString.endsWith(NEW_LINE_STR)){
    		return;
    	}
    	aString = buffer.toString();
    	buffer.setLength(0);

    	insertMessage(aString);
       
       if(aWriter!=null){
    	   try{
    		   aWriter.write(aString);
    		   aWriter.flush();
    	   }catch(IOException ioe){
    		   JOptionPane.showMessageDialog(null, ioe);
    	   }
	   }
    }
    
    /**
     * Closes the stream
     */
    public void close() throws IOException{
    	if(aWriter!=null) aWriter.close();
    	super.close();
    }

	public void actionPerformed(ActionEvent e) {
		synchronized(this){
			Object source = e.getSource();
			if(source==inputRowLimit){
				try{
					Integer.parseInt(inputRowLimit.getText());
					Configuration.getGlobalConfiguration().setProperty(LOGSIZE_KEY, inputRowLimit.getText());
				}catch(NumberFormatException nfe){
					inputRowLimit.setText(String.valueOf(Configuration.getGlobal(LOGSIZE_KEY,LOGSIZE_DEF)));
				}
			}else if(source==clearButton){
				textArea.setText("");
			}else if(source==lockButton){
				Configuration.getGlobalConfiguration().setProperty(LOGLOCK_KEY, String.valueOf(lockButton.isSelected()));
			}
		}
	}

	/**
	 * Catches all the exceptions generated
	 */
	public void uncaughtException(Thread t, Throwable e) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("Exception ");
		buffer.append(e);
		buffer.append(" in thread ");
		buffer.append(t.getName());
		buffer.append(" (");
		buffer.append(t.getId());
		buffer.append(")\n");
		for(StackTraceElement ste: e.getStackTrace()){
			buffer.append("\tat ");
			buffer.append(ste.getClassName());
			buffer.append(".");
			buffer.append(ste.getMethodName());
			buffer.append("():");
			buffer.append(ste.getLineNumber());
			buffer.append("\n");
		}
		try {
			filter(buffer.toString());
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		ShowExceptionThread.showException(buffer.toString());
	}
	

    /**
     * Just for testing
     * @param args
     */
    public static void main(String args[]){
    	PrintStream filteredPrintStream  =  new PrintStream(new FilteredStream(new ByteArrayOutputStream(),null,true));
		System.setErr(filteredPrintStream);
		System.setOut(filteredPrintStream);
		
		for(int i=0; i<1000000; i++){
			//filteredPrintStream.print("ciao");
			System.out.println("ciao asd asd asda sdadasdsdsa das ds prova");
			try{
				Thread.sleep(50);
			}catch(InterruptedException ie){
				ie.printStackTrace();
			}
		}
		System.out.println("Finished");
    }
}