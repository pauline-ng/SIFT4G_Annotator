package org.siftdna.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
//import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.siftdna.gui.RuntimeError;

//import javax.swing.JPopupMenu.Separator;




import org.siftdna.utils.FileFormatScanner;

public class InputFileManager {
	private String inputFile = null;
	private static RuntimeError runtimeError = null;
	FileInputStream query_fin = null; 
	InputStreamReader query_isr = null; 
	BufferedReader query_br = null;

	List<String> Headers = null;
	Map<String, List<String>> inputDict= null;
	List<String> ChrOrder = new ArrayList<String>();
	Integer InpCol = new Integer(0);
	private String fileFormat = null;
	final String delimiter = "\t"; // For SIFT format

	private String filename;
	public String Directory_Path;

	public InputFileManager() {

	}

	public void Getfileinfo(String inputFile, String outputDir){
		File f = new File(inputFile);
		File o = new File(outputDir);
		String seperator = System.getProperty("file.separator");
		Directory_Path = o.getPath();
		filename = f.getAbsolutePath().substring(f.getAbsolutePath().lastIndexOf(seperator) + 1);
	}
	public String GetOsType(){
		return System.getProperty("os.name");
	}

	public String GetFinalOutputFilename(boolean isMultiTranscripts){
		String outputFileName =null;
		if (filename.contains(".")) {
			outputFileName = filename.substring(0, filename.lastIndexOf('.'));
		}
		if(isMultiTranscripts)
			return outputFileName + "_SIFTpredictions";
		return outputFileName + "_SIFTpredictions";
	}
	
	public String GetFinalVcfOutputFileName(boolean isMultiTranscripts){
		String outputFileName = GetFinalOutputFilename(isMultiTranscripts);
		String VcfOuput = Directory_Path + System.getProperty("file.separator") + outputFileName + ".vcf";
		return VcfOuput;
	}
	
	public String GetFinalTsvOutputFileName(boolean isMultiTranscripts){
		String outputFileName = GetFinalOutputFilename(isMultiTranscripts);
		String file = outputFileName.substring(0, outputFileName.lastIndexOf("_SIFT"));
		String TsvOuput = Directory_Path + System.getProperty("file.separator") + file + "_SIFTannotations.xls";
		return TsvOuput;
	}
	

	
	public String Getoutputfilename()
	{
		String outputFileName =null;	
		if (filename.contains(".")) {
			outputFileName = filename.substring(0, filename.lastIndexOf('.'));
		} else {
			outputFileName = filename;
		}
		return outputFileName += "_SIFTPredictions";
	}

	public List<String> GetChrOrder(){
		// This function has to be called only after calling InitInputFileManager function
		return ChrOrder;
	}

	public void checkChrMatchesDatabase (Map<String, List<String>> inputDict, String dbDir, boolean headless) {
		 List <String> listOfNonexistingChr = new ArrayList<String>();

		 /* iterate through inputDict and check that it's in dbDir, else throw an error */
		for (String chr : inputDict.keySet()) {
		  File siftDBFile = new File (dbDir+ System.getProperty("file.separator") + chr + ".gz");
		  if (siftDBFile.exists()) {
		      /* that's good */
		  } else {
		      /* that's not good */
			  listOfNonexistingChr.add (chr);
		  }
		} /* end of for loop */
		if (listOfNonexistingChr.size() > 0) {
			String unlistedChrLine = StringUtils.join(listOfNonexistingChr, ", ");
			if(headless == false){
				runtimeError = new RuntimeError ();			
				runtimeError.createChromosomeMismatchMessage(unlistedChrLine);
			} else {
				System.out.println("The following chromosomes (or scaffolds/contigs) are not found in the SIFT 4G database and will not be annotated:\n"           
          		+ unlistedChrLine);
				System.out.println("Please contact us if you have any questions.");
			}
		}
	} /* end of function */
	
