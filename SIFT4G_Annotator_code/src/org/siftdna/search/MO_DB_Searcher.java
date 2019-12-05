package org.siftdna.search;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.siftdna.gui.utils.SIFTResultsTableHelper;
import org.siftdna.main.SIFTConstants;
import org.siftdna.siftobjects.ISIFTQuery;
import org.siftdna.siftobjects.SIFTDBLine;
import org.siftdna.siftobjects.SIFTQueryGenerator;
import org.siftdna.siftobjects.SIFTResult;
import org.siftdna.siftobjects.SingleSIFTResult;

public class MO_DB_Searcher extends Thread {

	private String queryFile = null;
	private String dbDir = null;
	private String outputDir = null;
	private String outputFileName = null;
	
	
	private String regionsFile = null;
	private String resultsFile = null;
	private String resultsVCF = null;
	private String chromosome = null;

	private boolean multiTranscripts = false;
	private String fileFormat = null;
	
	private float siftPredThreshold = 0.05f;
	
	
	private boolean isDebug = true; // Turn this to false during production
//	private SingleSIFTResult [] singleResults = null;

	public MO_DB_Searcher(String inputFile, 
			String fileFormat, String dbDir, String outputDir, String outputFileName, boolean multipleTranscripts, float siftPredictionThreshold) {

		this.queryFile = inputFile;
		this.fileFormat = fileFormat;
		this.dbDir = dbDir;
		this.outputDir = outputDir;
		this.outputFileName = outputFileName;
		this.multiTranscripts = multipleTranscripts;
		this.siftPredThreshold = siftPredictionThreshold;		
	}//end 


	public void run() {

		FileInputStream query_fin = null; InputStreamReader query_isr = null; BufferedReader query_br = null;
		FileWriter tsvWriter = null; BufferedWriter tsvOut = null; 
		FileWriter vcfWriter = null; BufferedWriter vcfOut = null;
		try {
			// Step 1: Open Input File			
			query_fin = new FileInputStream(queryFile); // VCF or SIFT Format
			query_isr = new InputStreamReader(query_fin);
			query_br = new BufferedReader(query_isr);
			
			// Open output file writers
			String vcfOutFile = outputDir + System.getProperty("file.separator") + outputFileName + ".vcf";
			String tsvOutFile = outputDir + System.getProperty("file.separator") + outputFileName + ".tsv";
			vcfWriter = new FileWriter(vcfOutFile);
			
			
			String queryline = null; // Each user query line is read into this string 			
			while((queryline = query_br.readLine()) != null) {
				
			}
			
			
			
		} catch (NumberFormatException nfe) {
			nfe.printStackTrace();
		} catch (IOException ioe){
			ioe.printStackTrace();
		} finally {
			// Close file streams
			try {
				query_br.close();
				query_isr.close();
				query_fin.close();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
		
	}
	
	
	public String getThreadName() {
		return this.chromosome;
	}
	
	private void debug(String line) {
		if (isDebug == true) {
			System.out.println(line);
		}
	}
}