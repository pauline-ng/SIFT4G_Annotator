package org.siftdna.genomes;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;












import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import org.siftdna.genomes.DownloadTask;

//import net.codejava.swing.JFilePicker;

/**
 * A Swing application that downloads file from an HTTP server.
 * @author www.codejava.net
 * 
 *
 */
public class SwingFileDownloadHTTP extends javax.swing.JDialog implements PropertyChangeListener  {

	private JTextField fieldFileName = new JTextField(20);
	private boolean closestatus = false;
	//private JLabel labelFileSize = new JLabel("File size (bytes): ");
	private JTextField fieldFileSize = new JTextField(20);
	private String saveDir =null;
	private JLabel labelProgress = new JLabel("Progress:");
	private JProgressBar progressBar = new JProgressBar(0, 100);
	private JProgressBar unzipBar = new JProgressBar(0,100);
	//private String downloadURL;
	//private String saveDir;
	//private SwingFileDownloadHTTP instance = null;
	/*public SwingFileDownloadHTTP(String downloadURL, String saveDir){
    	this.downloadURL = downloadURL;
    	this.saveDir = saveDir;
    }*/
	DownloadTask task = null;
	SwingFileDownloadHTTP instance = null;

	public SwingFileDownloadHTTP(JFrame mainFrame, final String location, final String path) {
		// TODO Auto-generated constructor stub
		super(mainFrame);
		loadComponents();
		instance = this;
		saveDir = path;
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				progressBar.setValue(0);
				task = new DownloadTask(instance, location, path);
				task.addPropertyChangeListener(instance);
				task.execute();
			}	
		});


	}


	void setFileInfo(String name, long l) {
		fieldFileName.setText(name);
		fieldFileSize.setText(String.valueOf(l));
	}

	/**
	 * Update the progress bar's state whenever the progress of download changes.
	 */
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals("progress")) {
			int progress = (Integer) evt.getNewValue();
			progressBar.setValue(progress);
		}
	}

	public String getFileName(){
		return fieldFileName.getText();
	}

	public String getFileSize(){
		return fieldFileSize.getText();
	}
	/*
	 * Launch the application

    public static void main(final java.awt.Frame dialogsParent, final String Link, final String Dir) {
        try {
            // set look and feel to system dependent
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
            	SwingFileDownloadHTTP instance = new SwingFileDownloadHTTP(Link, Dir);
            	instance.DownloadHTTP(dialogsParent, Link, Dir);
            	instance.setVisible(true);
            }
        });
    }*/

	private void loadComponents(){
		setTitle("Downloading and extracting the database");
		setModal(true);
		// set up layout

		Container contentPane = getContentPane();
		//contentPane.setLayout(new BorderLayout());
		//contentPane.setSize(new Dimension(250, 70));
		contentPane.setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.anchor = GridBagConstraints.WEST;
		constraints.insets = new Insets(5, 5, 5, 5);

		// set up components
		// filePicker.setMode(JFilePicker.MODE_SAVE);
		//filePicker.getFileChooser().setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		progressBar.setPreferredSize(new Dimension(550, 30));
		progressBar.setStringPainted(true);
		unzipBar.setPreferredSize(new Dimension(550,30));
		unzipBar.setStringPainted(true);
		// add components to the frame

		constraints.gridx = 0;
		constraints.gridy = 5;
		constraints.gridwidth = 1;
		constraints.anchor = GridBagConstraints.WEST;
		contentPane.add(labelProgress, constraints);
		//labelProgress.setPreferredSize(new Dimension(30, 25));
		//contentPane.add(labelProgress, BorderLayout.NORTH);

		constraints.gridx = 1;
		constraints.weightx = 1.0;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		contentPane.add(progressBar, constraints);
		//contentPane.add(progressBar, BorderLayout.CENTER);

		pack();
		setLocationRelativeTo(getOwner());    
		addWindowListener(new WindowAdapter() {
			@Override public void windowClosing(WindowEvent e) {
				showDownloadWarning();
			}
		});

	}


	protected void showDownloadWarning() {
		// TODO Auto-generated method stub

		int output = JOptionPane.showConfirmDialog(instance
				,"Are you sure you want to stop downloading?"
				,"WARNING"
				,JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE);
		if(output == JOptionPane.YES_OPTION || output == JOptionPane.CANCEL_OPTION){
			task.stopProcess();
			instance.dispose();
			deleteUnCompleted(getFileName(), saveDir);
			closestatus = true;
		}
		else if(output == JOptionPane.NO_OPTION){
			setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		}

	}
	
	private void deleteUnCompleted(String fname, String Dir) {
		// TODO Auto-generated method stub
		File file = new File(fname);
		File dir =  new File(Dir + File.separator+fname.substring(0, fname.lastIndexOf(".zip")));
		file.deleteOnExit();
		dir.deleteOnExit();
	}
	
	public  boolean isInterupted(){
		return closestatus;
	}

}
