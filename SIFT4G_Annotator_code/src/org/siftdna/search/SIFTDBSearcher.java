package org.siftdna.search;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
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

public class SIFTDBSearcher extends Thread {

	private String queryFile = null;
	private String dbFile = null;
	private String regionsFile = null;
	private String resultsFile = null;
	private String resultsVCF = null;
	private String chromosome = null;

	private boolean multiTranscripts = false;
	private String fileFormat = null;
	
	private float siftPredThreshold = 0.05f;
	
	
	private boolean isDebug = true; // Turn this to false during production
	private SingleSIFTResult [] singleResults = null;
	
	public SIFTDBSearcher(String queryFile, String fileFormat, String dbFile, String regionsFile, String intermediateDir,
							 String name, String chromosome, boolean multiTranscripts, float siftPredThreshold) {
		super();
		
		// Input information
		this.chromosome = chromosome;
		this.queryFile = queryFile;
		this.dbFile = dbFile; // This is a gz file
		System.out.println("DB File: " + dbFile);
		System.out.println("Regions file: " + regionsFile);
		this.regionsFile = regionsFile;
		
		this.siftPredThreshold = siftPredThreshold;
		this.fileFormat = fileFormat;
		
		// Output files
		this.resultsFile = intermediateDir + "/intermediatechr" + name + ".txt"; // Tab delimited output file
		this.resultsVCF = intermediateDir + "/intermediatechr" + name + ".vcf"; // VCF output file 
		this.multiTranscripts = multiTranscripts; // true if user wants multiple transcripts to be printed out
		
	}//end SIFTScoreSearcher

