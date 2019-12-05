package org.siftdna.main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
//import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
//import java.io.InputStream;
import java.net.*;

import javax.swing.*;

import java.io.IOException;
//import java.io.InterruptedIOException;
//import java.lang.reflect.Array;
import java.util.ArrayList;
//import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;




import java.util.regex.Matcher;
import java.util.regex.Pattern;


//import javax.annotation.processing.Completion;
import javax.imageio.ImageIO;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.Document;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.siftdna.gui.MO_ResultsPanel;
//import org.siftdna.gui.ResultsPanel;
import org.siftdna.gui.SIFTSummary;
import org.siftdna.siftobjects.SingleSIFTResult;
import org.siftdna.utils.UrlFileReader;
import org.siftdna.genomes.GenomeListItem;
import org.siftdna.genomes.GenomeSelection;
import org.siftdna.genomes.SwingFileDownloadHTTP;
import org.siftdna.utils.UrlFileReader;

public class SIFT4G_Main implements ActionListener, Runnable {

	private static SIFT4G_Main main = null;

	// GUI Objects
	private static JFrame mainFrame;
	private static JButton startButton = null;
	private static JTextField queryTx = null;
	private static JTextField dbTx = null;
	private static JCheckBox multiTranscriptsChkBox = null;
	// private static ResultsPanel resultsPanel = null;
	private static MO_ResultsPanel resultsPanel = null;
	private static JPanel progressBarPanel = null;
	private static JProgressBar pBar = null;
	private static JSlider thresholdSlider = null;
	private static JLabel resultsLabel = null;
	private static JButton outputFolderBtn = null;
	private static String outputFolder = null;
	private static String contentText = null;
	private static JPanel outputPanel = null;
	//private static COLOUMNERROR ColError = null
	private static JLabel vcflink = null;;

