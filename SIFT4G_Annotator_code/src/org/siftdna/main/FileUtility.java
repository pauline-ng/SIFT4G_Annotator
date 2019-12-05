package org.siftdna.main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.siftdna.siftobjects.ISIFTQuery;
import org.siftdna.siftobjects.SIFTQuery;
import org.siftdna.siftobjects.SIFTQueryGenerator;

/**********************************
 * Utility class to split by chromosomes and then sort
 * @author simnl
 * @date 2013.12.18
 **********************************/

public class FileUtility {

	public void prepareDirectories(String outputDir) {
		File odir = new File(outputDir);
		if (!odir.exists()) { odir.mkdir(); }		
	}
	
	
	public String [] splitInputsByChromosomes(String inputFile, String fileFormat, String intermediateDirectory)
	// Pre-condition: User's VCF file MUST be sorted, or at least all variants in the same Chromosome must be in consecutive rows.
	{
		Set<String> chromosomesFromInputFile = new HashSet<String>();
		
		String delimiter = null;
		if (fileFormat.equals(SIFTConstants.VCF_FORMAT)) {
			delimiter = SIFTConstants.VCF_DELIMITER;
		} else {
			delimiter = SIFTConstants.SIFT_DELIMITER;
		}
		
		List<String> files = new ArrayList<String>();
		
		FileInputStream fin = null;
		InputStreamReader isr = null;
		BufferedReader br = null;
		try {
			fin = new FileInputStream(inputFile);		    
			isr = new InputStreamReader(fin);
			br = new BufferedReader(isr);
			
			FileWriter filewriter = null;
			BufferedWriter out = null;
			String currentChromosome = null;
			
			String line = null;
			while ((line = br.readLine()) != null) {
				boolean isHeader = false;
				if (fileFormat.equals(SIFTConstants.VCF_FORMAT)) {
					if (line.startsWith("#")) {
						isHeader = true;
					}
				}
				if (!isHeader) {
					String [] components = line.split(delimiter);				
					String chromosome = components[0];
					if (chromosome.startsWith("chr")) { 
						chromosome = chromosome.substring(3); // For consistency, we do not keep chr 
					}
					if (currentChromosome == null || !currentChromosome.equals(chromosome)) {
						if (out != null) { out.close(); }
						if (filewriter != null) { filewriter.close(); }
						String filename = intermediateDirectory + "/chr" + chromosome + ".sorted.query";
						chromosomesFromInputFile.add(chromosome + "\t" + filename);
						
						filewriter = new FileWriter(filename);
						out = new BufferedWriter(filewriter);
						currentChromosome = chromosome;
						out.write(line);
					} else {
						out.write(line);
					}
				}
			}
			
			out.close();
			filewriter.close();
			
		} catch (IOException ioe){
			ioe.printStackTrace();
		} finally {
			try {
				br.close();
				isr.close();
				fin.close();
				
				
			} catch (IOException ioe) {
				ioe.printStackTrace();				
			}
		}
		
		return chromosomesFromInputFile.toArray(new String[chromosomesFromInputFile.size()]);
	}
	
	
	public String writeFinalOutputAsVCF(String outputDir, String outputFileName, List<String> headers,
			String intermediateDir, List<String> chromosomes, String fileFormat) {

		String outputFile = outputDir + System.getProperty("file.separator") + outputFileName;
		FileWriter filewriter = null; 
		BufferedWriter out = null; 		
		
		try {
			
			filewriter = new FileWriter(outputFile);
			out = new BufferedWriter(filewriter);
			
			
			// Print out headers
			for(String header : headers) {
				out.write(header + "\n");				
			}			
			// Now we write out the results
			FileInputStream fin = null;		    
			InputStreamReader isr = null;
			BufferedReader br = null;
			
			for (String chr : chromosomes) {
				String file = intermediateDir + chr + ".siftscores.vcf";
				System.out.println("File: " + file);
				fin = new FileInputStream(file);
				isr = new InputStreamReader(fin);
				br = new BufferedReader(isr);
				
				String line = null;
				while((line = br.readLine()) != null) { out.write(line + "\n"); } 
				
				br.close();
				isr.close();
				fin.close();
			}
		} catch (IOException ioe){
			ioe.printStackTrace();
		} finally {
			try {
				out.close();
				filewriter.close();
				return outputFile;
			} catch (IOException ioe) {
				ioe.printStackTrace();							
			}
		}		
		return outputFile;
	}
	
	public String writeFinalOutputAsTab(String outputDir,
			String outputFileName, String intermediateDir,
			List<String> chromosomes, String fileFormat) {
		
		String outputFile = outputDir + System.getProperty("file.separator") + outputFileName;
		FileWriter filewriter = null; 
		BufferedWriter out = null; 		
				
		try {
			
			filewriter = new FileWriter(outputFile);
			out = new BufferedWriter(filewriter);
			
			out.write(SIFTConstants.TAB_DELIMITED_HEADER + "\n");
			
			
			for (String chr : chromosomes) {
				String file = intermediateDir + chr + ".siftscores.txt";

				FileInputStream fin = null;		    
				InputStreamReader isr = null;
				BufferedReader br = null;
				
				fin = new FileInputStream(file);
				isr = new InputStreamReader(fin);
				br = new BufferedReader(isr);				
				
				String line = null;
				while((line = br.readLine()) != null) { out.write(line + "\n"); }
				
				br.close();
				isr.close();
				fin.close();

				
			}
	
		} catch (IOException ioe){
			ioe.printStackTrace();
		} finally {
			try {
				out.close();
				filewriter.close();
				return outputFile;
			} catch (IOException ioe) {
				ioe.printStackTrace();							
			}
		}	
		return outputFile;

	}
}