	public void run() {

		List<SIFTResult> results = new ArrayList<SIFTResult>();

		// Input file readers
		FileInputStream query_fin = null; InputStreamReader query_isr = null; BufferedReader query_br = null;
		FileInputStream rgn_fin = null; InputStreamReader rgn_isr = null; BufferedReader rgn_br = null;
		InputStream dbfileStream = null; InputStream gzipStream = null; Reader decoder = null; BufferedReader db_br = null;
		
		// Output file writers
		FileWriter filewriter = null; BufferedWriter out = null; 
		FileWriter vcfWriter = null; BufferedWriter vcfOut = null;
				
		try {			
			// Step 1: Open file handlers for reading / writing
			dbfileStream = new FileInputStream(dbFile);   // Chromosome file (gz)
			gzipStream = new GZIPInputStream(dbfileStream);
			decoder = new InputStreamReader(gzipStream, "UTF-8");
			db_br = new BufferedReader(decoder);

			filewriter = new FileWriter(resultsFile, false); // TSV output file handler
			out = new BufferedWriter(filewriter);
			
			vcfWriter = new FileWriter(resultsVCF, false); // VCF output file handler
			vcfOut = new BufferedWriter(vcfWriter);
			
			query_fin = new FileInputStream(queryFile);		 // User's query file (can be VCF or SIFT format)    
			query_isr = new InputStreamReader(query_fin);
			query_br = new BufferedReader(query_isr);
			
			rgn_fin = new FileInputStream(regionsFile);	 // Regions file	    
			rgn_isr = new InputStreamReader(rgn_fin);
			rgn_br = new BufferedReader(rgn_isr);
			
			String queryline = null; // Each user query line is read into this string 			
			String regionLine = null; // Each row in the regions file is read into this string
			
			
			// Now, the next queryline may have the same position as the previous query line,
			// eg. 22,12345,12346,1,A/C
			//     22,12345,12346,1,A/T
			// or  22 12346 A C,T (for VCF)
			// we cache all lines from the database
			// whose positions corresponds to the current query.
			// When we read the next query, we start from this array before reading the next database line.
			List<SIFTDBLine> dbCache = new ArrayList<SIFTDBLine>(); 
			
			boolean queryIsVCF = fileFormat.equals(SIFTConstants.VCF_FORMAT);
			
			// Need to handle case where multiple SNVs are in same region
			// I store the boundaries found
			List<String> boundariesToRecheck = new ArrayList<String>();
			
			// Step 2: Iterate through query, row by row
			while ((queryline = query_br.readLine()) != null) { // Read 1 line from the user query

				if (queryIsVCF) { 
					if (queryline.startsWith("#")) { // User passes a VCF file, we ignore the headers					
						continue;
					}
				}

				ISIFTQuery query = SIFTQueryGenerator.generateQuery(queryline, fileFormat); // We generate a query object to represent the line
				
				long queryPosition = query.getPosition(); // We get the genomic co-ordinate from the query

				// Step 2a: Check if position is in or out of region using the region file
				boolean boundariesFound = false;
				boolean isInCodingRegion = false;
				
				// Current user query may have positions in the same boundary as the previous position, need to re-check last boundary
				if (boundariesToRecheck.size() > 0) {
					for (String boundary : boundariesToRecheck) {
						String [] rgnData = boundary.split("\t");
						long low = Long.valueOf(rgnData[0]).longValue();
						long high = Long.valueOf(rgnData[1]).longValue();
						if (low <= queryPosition && queryPosition <= high) {
							isInCodingRegion = rgnData[2].equalsIgnoreCase(SIFTConstants.BOUNDARY_IN); // IN or OUT
							boundariesFound = true; // We have found the row in regions file that contains the genomic co-ordinate							
						}//end if (low <= queryPosition && queryPosition <= high)
					}//end for (String boundary : boundariesToRecheck)
				} else {					
					while(((regionLine = rgn_br.readLine()) != null) && // READLINE REGION FILE
							boundariesFound == false) {
						String [] rgnData = regionLine.split("\t");
						long low = Long.valueOf(rgnData[0]).longValue();
						long high = Long.valueOf(rgnData[1]).longValue();					
						if (low <= queryPosition && queryPosition <= high) {
							isInCodingRegion = rgnData[2].equalsIgnoreCase(SIFTConstants.BOUNDARY_IN); // IN or OUT
							boundariesFound = true; // We have found the row in regions file that contains the genomic co-ordinate
							int lastIndex = boundariesToRecheck.size() - 1;
							String lastBoundary = null;
							if (lastIndex >= 0) {
								lastBoundary = boundariesToRecheck.get(lastIndex);								
							}
							boundariesToRecheck.clear();
							if (lastBoundary != null) {
								boundariesToRecheck.add(lastBoundary);
							}
							boundariesToRecheck.add(regionLine); // We have already read that next line, pointer is at row after that, so we need to re-check this.
						}
					}//end while
				}//end else
				
				if (boundariesFound == false) { 
					isInCodingRegion = false; // If we have reached the end of the regions file, then it must be out of coding region. 
				} // If we go over the entire regions file, boundariesFound would be false.
				
				debug(isInCodingRegion + "\t" + queryline);

				// Step 2b: if isInCodingRegion, then we continue, otherwise, we create a SIFTResult with no scores				
				SIFTResult siftResult = new SIFTResult(query); 
				if (isInCodingRegion) {
					// First, go through the cache for the last position read
					// see if we obtain any of these
					boolean isSamePositionAsLast = false;
					for (SIFTDBLine dbLine : dbCache) {				
						long dbPosition = dbLine.getPosition();
						if (dbPosition == query.getPosition()) { // Then this is query happens to have the same position as the previous query line
							isSamePositionAsLast = true;
							break; // I only need 1 from the array if it's the same position
						}//end if (dbPosition == query.getPosition())						
					}//end for (String dbcache : dbCache)
					
					if (isSamePositionAsLast == false) {
						// Then we clear the cache, because since queries are sorted, remaining queries have positions > dbCache position
						// Also, this means we need to continue iterating over the database until we hit the first position that matches.
						dbCache.clear(); 
												
						boolean hitFirstPosition = false;
						String lineFromDB = null;
						List<SIFTDBLine> candidatesWithSamePosition = new ArrayList<SIFTDBLine>();
						while(((lineFromDB = db_br.readLine()) != null) && hitFirstPosition == false) {
							SIFTDBLine candidateDBLine = new SIFTDBLine(lineFromDB);
							if (candidateDBLine.getPosition() == query.getPosition()) {
								hitFirstPosition = true;
								candidatesWithSamePosition.add(candidateDBLine);
								dbCache.add(candidateDBLine);
							}
						}//end while(((lineFromDB = db_br.readLine()) != null) && hitFirstPosition == false)
						boolean hitPositionAfterThisPosition = false;
						while(((lineFromDB = db_br.readLine()) != null) && hitPositionAfterThisPosition == false) {
							SIFTDBLine candidateDBLine = new SIFTDBLine(lineFromDB);
							if (candidateDBLine.getPosition() == query.getPosition()) {
								candidatesWithSamePosition.add(candidateDBLine);
								dbCache.add(candidateDBLine);
							} else {
								hitPositionAfterThisPosition = true; // this breaks us up out of the 
							}
						}// while(((lineFromDB = db_br.readLine()) != null) && hitPositionAfterThisPosition == false)
					}//if (isSamePositionAsLast == false	
					else {
						// We don't have to do anything, in particular, we DO NOT clear the dbCache 
						// the dbCache will contain the rows that need to be checked against this current query.
					}
					
					// Now, dbCache contains all SIFT database rows where position are the same
					// We now iterate through by comparing with reference and alternate allele(s)
					// User may have provided reversed complemented ref/alt alleles, we check the reverse complements as well.
					String queryRef = query.getRefAllele();
					List<String> alternateAllelesInQuery = query.getAltAlleles(); // If VCF, each row may contain more than 1 alt allele (eg. C,T)
					for(String queryAlt : alternateAllelesInQuery) {
						for (SIFTDBLine dbline : dbCache) {
							String dbRef = dbline.getRefAllele();
							String dbAlt = dbline.getAltAllele();														
							if ((dbRef.equalsIgnoreCase(queryRef) && dbAlt.equalsIgnoreCase(queryAlt))) {

								// Then we want this
								for (String alt : query.getAltAlleles()) { 
									siftResult.addAlt_alleles(alt); 
								}
								siftResult.addTranscript_ids(dbline.getTranscriptID());								
								siftResult.addGene_ids(dbline.getGeneID());
								siftResult.addGene_name(dbline.getGeneName());	
								siftResult.addRegion(dbline.getRegion());	
								siftResult.addRef_amino(dbline.getRefAmino());
								siftResult.addAlt_aminos(dbline.getAltAmino());	
								siftResult.addAmino_positions(dbline.getAminoPosition());	
								siftResult.addSiftScores(dbline.getSiftScore());
								siftResult.addSiftPrediction(dbline.getSiftScore(), siftPredThreshold);
								siftResult.addSiftMedians(dbline.getSiftMedian());
								siftResult.addSiftNumSeqs(dbline.getSiftNumSeqs());
								//SIFTResult.adddbSNP(dbline.getdbSNP());
								siftResult.addAnnotations(dbline.getdbSNP());								
							}//end if ((dbRef.equals(queryRef) && dbAlt.equals(queryAlt))							
						}//end for (SIFTDBLine dbline : dbCache)
					}//end for(String alt : alternateAllelesInQuery)					
				}//end if (inCodingRegion)	
				results.add(siftResult);				
			}//end while ((queryline = query_br.readLine()) != null)
			
			// This is used to create both the TSV file, and the table in the GUI
			SIFTResultsTableHelper helper = new SIFTResultsTableHelper();
			singleResults = helper.createSingleSIFTResults(results, multiTranscripts);
			
			for (SIFTResult r : results) {
				String line = r.createVCFLine(queryIsVCF, multiTranscripts);	
				System.out.println(line);
				vcfOut.write(line + "\n"); // Write out in VCF format for 1 chromosome (each SIFTDBSearcher deals only with 1 chromosome)
				
				String [] l = r.createTabDelimitedLines(multiTranscripts);
				if (l.length > 0) {
					for(String s : l) {
						out.write(s + "\n"); // Write out in tab-delimited format for 1 chromosome
					}
				}

			}
			
		} catch (NumberFormatException nfe) {
			nfe.printStackTrace();
		} catch (IOException ioe){
			ioe.printStackTrace();
		} finally {
			try {	
				rgn_br.close();
				rgn_isr.close();
				rgn_fin.close();
								
				query_br.close();
				query_isr.close();
				query_fin.close();
								
				db_br.close();
				decoder.close();
				gzipStream.close();
				dbfileStream.close();
				
				vcfOut.close();
				vcfWriter.close();
				
				out.close();
				filewriter.close();

			} catch (IOException ioe) {
				ioe.printStackTrace();				
			}
		}//end finally		

	}//end run method
	
	public SingleSIFTResult [] getSingleResults() {
		return singleResults;
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