	public Map<String, List<String>> InitInputFileManger(String inputFile, String outputDir, float siftPredictionThreshold, boolean isMultiTranscript, boolean headless) throws IOException{
		Getfileinfo(inputFile, outputDir);
		query_fin = new FileInputStream(inputFile); // VCF or SIFT Format
		query_isr = new InputStreamReader(query_fin);
		query_br = new BufferedReader(query_isr);
		// Get input file format
		FileFormatScanner ffs = new FileFormatScanner();
		fileFormat = ffs.getFileFormat(inputFile);

		Headers = new ArrayList<String>();
		inputDict = new HashMap<String, List<String>>();
		Set chrSeen = new HashSet();
		
		String queryline = null;
		String currentChromosome = null;
        long currentPos = 0;

		while((queryline = query_br.readLine()) != null) {

			// Step 3: Check if it is a VCF Header, if so, write out into output VCF file
			
			if (queryline.startsWith("#CHROM")) {
				// Append VCF info lines first		
				Headers.add("##SIFT_Threshold: " + siftPredictionThreshold + "\n");
				Headers.add(SIFTConstants.SIFT_HEADER + "\n"); // We also want to append our own SIFT headers
				Headers.add(queryline + "\n");
			} else if (queryline.startsWith("#")){
				Headers.add(queryline + "\n");
			} else {
				// Step 4: Check if need to change SIFT Database
				String [] components = queryline.split(delimiter); // parse VCF line
				InpCol = components.length;
				if(InpCol < SIFTConstants.REQ_COL){
					if(headless == false){
						// GUI col error
						runtimeError = new RuntimeError ();
						runtimeError.createColumnError(queryline);
					}
					else{
						System.out.println("ERROR! Input VCF file should contain at least 8 columns. See line: \n" + queryline );
						System.exit(1);
					}
					
				}
				//Get chromosome information.
				String chr = components[SIFTConstants.VCF_CHR_COL]; // Get chromosome		
				long pos = Long.valueOf (components[SIFTConstants.VCF_POS_COL]).longValue(); // get Positions

				
				if (chr.startsWith("chr")) 
				{ 
					chr = chr.substring(3); 
				}// Make it consistent by removing chr if it is there

				 if (currentChromosome == null || !currentChromosome.equals(chr)) {
                     currentChromosome = chr;
                     currentPos = pos;
                     if (!chrSeen.contains(chr)) {
                             chrSeen.add(currentChromosome);
                     } else { // seen this chromosome twice. not an ordered file, throw an error
                    	 	if(headless == false){
                    	 		runtimeError = new RuntimeError ();                    	 	
                    	 		runtimeError.createErrorForUnorderedFile (queryline);
                    	 	}	else {
                    	 		System.out.println ("ERROR! Chromosome positions should be sorted in ascending order.");
                    	 		System.out.println ("The line " + queryline + " seems out of place.");
                    	 		System.exit(1);
                    	 	}
                     }
				 }

				 if (pos < currentPos) { // file is out of order
                     if (headless == false) {
                    	 runtimeError = new RuntimeError ();
                    	 
                    	 runtimeError.createErrorForUnorderedFile (queryline);
                     } else {
                    	 System.out.println ("ERROR! Chromosome positions should be sorted in ascending order.");                     
                    	 System.out.println ("The line " + queryline + " seems out of place.");
                    	 System.exit(1);
                     }
				 } else {
                     currentPos = pos;
				 }

				//Creating dictionary for each chr.
				List<String> localList;
				if(inputDict.containsKey(chr))
				{
					continue;
				}else
				{
					localList = new ArrayList<String>();
					localList.add(queryline);
					inputDict.put(chr, localList);
					if(isMultiTranscript)
						ChrOrder.add(Getoutputfilename() +"_"+ chr+"_multiTranscripts");
					else
						ChrOrder.add(Getoutputfilename() +"_"+ chr);
				}
			}
		}

		return inputDict;
	}//End of the function
}
