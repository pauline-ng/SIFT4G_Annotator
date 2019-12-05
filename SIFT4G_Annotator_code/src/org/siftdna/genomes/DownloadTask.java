package org.siftdna.genomes;

import org.siftdna.genomes.HTTPDownloadUtil;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;




import java.io.OutputStream;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * Execute file download in a background thread and update the progress.
 * @author www.codejava.net
 *
 */
public class DownloadTask extends SwingWorker<Void, Void> {
	private static final int BUFFER_SIZE = 4096;   
	private String downloadURL;
	private String saveDirectory;
	private static SwingFileDownloadHTTP gui;

	public DownloadTask(SwingFileDownloadHTTP gui, String downloadURL, String saveDirectory) {
		this.gui = gui;
		this.downloadURL = downloadURL;
		this.saveDirectory = saveDirectory;
	}


	/**
	 * Executed in background thread
	 */
	@Override
	protected Void doInBackground() throws Exception {
		try {
			HTTPDownloadUtil util = new HTTPDownloadUtil();
			util.downloadFile(downloadURL);

			// set file information on the GUI
			gui.setFileInfo(util.getFileName(), util.getContentLength());

			String saveFilePath = saveDirectory + File.separator + util.getFileName();
			String Fname = util.getFileName();
			String ZipDir = saveDirectory + File.separator + Fname.substring(0,Fname.lastIndexOf(".zip"));
			InputStream inputStream = util.getInputStream();
			// opens an output stream to save into file
			FileOutputStream outputStream = new FileOutputStream(saveFilePath);

			byte[] buffer = new byte[BUFFER_SIZE];
			int bytesRead = -1;
			long totalBytesRead = 0;
			int percentCompleted = 0;
			long fileSize = util.getContentLength() + 1000;
			if(util.getContentLength()<0){
				fileSize = Long.MAX_VALUE ;
			}
			while ((bytesRead = inputStream.read(buffer)) != -1) {
				/*if(isOnline()){
					JOptionPane.showMessageDialog(gui,
							"Internet Problem", "Error",
							JOptionPane.ERROR_MESSAGE);
					stopProcess();
					outputStream.close();
					util.disconnect();
					deleteUnCompleted(Fname, ZipDir);
					gui.dispose();
					break;
				}*/
				if(!isCancelled()){
					outputStream.write(buffer, 0, bytesRead);
					totalBytesRead += bytesRead;
					percentCompleted = (int) (totalBytesRead * 100 / fileSize);
					setProgress(percentCompleted);      
					//System.out.println("Total Bytes : Bytes Read: "+ fileSize+" : "+totalBytesRead + "\tPercentage :"+ percentCompleted);
				}
			}
			//System.out.println("Total No of Bytes Read: "+ totalBytesRead + "\tPercentage :"+ percentCompleted);
			outputStream.close();
			unzipFunction(ZipDir, saveFilePath);
			setProgress(100);
			util.disconnect();   

		} catch (IOException ex) {
			JOptionPane.showMessageDialog(gui, "Error downloading file: " + ex.getMessage(),
					"Error", JOptionPane.ERROR_MESSAGE);           
			ex.printStackTrace();
			cancel(true);      
		}
		return null;
	}




	private static void unzipFunction(String destinationFolder, String zipFile) {

		File directory = new File(destinationFolder);
		// if the output directory doesn't exist, create it
		if(!directory.exists())
			directory.mkdirs();
		// buffer for read and write data to file
		byte[] buffer = new byte[2048];
		try {
			FileInputStream fInput = new FileInputStream(zipFile);
			ZipInputStream zipInput = new ZipInputStream(fInput);
			ZipEntry entry = zipInput.getNextEntry();
			while(entry != null){
				String entryName = entry.getName();
				File file = new File(destinationFolder + File.separator + entryName);
				//System.out.println("Unzip file " + entryName + " to " + file.getAbsolutePath());
				// create the directories of the zip directory
				if(entry.isDirectory()) {
					File newDir = new File(file.getAbsolutePath());
					if(!newDir.exists()) {
						boolean success = newDir.mkdirs();
						if(success == false) {
							System.out.println("Problem creating Folder");
							JOptionPane.showInputDialog(gui, "Problem creating Folder: " +  file.getAbsolutePath(), JOptionPane.ERROR_MESSAGE);
						}
					}
				}
				else {
					FileOutputStream fOutput = new FileOutputStream(file);
					int count = 0;
					while ((count = zipInput.read(buffer)) > 0) {
						// write 'count' bytes to the file output stream
						fOutput.write(buffer, 0, count);
					}
					fOutput.close();
				}
				// close ZipEntry and take the next one
				zipInput.closeEntry();
				entry = zipInput.getNextEntry();
			}
			// close the last ZipEntry
			zipInput.closeEntry();
			zipInput.close();
			fInput.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Executed in Swing's event dispatching thread
	 */
	@Override
	protected void done() {
		if (!isCancelled()) {
			//setProgress(100);
			JOptionPane.showMessageDialog(gui,
					"Database has been downloaded successfully!", "Message",
					JOptionPane.INFORMATION_MESSAGE);
			gui.dispose();
		}
	}


	public void stopProcess() {
		// TODO Auto-generated method stub
		this.cancel(true);
	}  
	
	protected Boolean isOnline() {
	    try {
	        Process process = java.lang.Runtime.getRuntime().exec("ping -c 1 www.google.com");
	        int returnVal = process.waitFor();
	        boolean reachable = (returnVal==0);
	        return reachable;
	    } catch (Exception e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
	    }
	    return false;
	}
}
