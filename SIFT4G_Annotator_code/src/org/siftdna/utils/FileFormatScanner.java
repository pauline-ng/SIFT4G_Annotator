package org.siftdna.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.siftdna.main.SIFTConstants;

/**********************************************************
 * 
 * Utility class that helps determine the file format
 * of the user's input file - VCF or SIFT format
 * 
 * @author simnl
 * @date: 2013.12.27
 *********************************************************/



public class FileFormatScanner {

	public FileFormatScanner() {
		super();
	}//end getFileFormat

	public String getFileFormat(String inputFile) {
		Scanner scanner = null;
		String format = SIFTConstants.UNKNOWN_FORMAT;
		try {
			scanner = new Scanner(new File(inputFile));
			if (scanner.hasNext()) {
				String s = scanner.next();
				if (s.contains("#fileformat") || s.startsWith("#")) {
					format = SIFTConstants.VCF_FORMAT;
				} else { // Check if it is SIFT format, and if so, which one
					String [] comp = s.split(",");
					if (comp.length > 3) {
						long firstPos = Long.valueOf(comp[1]).longValue();
						long secondPos = Long.valueOf(comp[2]).longValue();						
						if (secondPos == (firstPos + 1)) {
							format = SIFTConstants.SIFT_SPACE_FORMAT;
						} else if (secondPos == 1 || secondPos == -1) { // that is, orientation, then it is in residue format
							format = SIFTConstants.SIFT_RESIDUE_FORMAT; 
						}
					}
				} 
			} else {
				return format;
			}
			scanner.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
			return format;
		}
		return format;
	}//end getFileFormat

	public List<String> getVCFHeaders(String inputFile, float siftPredictionThreshold, String fileFormat) {
		List<String> headers = new ArrayList<String>();

		if (!fileFormat.equals(SIFTConstants.VCF_FORMAT)) {
			headers.add(SIFTConstants.VCF_FIRST_ROW);
			String thresholdHeader = "##SIFT_PREDICTION_THRESHOLD=" + Float.toString(siftPredictionThreshold);	
			headers.add(thresholdHeader);
			headers.add(SIFTConstants.SIFT_HEADER);
			headers.add(SIFTConstants.SIFT_HEADER1);
		} else {
			FileInputStream fin = null;		    
			InputStreamReader isr = null;
			BufferedReader br = null;

			try {
				fin = new FileInputStream(inputFile);
				isr = new InputStreamReader(fin);
				br = new BufferedReader(isr);
				String line = null;
				boolean hitLastHeader = false;
				while(((line = br.readLine()) != null) && hitLastHeader == false) {
					if (line.startsWith("##")) {
						System.out.println("line: " + line);
						headers.add(line);
					} else if (line.startsWith("#CHROM") || line.startsWith("CHROM")) {
						headers.add(SIFTConstants.SIFT_HEADER);					
						String thresholdHeader = "##SIFT_PREDICTION_THRESHOLD=" + Float.toString(siftPredictionThreshold);					
						headers.add(thresholdHeader);
						headers.add(line); // last line #CHROM POS ...				
						hitLastHeader = true;
					}
				}//end while
			} catch (IOException ioe){
				ioe.printStackTrace();
			} finally {
				try {
					br.close();
					isr.close();
					fin.close();
					return headers;
				} catch (IOException ioe) {
					ioe.printStackTrace();					
				}
			}//end finally		

		}//end else

		return headers;
	}
}//end FileFormatScanner