	// private static SIFT_CommandLine cli = null;
	private static MO_CommandLine[] cli = null;
	private static Thread[] cli_threads = null;
	private static boolean HEADLESS = false; // If true, then it is run on command line.
	private static float THRESHOLD = 0.05f; // SIFT predictions threshold
	private static Map<String, List<String>> inputDict = null;
	private static  List<String> TempFilesOrder = null;
	private static Map<String, String[]> displayDict = null;
	private static Map<String, String> displayDictMapping = null;
	private static boolean isMultiTranscripts = false;
	private int count = 0; // Incrementing this causes progress bar to change.
	private static InputFileManager ifm = null;
	private static File cmdoutDir = null;
	private static GenomeListItem genome = null;
	private static GenomeSelection genomeSelection = null;
	private static SwingFileDownloadHTTP swingFile = null;
	private static JLabel VCFoutFileName = new JLabel();
	private static JLabel TSVoutFileName = new JLabel();
	private static JButton VCFhelpButton = null;
	private static JButton TSVhelpButton = null;
	private static JPanel announcementPanel = null;
	private static UrlFileReader urlRead = null;
	public static void main(String args[]) {
	
		EventManager.GetInstance();
		Calendar startTimestamp = Calendar.getInstance();
		startTimestamp.add(Calendar.DATE, 1);
		String startTime = startTimestamp.getTime().toString();
		System.out.println("Start Time for SIFT4G code: " + startTime);
		main = new SIFT4G_Main();
		new Thread(main).start();
		cli = new MO_CommandLine[1];
		cli[0] = new MO_CommandLine(main);

		// Parse commands from received args
		CommandLine cmdLine = getCommandLine(args);
		HEADLESS = cmdLine.hasOption('c');

		if (HEADLESS == false) {

			javax.swing.SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					try {
						createAndShowGUI();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});
		}
		else {
			String inputFile = cmdLine.getOptionValue(SIFTConstants.INPUT_FILE_OPT);
			String outputDir = cmdLine.getOptionValue(SIFTConstants.OUTPUT_DIR_OPT);
			//String outputFileName = cmdLine.getOptionValue(SIFTConstants.OUTPUT_FILE_OPT);
			String dbDir = cmdLine.getOptionValue(SIFTConstants.DATABASE_DIR_OPT);
			isMultiTranscripts = cmdLine.hasOption(SIFTConstants.MULTI_TRANSCRIPTS_OPT);
			String sift_pred_threshold = cmdLine.getOptionValue(SIFTConstants.SIFT_PREDICTION_THRESHOLD_OPT);
			if (sift_pred_threshold == null || Float.isNaN(Float.valueOf(sift_pred_threshold))) {
				THRESHOLD = 0.05f; // Default value
			} else {
				THRESHOLD = Float.valueOf(sift_pred_threshold);
			}
			if(cmdLine.hasOption('h')){
				System.err.println();
				commandlinehelp();
			}
			/* -i -o -d */
			if (inputFile == null|| dbDir == null) {
				System.err.println("Please provide both -i inputfile.vcf  and -d databaseFolder options for the program");
				commandlinehelp();
			}
			if (outputDir == null || outputDir.isEmpty()) {
				File iFile = new File(inputFile);
				if(iFile.getParent() == null){
					System.err.println("Please provide the full system path to the input file!");
					System.exit(1);
				}
				outputDir = iFile.getParent()+ System.getProperty("file.separator") + "SIFT4G_results";
			}
			// Let's create the output directory if it doesn't exist
			// Announcements should be shown here. 
			urlRead = new UrlFileReader();
			contentText = urlRead.FileReader(SIFTConstants.SIFTDB_ANNOUNCEMENT);
			if(contentText.equalsIgnoreCase("NA")){
				contentText = "Cannot read latest announcements from server !!!";
			}
			else if(contentText.equalsIgnoreCase("NoInternet")){
				contentText = "Cannot connect to network"+"\t"+"Please check your internet connection !!!!";
			}
			else if(contentText.isEmpty()){
				contentText = "No updates from server!!";
			}
			String pattern = "^(.*) Please go to .*$";
			Pattern r = Pattern.compile(pattern);
			Matcher m = r.matcher(contentText);
			if(m.find())
				contentText = m.group(1);
			contentText = contentText + " Please go to http:sift-dna.org for updates.";
			System.out.println("Updates:"+"\n"+contentText);
			System.out.println("\n"+"Started Running .......");
			cmdoutDir = new File(outputDir);
			if(isMultiTranscripts){
				System.out.println("Running in Multitranscripts mode");
			}
			else{
				System.out.println("Running in Single transcripts mode");
			}
			System.out.println();
			System.out.println("Chromosome\tWithSIFT4GAnnotations\tWithoutSIFT4GAnnotations\tProgress");
			if(!cmdoutDir.exists())
				cmdoutDir.mkdirs();

			// Read Input file and store data in dictionary index of chr no
			ifm = new InputFileManager();
			try {

				inputDict = ifm.InitInputFileManger(inputFile, outputDir, THRESHOLD,isMultiTranscripts, HEADLESS);
				ifm.checkChrMatchesDatabase (inputDict, dbDir, HEADLESS);
				TempFilesOrder = ifm.GetChrOrder();
				// Merge the all output file CSV
				OutputFileManager OFM = new OutputFileManager();
				// remove all previous output files exists in the results folder
				try{
					File folder = new File(ifm.Directory_Path);
					File files[] = folder.listFiles();
					for (int i = 0; i < files.length; i++) {
						if(files[i].getName().endsWith("multiTranscripts.vcf") || files[i].getName().endsWith("multiTranscripts.tsv")){
							OFM.DeleteFile(ifm.Directory_Path, files[i].getName());
						}
					}
				}catch(Exception ex){
					ex.printStackTrace();
					System.exit(1);
				}
				int k = 0;
				cli = new MO_CommandLine[inputDict.size()];
				cli_threads = new Thread[inputDict.size()];

				for (String chrNo : inputDict.keySet()) {
					cli[k] = new MO_CommandLine(main);
					List<String> localList1 = inputDict.get(chrNo);
					cli[k].setInfo(inputFile, outputDir, dbDir, isMultiTranscripts, THRESHOLD, startTime,localList1, chrNo);
					outputFolder = new String(outputDir);
					cli_threads[k] = new Thread(cli[k]);
					cli_threads[k].start();
					k++;
				}
				
			}
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.exit(-1);
			}
			
		}


	}


	private static void createAndShowGUI() throws Exception {
		// Step 1: Create main frame
		mainFrame = new JFrame("SIFT 4G");
		mainFrame.setTitle("SIFT 4G Annotator");
		Toolkit tk = Toolkit.getDefaultToolkit();
		int xSize = ((int) tk.getScreenSize().getWidth() / 2) + 200;
		int ySize = ((int) tk.getScreenSize().getHeight() / 2) + 100;
		mainFrame.setSize(xSize, ySize);

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		URL imgURL =null;
		BufferedImage image = null;
		try {
			imgURL = SIFT4G_Main.class.getResource("images/sift_cup.jpg");
			image = ImageIO.read(imgURL);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mainFrame.setIconImage(image);
		mainFrame.setLayout(new BorderLayout());
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		mainFrame.setLocation(dim.width / 2 - mainFrame.getSize().width / 2,
				dim.height / 2 - mainFrame.getSize().height / 2);

		// Step 2: do up UI
		resultsPanel = new MO_ResultsPanel();
		outputPanel = new JPanel(new BorderLayout());
		outputPanel.add(resultsPanel, BorderLayout.CENTER);

		JPanel outputBtnPanel = new JPanel(new BorderLayout());

		resultsLabel = new JLabel("");
		resultsLabel.setFont(new Font(resultsLabel.getFont().getName(), Font.BOLD, 15));

		outputFolderBtn = new JButton(SIFTConstants.OUTPUT_FOLDER_BTN_TEXT);

		outputFolderBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (Desktop.isDesktopSupported()) {
					try {
						Desktop.getDesktop().open(new File(outputFolder));
					} catch (IOException ioe) {
						ioe.printStackTrace();
					}
				}
			}
		});
		outputFolderBtn.setEnabled(false);

		JPanel outputInfoPanel = new JPanel(new BorderLayout());
		outputInfoPanel.add(getResultsLabel(), BorderLayout.CENTER);
		outputInfoPanel.add(outputFolderBtn, BorderLayout.EAST);
		outputBtnPanel.add(outputInfoPanel);
		//outputPanel.add(outputBtnPanel, BorderLayout.SOUTH);
		JPanel resultsNewPanel = new JPanel(new BorderLayout());
		resultsNewPanel = getResultsPanel();
		outputPanel.add(resultsNewPanel, BorderLayout.SOUTH);
		mainFrame.getContentPane().add(outputPanel, BorderLayout.CENTER);
		JPanel OuterPanel  = new JPanel(new BorderLayout());
		JPanel inputInfoPanel = new JPanel(new BorderLayout());

		JPanel inputPanel = new JPanel(new BorderLayout());
		JPanel userQueryPanel = getUserInputPanel();
		inputPanel.add(userQueryPanel, BorderLayout.NORTH);
		JPanel databasePanel = getDatabasePanel();
		inputPanel.add(databasePanel, BorderLayout.SOUTH);
		JPanel inputHelpPanel = new JPanel(new BorderLayout());
		inputHelpPanel.add(inputPanel, BorderLayout.CENTER);
		JPanel questionPanel = getHelpPanel();
		inputHelpPanel.add(questionPanel, BorderLayout.EAST);

		inputInfoPanel.add(inputHelpPanel, BorderLayout.CENTER);
		JPanel optionsPanel = createOptionsPanel();
		inputInfoPanel.add(optionsPanel, BorderLayout.SOUTH);

		createStartButton();
		inputInfoPanel.add(startButton, BorderLayout.EAST);
		OuterPanel.add(inputInfoPanel, BorderLayout.CENTER);
		announcementPanel = getAnnouncementPanel();
		OuterPanel.add(announcementPanel, BorderLayout.SOUTH);
		startButton.setEnabled(false);

		mainFrame.getContentPane().add(OuterPanel, BorderLayout.NORTH);
		createProgressBarPanel();

		//mainFrame.getContentPane().add(OuterPanel, BorderLayout.NORTH);

		mainFrame.setVisible(true);

	}

	private static JPanel getAnnouncementPanel()  {
		// TODO Auto-generated method stub
		JPanel Apanel = new JPanel(new BorderLayout());
		JEditorPane editorpane= new JEditorPane();
	    JScrollPane editorScrollPane = new JScrollPane(editorpane);
	    editorScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
	    editorpane.setContentType("text/html");
	    editorpane.setEditable(false);
	    editorpane.setOpaque(false);
	    MouseListener l = new MouseListener() {
			@Override
			public void mouseReleased(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			@Override
			public void mousePressed(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			@Override
			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			@Override
			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			@Override
			public void mouseClicked(MouseEvent e) {
				// TODO Auto-generated method stub
				try{
					Desktop.getDesktop().browse(new URI(SIFTConstants.SIFTDB_HELP));
				}
				catch(URISyntaxException ex){	
					ex.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
			}
		};
		
	    urlRead = new UrlFileReader();
	    contentText = urlRead.FileReader(SIFTConstants.SIFTDB_ANNOUNCEMENT);
	    if(contentText=="NA"){
	    	contentText = "Cannot read latest announcements from server !!!";
	    	editorpane.removeMouseListener(l);
	    }
	    else if (contentText=="NoInternet") {
	    	contentText = "Cannot connect to network"+"\t"+"Please check your internet connection !!!!";
	    	editorpane.removeMouseListener(l);
		}
	    else if(contentText.isEmpty()){
	    	contentText = "No latest updates!!";
	    	Apanel.setVisible(false);
	    	editorpane.removeMouseListener(l);
	    }
	    else{
	    	editorpane.addMouseListener(l);
	    }
		editorpane.setText(contentText);
		String footerlink = "<div>Please go to <a href=\"http://sift-dna.org/\">SIFT 4G</a> for updates</div>";
		editorpane.setText(editorpane.getText()+"\n"+ footerlink);
	    editorpane.setForeground(Color.BLUE);
	    //JLabel Alabel = new JLabel("Updates: ");
	    //Alabel.setForeground(Color.RED);
	    final JEditorPane finalpane = editorpane;
	    Apanel.add(finalpane, BorderLayout.CENTER);
	    //Apanel.add(Alabel, BorderLayout.NORTH);
		return Apanel;
	}


	private static JPanel getResultsPanel(){
		JPanel ResultPanel = new JPanel(new BorderLayout());
		JPanel resultsName = new JPanel(new BorderLayout());
		resultsName.add(getResultsLabel(),BorderLayout.WEST);
		JPanel vcfresult = new JPanel(new BorderLayout());
		JPanel tsvresult = new JPanel(new BorderLayout());
		tsvresult.add(resultsName,BorderLayout.NORTH);
		
		VCFhelpButton = new JButton("Help");
		//VCFhelpButton.setBackground(Color.RED);
		//VCFhelpButton.setForeground(Color.RED);
		VCFhelpButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				JOptionPane.showMessageDialog(null, "<html><body><p style='width: 350px;'> SIFT 4G annotations will be appended to the INFO column using the key \"SIFTINFO=\". Each data field is separated by \"|\". </p></body></html>", null, JOptionPane.PLAIN_MESSAGE);
			}
		});
		vcfresult.add(VCFhelpButton, BorderLayout.WEST);
		VCFhelpButton.setVisible(false);
		VCFoutFileName.setVisible(false);
		VCFoutFileName.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		VCFoutFileName.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent me) {
				try {
					String p = VCFoutFileName.getText().substring(VCFoutFileName.getText().lastIndexOf("\">")+2, VCFoutFileName.getText().lastIndexOf("</a"));
					Desktop.getDesktop().open(new File(p));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		vcfresult.add(VCFoutFileName, BorderLayout.CENTER);
		
		TSVhelpButton = new JButton("Help");
		//TSVhelpButton.setForeground(Color.RED);
		//TSVhelpButton.setBackground(Color.RED);
		TSVhelpButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				JOptionPane.showMessageDialog(null, "<html><body><p style='width: 200px;'> 	The first four columns in the XLS output are the same as the input VCF file. The remaining columns contain the SIFT annotations.</a></p><br> " 
	
					+	"<p style='width: 200px;'> When the user selects the �Multiple Transcripts� option, the variant will appear on separate rows for each transcript. </p></body></html>", null, JOptionPane.PLAIN_MESSAGE);
			}
		});
		tsvresult.add(TSVhelpButton, BorderLayout.WEST);
		TSVhelpButton.setVisible(false);
		TSVoutFileName.setVisible(false);
		TSVoutFileName.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		TSVoutFileName.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent me) {
				try {
					String q = TSVoutFileName.getText().substring(TSVoutFileName.getText().lastIndexOf("\">")+2 , TSVoutFileName.getText().lastIndexOf("</a"));
					Desktop.getDesktop().open(new File(q));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		tsvresult.add(TSVoutFileName,BorderLayout.CENTER);
		ResultPanel.add(vcfresult, BorderLayout.SOUTH);
		ResultPanel.add(tsvresult, BorderLayout.NORTH);
		JPanel FinalResultPanel = new JPanel(new BorderLayout());
		FinalResultPanel.add(ResultPanel, BorderLayout.WEST);
		return FinalResultPanel;
	}
	
	private static void createProgressBarPanel() {

		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				progressBarPanel = new JPanel(new BorderLayout());
				pBar = new JProgressBar();
				// Because we do not know the number of chromosomes in the user
				// query file, We cannot set the maximum value, the progress bar has to be indeterminate
				pBar.setIndeterminate(true);
				pBar.setVisible(false);
				pBar.setStringPainted(true);
				progressBarPanel.add(pBar, BorderLayout.CENTER);
				mainFrame.getContentPane().add(progressBarPanel,
						BorderLayout.SOUTH);
			}
		});
	}

	// region Threshold bar and multi-transcript Checkbox Code
	private static JPanel createOptionsPanel() {
		JPanel optionsPanel = new JPanel(new BorderLayout());
		multiTranscriptsChkBox = new JCheckBox();
		multiTranscriptsChkBox.setSelected(false);
		multiTranscriptsChkBox.setText("Multiple Transcripts");

		thresholdSlider = new JSlider(JSlider.HORIZONTAL, 0, 10, 5);
		thresholdSlider.setSnapToTicks(true);
		Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
		for (int i = 0; i <= 10; i++) {
			float labelValue = (i * 1.0f) / 100.0f;
			String v = Float.toString(labelValue);
			labelTable.put(new Integer(i), new JLabel(v));
		}
		thresholdSlider.setLabelTable(labelTable);

		thresholdSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				int v = thresholdSlider.getValue();
				THRESHOLD = (v * 1.0f / 100.0f);
			}
		});

		// Turn on labels at major tick marks.
		thresholdSlider.setMajorTickSpacing(1);
		thresholdSlider.setPaintTicks(true);
		thresholdSlider.setPaintLabels(true);
		JPanel checkBoxPanel = new JPanel(new BorderLayout());
		JPanel chkbox = new JPanel(new BorderLayout());
		chkbox.add(multiTranscriptsChkBox,BorderLayout.CENTER);
		JButton thelp = new JButton("Help");
		//thelp.setForeground(Color.RED);
		//thelp.setBackground(Color.RED);
		thelp.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				JOptionPane.showMessageDialog(null, "<html><body><p style='width: 200px;'>Check box to have each variant annotated by all transcripts. </p></body></html>", null, JOptionPane.PLAIN_MESSAGE);
			}
		});
		chkbox.add(thelp, BorderLayout.EAST);
		checkBoxPanel.add(chkbox, BorderLayout.WEST);

		JPanel sliderPanel = new JPanel(new BorderLayout());

		optionsPanel.add(checkBoxPanel, BorderLayout.NORTH);
		optionsPanel.add(sliderPanel, BorderLayout.SOUTH);
		return optionsPanel;
	}

	private static void checkButtonState() {
		boolean state = (queryTx.getText().length() > 0)
				&& (dbTx.getText().length() > 0);
		startButton.setEnabled(state);
		announcementPanel.setVisible(false);;
	}

	private static JPanel getHelpPanel(){
		JPanel hpanel = new JPanel(new BorderLayout());
		JButton qhelp = new JButton("Help");
		//qhelp.setForeground(Color.RED);
		//qhelp.setBackground(Color.RED);
		
		qhelp.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				JLabel label = new JLabel();
			    Font font = label.getFont();

			    StringBuffer style = new StringBuffer("font-family:" + font.getFamily() + ";");
			    style.append("font-weight:" + (font.isBold() ? "bold" : "normal") + ";");
			    style.append("font-size:" + font.getSize() + "pt;");
			    style.append("width: 250px");
				JEditorPane ep = new JEditorPane("text/html", "<html><body style=\"" + style + "\">" 
			            + "The input file should be in <a href=\"http://vcftools.sourceforge.net/specs.html\">VCF format</a>.</p><br></br> "
			            + "<html><body><p style=\"" + style +"\" >A VCF file contains at least eight columns. The predictions will be appended to the eighth column.</p><br></br> "
						+ "<p style='width: 250px'>If the user's input file contains the chromosome, position, reference, and alternate alleles alone,"
						+ " the user can append dummy columns to ensure that the input file will have at least eight columns.</p></body></html>");

			    // handle link events
			    ep.addHyperlinkListener(new HyperlinkListener()
			    {
			        @Override
			        public void hyperlinkUpdate(HyperlinkEvent e)
			        {
			            if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED))
							try {
								Desktop.getDesktop().browse(new URI("http://vcftools.sourceforge.net/specs.html"));
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							} catch (URISyntaxException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							} // roll your own link launcher or use Desktop if J6+
			        }
			    });
			    ep.setEditable(false);
			    ep.setBackground(label.getBackground());
			    JOptionPane.showMessageDialog(null, ep, null, JOptionPane.PLAIN_MESSAGE);
			}
		});
		JButton dhelp = new JButton("Help");
		//dhelp.setForeground(Color.RED);
		//dhelp.setBackground(Color.RED);
		dhelp.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				JOptionPane.showMessageDialog(null, "<html><body><p style='width: 250px;'>Select 'Click here to download SIFT 4G databases' to download and upload a database.</p><br></br>"
						+ "<html><body><p style='width: 250px;'> Databases will be stored in the folder \"SIFT4G/databases\" located in the user's home directory.</p></body></html>", null, JOptionPane.PLAIN_MESSAGE);
			}
		});
		hpanel.add(qhelp,BorderLayout.NORTH);
		hpanel.add(dhelp, BorderLayout.SOUTH);
		
		return hpanel;
	}

	private static JPanel getDatabasePanel() throws IOException {
		//

		JPanel panel = new JPanel(new BorderLayout());

		JLabel label = new JLabel("Database: ");

		dbTx = new JTextField();
		dbTx.setEditable(false);
		genome = new GenomeListItem();
		final String path = getDatabaseDir();
		final List<GenomeListItem> ServerGenomes = genome.getServerGenomeList();
		final Object [] downloaded_genomes = genome.list_downloaded(path);
		final JComboBox cb = new JComboBox();
		cb.setModel(getComboBoxModel(downloaded_genomes));
		cb.setVisible(true);
		cb.setSelectedItem(null);
		cb.addActionListener(new ActionListener() {
			@SuppressWarnings({ "unchecked", "rawtypes" })
			@Override
			public void actionPerformed(ActionEvent e) {
				JComboBox cb = (JComboBox) e.getSource();
				String db = (String) cb.getSelectedItem();
				if (db.equalsIgnoreCase("Select database to download")){
					GenomeSelection genomeSelection = new GenomeSelection(mainFrame,ServerGenomes,ListSelectionModel.SINGLE_SELECTION);
					genomeSelection.setVisible(true);
					List <GenomeListItem> selectedGenome = null;
					String selGenome = null;
					boolean check = true;
					if(genomeSelection.getSelectedValuesList() == null){
						check = false;
						cb.setSelectedIndex(0);
						dbTx.setText(null);
						checkButtonState();
					}
					else{
						selectedGenome = genomeSelection.getSelectedValuesList();
						selGenome = selectedGenome.get(0).getDisplayableName();
						boolean b = genome.hasDownloaded(selGenome)? true : false ;
						if(b){
							int output = JOptionPane.showConfirmDialog(mainFrame
									,selectedGenome.get(0).getDisplayableName() + " is already Downloaded. Do you still want to proceed?"
									,"WARNING"
									,JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE);
							if(output == JOptionPane.YES_OPTION || output == JOptionPane.CANCEL_OPTION){
								check = true;
							}
							else if(output == JOptionPane.NO_OPTION){
								check = false;
								dbTx.setText(genome.get_local_path(selGenome) + File.separator + selectedGenome.get(0).getFileName());
								cb.setSelectedItem(selGenome);
								checkButtonState();
							}
						}
					}
					if(selectedGenome!=null && selectedGenome.size()>=1 && check){
						//final JFrame downloadFrame = new JFrame();
						SwingFileDownloadHTTP swingFile = new SwingFileDownloadHTTP(mainFrame, selectedGenome.get(0).getLocation(), path);
						swingFile.setVisible(true);
						if(!swingFile.isInterupted()){
							String DbName = selectedGenome.get(0).getDisplayableName();
							cb.setModel(getComboBoxModel(genome.list_downloaded(path)));
							cb.setSelectedItem(DbName);
							File newPath = new File(path + File.separator + selectedGenome.get(0).getFileName() + File.separator + selectedGenome.get(0).getFileName());
							dbTx.setText(newPath.getAbsolutePath());
							checkButtonState();
							swingFile.dispose();
						}
						else{
							cb.setSelectedIndex(0);
							dbTx.setText(null);
							checkButtonState();
						}
					}
				}
				else if(!db.equalsIgnoreCase("Select Databases")){
					File f = new File(genome.get_local_path(db)+ System.getProperty("file.separator") + genome.get_local_fileName(db));
					dbTx.setText(f.getAbsolutePath());
					checkButtonState();
				}
				else if(db.equalsIgnoreCase("Select Databases")){
					dbTx.setText(null);
					checkButtonState();
				}
			}
		});

		panel.add(label, BorderLayout.WEST);
		panel.add(cb, BorderLayout.CENTER);
		return panel;
	}

	@SuppressWarnings("unchecked")
	private static ComboBoxModel getComboBoxModel(
			Object[] downloaded_genomes) {
		// TODO Auto-generated method stub
		@SuppressWarnings("rawtypes")
		DefaultComboBoxModel model = new DefaultComboBoxModel();
		model.removeAllElements();
		//model.addElement("Select Databases");
		for(Object item: downloaded_genomes){
			model.addElement(item);
		}
		model.addElement("Select database to download");
		return model;
	}


	private static String getDatabaseDir() {

		File SiftDbDir = new File(System.getProperty("user.home") + File.separator + "SIFT_4G" + File.separator+ "Databases");
		if(!SiftDbDir.exists()){
			SiftDbDir.mkdirs();
		}
		return SiftDbDir.getAbsolutePath();

	}


	/*private static Object[] getDownloadedDatabases(){
		Object[] result = null;
		if(dbTx.getText().length()>0){
			 result = genome.list_downloaded(dbTx.getName(), false);
		}
		else{
			result = genome.list_downloaded(null, false);
		}
		return result;

	}*/
	private static void createStartButton() {
		startButton = new JButton("Start");
		startButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// This is what happens when user click the START button
				String infile = queryTx.getText();
				String dbdir = dbTx.getText();
				System.out.println("Start Running...");
				// Store results in input directory
				Integer inpCol = new Integer(0);
				File inF = new File(infile);
				String outputDir = inF.getParent()+ System.getProperty("file.separator") + "SIFT4G_results";
				File outDir = new File(outputDir);
				if(!outDir.exists())
					outDir.mkdirs();
				boolean multiTranscripts = multiTranscriptsChkBox.isSelected();
				resultsPanel.clearTable();
				Calendar startTimestamp = Calendar.getInstance();
				startTimestamp.add(Calendar.DATE, 1);
				String startTime = startTimestamp.getTime().toString();
				System.out.println("Start Time for parallel code: " + startTime);
				pBar.setVisible(true);
				main.startedRunning();
				main.informUserAboutOutputDir("");

				ifm = new InputFileManager();
				try {
					inputDict = ifm.InitInputFileManger(infile, outputDir, THRESHOLD, multiTranscriptsChkBox.isSelected(), HEADLESS);
					ifm.checkChrMatchesDatabase (inputDict, dbdir, HEADLESS);
					TempFilesOrder = ifm.GetChrOrder();
					// Merge the all output file CSV
					OutputFileManager OFM = new OutputFileManager();
					// remove all previous output files exists in the results folder
					try{
						File folder = new File(ifm.Directory_Path);
						File files[] = folder.listFiles();
						for (int i = 0; i < files.length; i++) {
							if(files[i].getName().contains("SIFT")){
								OFM.DeleteFile(ifm.Directory_Path, files[i].getName());
							}
						}
					}catch(Exception ex){
						ex.printStackTrace();
						System.exit(1);
					}
					// MergeTempOutputFiles();
					PrepareDisplayTable();
					int k = 0;
					cli = new MO_CommandLine[inputDict.size()];
					cli_threads = new Thread[inputDict.size()];

					for (String chrNo : inputDict.keySet()) {
						cli[k] = new MO_CommandLine(main);
						List<String> localList1 = inputDict.get(chrNo);
						cli[k].setInfo(infile, outputDir, dbdir, multiTranscripts, THRESHOLD, startTime, localList1, chrNo);
						outputFolder = new String(outputDir);
						outputFolderBtn.setEnabled(false);
						outputFolderBtn.setVisible(false);
						cli_threads[k] = new Thread(cli[k]);
						cli_threads[k].start();
						k++;
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}


	private static JPanel getUserInputPanel() {
		JPanel panel = new JPanel(new BorderLayout());

		JLabel inputFileLabel = new JLabel("Variant File: ");

		queryTx = new JTextField();
		queryTx.setEditable(false);

		Document textFieldDoc = queryTx.getDocument();
		textFieldDoc.addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				updated(e);
			}

			public void insertUpdate(DocumentEvent e) {
				updated(e);
			}

			public void removeUpdate(DocumentEvent e) {
				updated(e);
			}

			private void updated(DocumentEvent e) {
				checkButtonState();
			}
		});

		final JFileChooser fc = new JFileChooser();
		JButton openButton = new JButton("Select a VCF file", null);
		openButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				int returnVal = fc.showOpenDialog(mainFrame);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = fc.getSelectedFile();
					String path = file.getAbsolutePath();
					queryTx.setText(path);
				}
			}
		});

		panel.add(inputFileLabel, BorderLayout.WEST);
		panel.add(queryTx, BorderLayout.CENTER);
		panel.add(openButton, BorderLayout.EAST);


		return panel;
	}



	private static CommandLine getCommandLine(String[] args) {

		try {
			// STAGE 1: Collect information from arguments
			Options options = new Options();
			options.addOption(SIFTConstants.INPUT_FILE_OPT, true,
					SIFTConstants.INPUT_FILE_OPT_DESC);

			options.addOption(SIFTConstants.SIFT_PREDICTION_THRESHOLD_OPT,
					false, SIFTConstants.SIFT_PREDICTION_THRESHOLD_OPT_DESC);

			options.addOption(SIFTConstants.DATABASE_DIR_OPT, true,
					SIFTConstants.DATABASE_DIR_OPT_DESC);
			options.addOption(SIFTConstants.OUTPUT_FILE_OPT, false,
					SIFTConstants.OUTPUT_FILE_OPT_DESC);
			options.addOption(SIFTConstants.OUTPUT_DIR_OPT, true,
					SIFTConstants.OUTPUT_DIR_OPT_DESC);
			options.addOption(SIFTConstants.HEADLESS_OPT, false,
					SIFTConstants.HEADLESS_OPT_DESC);
			options.addOption(SIFTConstants.MULTI_TRANSCRIPTS_OPT, false,
					SIFTConstants.MULTI_TRANSCRIPTS_OPT_DESC);
			options.addOption(SIFTConstants.HELP_OPTION, false, SIFTConstants.HELP_DESC);

			options.getOption(SIFTConstants.INPUT_FILE_OPT).setRequired(true);
			options.getOption(SIFTConstants.DATABASE_DIR_OPT).setRequired(true);
			options.getOption(SIFTConstants.OUTPUT_FILE_OPT).setRequired(false);
			options.getOption(SIFTConstants.OUTPUT_DIR_OPT).setRequired(true); // If user did not provide output directory, we write to dir containing input file
			options.getOption(SIFTConstants.HEADLESS_OPT).setRequired(false);
			options.getOption(SIFTConstants.MULTI_TRANSCRIPTS_OPT).setRequired(false); 
			options.getOption(SIFTConstants.HELP_OPTION).setRequired(false);
			CommandLineParser parser = new PosixParser();
			CommandLine cmd = parser.parse(options, args);
			return cmd;

		} catch (ParseException pe) {
			pe.printStackTrace();
			System.out.println("Unable to parse options correctly: please check your options");
			commandlinehelp();
		}
		return null;
	}




	@Override
	public void actionPerformed(ActionEvent e) {
	}

	public void updateResultsTable(List<SingleSIFTResult> resultsForTable) {

	}

	public void startedRunning() {
		count = 0;
		if (pBar != null) {
			pBar.setString("Running...");
			pBar.setStringPainted(true);
			pBar.setVisible(true);
		}

		if (mainFrame != null) {
			mainFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		}
	}

	public void sendCompletedNotification(String vcfFile, String tsvFile) {
		if (mainFrame != null) {
			mainFrame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			pBar.setVisible(false);
		}
		if (isCommandLine()) {
			System.out.println("Output VCF file: " + vcfFile);
			System.out.println("Output TSV file: " + tsvFile);
			System.exit(0);
		}
	}

	public boolean isCommandLine() {
		return HEADLESS;
	}

	public void setCommandLine(boolean c) {
		HEADLESS = c;
	}

	public void informUserAboutOutputDir(String fileinfo) {
		if (!HEADLESS) {
			if (!fileinfo.equals("")) {
				SIFT4G_Main.getResultsLabel().setText("Results :");
				SIFT4G_Main.getResultsLabel().setEnabled(true);
				/*SIFT4G_Main.getOutputFolderBtn().setVisible(true);
				SIFT4G_Main.getOutputFolderBtn().setEnabled(true);
				SIFT4G_Main.getOutputFolderBtn().setBackground(new Color(3, 59, 90).brighter());*/
				String vcfOutput = ifm.GetFinalVcfOutputFileName(isMultiTranscripts);
				String tsvOutput = ifm.GetFinalTsvOutputFileName(isMultiTranscripts);
				VCFoutFileName.setText("<html> <a href = \"\">" + vcfOutput + "</a> </html>");
				VCFoutFileName.setVisible(true);
				TSVoutFileName.setText("<html> <a href = \"\">" + tsvOutput + "</a> </html>");
				TSVoutFileName.setVisible(true);	
				VCFhelpButton.setVisible(true);
				TSVhelpButton.setVisible(true);
			}
			else{
				SIFT4G_Main.getResultsLabel().setText(fileinfo);
				VCFoutFileName.setVisible(false);
				TSVoutFileName.setVisible(false);
				VCFhelpButton.setVisible(false);
				TSVhelpButton.setVisible(false);
			}
		}
	}

	private static AbstractButton getOutputFolderBtn() {
		return outputFolderBtn;
	}

	// Set the maximum number for Progress Bar (eg. 20)
	// so, if count is increased from 0 to 1,
	// the Progress bar will show 1/20 = 5%.
	public void setPBarMax(long max) {
		if (pBar != null) {
			System.out.println("Set PBarMax: " + max);
			pBar.setMaximum((int) max);
			pBar.setStringPainted(true);
		}
	}
	// This is invoked when a portion of the code is completed,
	// and this increases the progress bar.
	public void increment(String chromosome) {
		count++;
		if (pBar != null && chromosome != null) {
			pBar.setString("Completed " + chromosome);
			pBar.setStringPainted(true);
		}
	}

	// This causes SIFTMain to run on a thread that's different from the
	// Progress Bar.
	// If they are running on the same thread, the Progress Bar will not update
	// dynamically.
	// It will be 0% all the way until the run is completed, and change to 100%
	// suddenly.
	@Override
	public void run() {
		while (true) {
			// wait for the signal from the GUI
			try {
				synchronized (this) {
					wait();
				}
			} catch (InterruptedException e) {
			}
		}
	}

	public static JLabel getResultsLabel() {
		return resultsLabel;
	}

	public void notifyTable(String chromosome, long numberWithSIFTAnnotations, long numberWithoutSIFTAnnotations) {
		String endTime = null;
		Calendar endTimestamp = null;
		// prepare display inputDict
		if (chromosome != null && HEADLESS == false) {
			int completed = inputDict.size() - count;
			increment(inputDict.size() - completed + 1 + "/" + inputDict.size());
			String[] line = SIFTSummary.getSummaryLine(chromosome, numberWithSIFTAnnotations, numberWithoutSIFTAnnotations);
			Map<String, String[]> treeMap = new TreeMap<String, String[]>();
			try {
				String chr1 = line[0];
				String chr1Ref = displayDictMapping.get(chr1);
				displayDict.put(String.format("%04d", Integer.parseInt(chr1Ref)),line);
				treeMap = new TreeMap<String, String[]>(displayDict);
				resultsPanel.clearTable();
				for (String chr : treeMap.keySet()) {
					String[] DisplayLines = treeMap.get(chr);
					resultsPanel.addRow(DisplayLines);
				}
			} catch (Exception ex) {
			}
			// When we reach all
			if (completed == 1) {
				for (String ch : displayDict.keySet()) {
					String temp_str[] = displayDict.get(ch);
					if (temp_str[1] == "Running..") {
						temp_str[1] = "NO SIFT4G ANNOTATION";
						temp_str[2] = "NO SIFT4G ANNOTATION";
					}
					displayDict.put(ch, temp_str);
				}

				resultsPanel.clearTable();
				for (String chr : treeMap.keySet()) {
					String[] DisplayLines = treeMap.get(chr);
					resultsPanel.addRow(DisplayLines);
				}
				if (pBar != null ) { 
					pBar.setString("Merging temp files...");
					pBar.setStringPainted(true);
				}
				endTimestamp = Calendar.getInstance();
				endTimestamp.add(Calendar.DATE, 1);
				endTime = endTimestamp.getTime().toString();
				System.out.println("End Time for parallel code: " + endTime +"\n");
				MergeTempOutputFiles();
				pBar.setVisible(false);
				mainFrame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			}
		}

		if(chromosome != null && HEADLESS == true){
			int completed = inputDict.size() - count;
			increment(inputDict.size() - completed + 1 + "/" + inputDict.size());
			System.out.println(chromosome+ "\t\t\t" + numberWithSIFTAnnotations + "\t\t\t"+ numberWithoutSIFTAnnotations+"\t\t\t"+ 
					"Completed : " + (inputDict.size() - completed + 1)  + "/" + inputDict.size());
			if (completed == 1) {
				System.out.println("\nMerging temp files....");
				MergeTempOutputFiles();
				System.out.println("SIFT4G Annotation completed !");
				System.out.println("Output directory:" + cmdoutDir.getPath());
				endTimestamp = Calendar.getInstance();
				endTimestamp.add(Calendar.DATE, 1);
				endTime = endTimestamp.getTime().toString();
				System.out.println("End Time for parallel code: " + endTime +"\n");
				try {
					System.exit(0);
				} catch (Exception e) {
					System.exit(-1);
				}
			}
		}
	}

	public static void MergeTempOutputFiles() {
		OutputFileManager OFM = new OutputFileManager();
		String Outputfilename = null;
		if(HEADLESS==true)
			Outputfilename = ifm.GetFinalOutputFilename(isMultiTranscripts);
		else
			Outputfilename = ifm.GetFinalOutputFilename(multiTranscriptsChkBox.isSelected());

		try {

			OFM.CreateVCFOutPutFile(ifm.Directory_Path, Outputfilename, ifm.Headers);

			for (String temp_filename : TempFilesOrder) {
				try {
					temp_filename += ".vcf";
					OFM.WriteFile(ifm.Directory_Path, temp_filename ,Outputfilename+".vcf");
					OFM.DeleteFile(ifm.Directory_Path, temp_filename);
				} catch (IOException e) {
					// TODO Auto-generated catch block 
					e.printStackTrace();
				}
			}
			OFM.CloseVCFOutPutFile();
		} catch (Exception ex) {
			ex.printStackTrace();
		}


		List<String> TSVHeader = new ArrayList<String>();
		TSVHeader.add(SIFTConstants.TAB_DELIMITED_HEADER);
		try {
			OFM.CreateTSVOutPutFile(ifm.Directory_Path, Outputfilename, TSVHeader);
			for (String temp_filename : TempFilesOrder) {
				try {
					temp_filename += ".tsv";
					OFM.WriteFile(ifm.Directory_Path, temp_filename, Outputfilename+".tsv");
					OFM.DeleteFile(ifm.Directory_Path, temp_filename);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			OFM.CloseTSVOutPutFile();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		if(HEADLESS==false)
			main.informUserAboutOutputDir(ifm.Directory_Path);
	}

	public static void PrepareDisplayTable() {

		try {
			displayDict = new HashMap<String, String[]>();
			displayDictMapping = new HashMap<String, String>();
			List<String> myList = new ArrayList<String>(inputDict.keySet());
			Collections.sort(myList,new IntuitiveStringComparator<String>());
			for(int i=0;i<myList.size();i++){
				String chr =(String) myList.get(i);
				String[] templist = new String[3];
				templist[0] = chr;
				templist[1] = "Running..";
				templist[2] = "Running..";

				if(tryParseInt(chr.toUpperCase())==false){
					int chrnum = displayDict.size() + 1;
					displayDictMapping.put(chr, String.format("%04d", chrnum));
					displayDict.put(String.format("%04d", Integer.parseInt(String.format("%d", chrnum ))),templist);
				}
				else{
					displayDictMapping.put(chr,String.format("%04d", Integer.parseInt(chr)));
					displayDict.put(String.format("%04d", Integer.parseInt(chr)),templist);
				}
			}
			Map<String, String[]> treeMap = new TreeMap<String, String[]>(displayDict);
			resultsPanel.clearTable();
			for (String chr : treeMap.keySet()) {
				String[] DisplayLines = treeMap.get(chr);
				resultsPanel.addRow(DisplayLines);
			}
		} catch (Exception ex1) {

		}

		resultsPanel.validate();
	}

	static boolean tryParseInt(String value)  
	{  
		try  
		{  
			Integer.parseInt(value);  
			return true;  
		} catch(NumberFormatException nfe)  
		{  
			return false;  
		}  
	}

	private static void commandlinehelp(){
		System.err.println("java -jar MO-SIFT-21Apr14.jar OPTIONS");
		System.err.println();
		System.err.println("-c[boolean]  to run in command line ");
		System.err.println("-i[path]     path to input vcf file");
		System.err.println("-d[path]     path to database directory");
		System.err.println("-t[boolean]  Check Multitranscripts");
		System.err.println("-h[boolean]  print help");
		System.exit(1);
	}

}


/**
 * <p>A comparator that emulates the "intuitive" sorting used by Windows
 * Explorer.  The rules are as follows:</p>
 *
 * <ul><li>Any sequence of one or more digits is treated as an atomic unit, a
 * number.  When these number units are matched up, they're compared according
 * to their respective numeric values.  If they're numerically equal, but one
 * has more leading zeroes than the other, the longer sequence is considered
 * larger.</li>
 * <li>Numbers always sort before any other kind of character.</li>
 * <li>Spaces and all punctuation characters always sort before letters.</li>
 * <li>Letters are sorted case-insensitively.</li></ul>
 *
 * <p>Explorer's sort order for punctuation characters is not quite the same
 * as their ASCII order.  Also, some characters aren't allowed in file names,
 * so I don't know how they would be sorted.  This class just sorts them all
 * according to their ASCII values.</p>
 *
 * <p>This comparator is only guaranteed to work with 7-bit ASCII strings.</p>
 *
 * @author Alan Moore
 */
class IntuitiveStringComparator<T extends CharSequence>
implements Comparator<T>
{
	private T str1, str2;
	private int pos1, pos2, len1, len2;

	public int compare(T s1, T s2)
	{
		str1 = s1;
		str2 = s2;
		len1 = str1.length();
		len2 = str2.length();
		pos1 = pos2 = 0;

		if (len1 == 0)
		{
			return len2 == 0 ? 0 : -1;
		}
		else if (len2 == 0)
		{
			return 1;
		}

		while (pos1 < len1 && pos2 < len2)
		{
			char ch1 = str1.charAt(pos1);
			char ch2 = str2.charAt(pos2);
			int result = 0;

			if (Character.isDigit(ch1))
			{
				result = Character.isDigit(ch2) ? compareNumbers() : -1;
			}
			else if (Character.isLetter(ch1))
			{
				result = Character.isLetter(ch2) ? compareOther(true) : 1;
			}
			else
			{
				result = Character.isDigit(ch2) ? 1
						: Character.isLetter(ch2) ? -1
								: compareOther(false);
			}

			if (result != 0)
			{
				return result;
			}
		}

		return len1 - len2;
	}

	private int compareNumbers()
	{
		int delta = 0;
		int zeroes1 = 0, zeroes2 = 0;
		char ch1 = (char)0, ch2 = (char)0;

		// Skip leading zeroes, but keep a count of them.
		while (pos1 < len1 && (ch1 = str1.charAt(pos1++)) == '0')
		{
			zeroes1++;
		}
		while (pos2 < len2 && (ch2 = str2.charAt(pos2++)) == '0')
		{
			zeroes2++;
		}

		// If one sequence contains more significant digits than the
		// other, it's a larger number.  In case they turn out to have
		// equal lengths, we compare digits at each position; the first
		// unequal pair determines which is the bigger number.
		while (true)
		{
			boolean noMoreDigits1 = (ch1 == 0) || !Character.isDigit(ch1);
			boolean noMoreDigits2 = (ch2 == 0) || !Character.isDigit(ch2);

			if (noMoreDigits1 && noMoreDigits2)
			{
				return delta != 0 ? delta : zeroes1 - zeroes2;
			}
			else if (noMoreDigits1)
			{
				return -1;
			}
			else if (noMoreDigits2)
			{
				return 1;
			}
			else if (delta == 0 && ch1 != ch2)
			{
				delta = ch1 - ch2;
			}

			ch1 = pos1 < len1 ? str1.charAt(pos1++) : (char)0;
			ch2 = pos2 < len2 ? str2.charAt(pos2++) : (char)0;
		}
	}

	private int compareOther(boolean isLetters)
	{
		char ch1 = str1.charAt(pos1++);
		char ch2 = str2.charAt(pos2++);

		if (ch1 == ch2)
		{
			return 0;
		}

		if (isLetters)
		{
			ch1 = Character.toUpperCase(ch1);
			ch2 = Character.toUpperCase(ch2);
			if (ch1 != ch2)
			{
				ch1 = Character.toLowerCase(ch1);
				ch2 = Character.toLowerCase(ch2);
			}
		}

		return ch1 - ch2;

	}
}

