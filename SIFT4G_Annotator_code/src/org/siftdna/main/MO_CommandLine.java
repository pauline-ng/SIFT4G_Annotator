package org.siftdna.main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
//import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;
//import javax.print.attribute.standard.Chromaticity;


import org.apache.commons.lang.StringUtils;
import org.siftdna.siftobjects.SIFT_DB_COLS;
import org.siftdna.utils.FileFormatScanner;

public class MO_CommandLine implements Runnable {

	static Map<String, String> Display = new HashMap<String, String>();
	private SIFT4G_Main main = null;
	private String ChrNo;
	private String inputFile = null;
	private String outputDir = null;
	private String dbDir = null;
	boolean multipleTranscripts = false;
	private String outputFileName = null;
	private String fileFormat = null;
	private List<String> localList = null;
	private String PreservedDbLine = null;
	private float siftPredictionThreshold = 0.05f; // default is 0.05
	private String startTime = null;

	public MO_CommandLine(SIFT4G_Main main) {
		super();
		this.main = main;
	}

	public void setInfo(String inputFile, String outputDir, String dbDir,boolean multipleTranscripts,
			float threshold, String startTime,List<String> localList, String chrNo) {
		
		this.inputFile = inputFile;
		this.ChrNo = chrNo;
		this.outputDir = outputDir;
		this.dbDir = dbDir;
		this.multipleTranscripts = multipleTranscripts;
		this.siftPredictionThreshold = threshold;
		this.startTime = startTime; 
		this.localList = localList;
		//this.fileFormat = fileFormat;
		// Get input file format
		FileFormatScanner ffs = new FileFormatScanner();
		fileFormat = ffs.getFileFormat(inputFile);

		// We take input file, remove the extension (if there is one) and append _SIFTpredictions.
		File f = new File(inputFile);
		String filename = f.getAbsolutePath().substring(f.getAbsolutePath().lastIndexOf(System.getProperty("file.separator")) + 1);
		// Check to see if there is an extension
		if (filename.contains(".")) {
			outputFileName = filename.substring(0, filename.lastIndexOf('.'));
		} else {
			outputFileName = filename;
		}
		outputFileName += "_SIFTPredictions_" + chrNo;

	}

	@Override
	public void run() {
		// region Variables Declaration. Variables to store rows read from regions and database
		String dbLine = null;
		String regionsLine = null;
		List<String> dbForPosition = new ArrayList<String>();
		int nLine = 0;
		String queryline = null; // Each user query line is read into this string
		String delimiter = ","; // For SIFT format
		if (fileFormat.equals(SIFTConstants.VCF_FORMAT)) {
			delimiter = "\t";
		}
		
		FileInputStream query_fin = null; InputStreamReader query_isr = null;
		BufferedReader query_br = null;
		// Open Output File Writers
		FileWriter tsvWriter = null;
		BufferedWriter tsvOut = null;
		FileWriter vcfWriter = null;
		BufferedWriter vcfOut = null;
		// Prepare output file names
		String vcfOutFile = outputDir + System.getProperty("file.separator") + outputFileName + ".vcf";
		String tsvOutFile = outputDir + System.getProperty("file.separator") + outputFileName + ".tsv";
		if (multipleTranscripts) {
			vcfOutFile = outputDir + System.getProperty("file.separator") + outputFileName + "_multiTranscripts.vcf";
			tsvOutFile = outputDir + System.getProperty("file.separator") + outputFileName + "_multiTranscripts.tsv";
		}
		try {
			vcfWriter = new FileWriter(vcfOutFile, false);
			vcfOut = new BufferedWriter(vcfWriter);

			tsvWriter = new FileWriter(tsvOutFile, false);
			tsvOut = new BufferedWriter(tsvWriter);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		// region Database file preparation
		InputStream dbfileStream = null;
		InputStream gzipStream = null;
		Reader decoder = null;
		BufferedReader db_br = null;
		FileInputStream rgn_fin = null;
		InputStreamReader rgn_isr = null;
		BufferedReader rgn_br = null;

		// region notification data preparation Variable to track which chromosome the query is looking at,
		// if the next row's chromosome is different, then we close current dbFile and open the right chromosome file.
		String currentChromosome = null;
		long numberWithSIFTAnnotations = 0;
		long numberWithoutSIFTAnnotations = 0;
		// endregion

		try {

			query_fin = new FileInputStream(inputFile); // VCF or SIFT Format
			query_isr = new InputStreamReader(query_fin);
			query_br = new BufferedReader(query_isr);
			while((queryline = query_br.readLine()) != null) {
				boolean isIn = false;
				boolean foundRegion = false;
				boolean allLinesWithCorrespondingPosFound = false;

				// Step 3: Check if it is a VCF Header, if so, write out into output VCF file
				if (queryline.startsWith("##")) { 
					continue;
				} else if (queryline.startsWith("#CHROM")) { 
					continue; 
				}
				//Step 4: Check if need to change SIFT Database
				String[] components = queryline.split(delimiter); // parse VCF line
				String chr = components[SIFTConstants.VCF_CHR_COL]; // Get chromosome
				if (chr.startsWith("chr")) {
					chr = chr.substring(3);
				}
				if(!chr.equals(this.ChrNo)){
					continue;
				}
				else
				{}			
				if(currentChromosome == null){
					//database file preparation
					// prepare db input files
					String siftDBFile = dbDir+ System.getProperty("file.separator") + chr + ".gz";
					String siftRegionsFile = dbDir+ System.getProperty("file.separator") + chr + ".regions";
					File dbFile = new File(siftDBFile);
					File regionsFile = new File(siftRegionsFile);
					//region missing database or region file
					if (!dbFile.exists() || !regionsFile.exists()) {
						nLine = nLine +1;
						System.out.println(regionsFile + " does not exist");
					} 

					//region database and chr region files found
					else {
						// We open the db file stream & regions file stream for the new chromosome
						dbfileStream = new FileInputStream(siftDBFile); // Chromosome file (gz)
						gzipStream = new GZIPInputStream(dbfileStream);
						decoder = new InputStreamReader(gzipStream, "UTF-8");
						db_br = new BufferedReader(decoder);
						rgn_fin = new FileInputStream(siftRegionsFile); // Regions file
						rgn_isr = new InputStreamReader(rgn_fin);
						rgn_br = new BufferedReader(rgn_isr);
						dbLine = null; // We null it so that it doesn't  'flow' over to the next chromosome
					}
				}
		
				// At this stage, we should have the correct files opened
				// Step 5: Now we get the query's genomic co-ordinate, iterate down the regions file, and look for the boundaries containing this co-ordinate
				//region get query coordinate, ref and alternate allele
				long query_coordinate = Long.valueOf(components[SIFTConstants.VCF_POS_COL]).longValue();
				String queryReferenceAllele = components[SIFTConstants.VCF_REF_COL];
				String[] alternateAlleles = components[SIFTConstants.VCF_ALT_COL].split(","); // VCF may have A,G in each row				
				if (regionsLine != null) {
					String[] rgnComponents = regionsLine.split("\t"); // Check the current regions line that was read
					long startPos = Long.valueOf(rgnComponents[0]).longValue();
					long endPos = Long.valueOf(rgnComponents[1]).longValue();
					if ((startPos <= query_coordinate)&& (query_coordinate <= endPos)) {
						foundRegion = true;
						isIn = rgnComponents[2].equalsIgnoreCase("IN");
					}
				}


				//region search for query coordinate in regions file 
				// *Important*: We use short circuit evaluation, so that we do not read the next regions line pre-maturely
				while (foundRegion == false  && nLine ==0 &&  (regionsLine = rgn_br.readLine()) != null) {
					String[] rgnComponents = regionsLine.split("\t");
					long startPos = Long.valueOf(rgnComponents[0]).longValue();
					long endPos = Long.valueOf(rgnComponents[1]).longValue();
					if ((startPos <= query_coordinate)&& (query_coordinate <= endPos)) {
						foundRegion = true;
						isIn = rgnComponents[2].equalsIgnoreCase("IN");
					}
				}

				// At this stage, we know if the co-ordinate is in an IN or OUT region by looking at the boolean isIn
				//If IN found in regions file for query cooirdinate
				
				if (isIn == true) { // Then, we know the SIFT database contains rows with this  genomic-coordinate

					// Step 6: Get SIFT database rows with corresponding genomic-coordinates
					// dbForPosition array contains the row(s) that were used in the previous iteration
					// Since this current query's co-ordinate may have the same position, we iterate over again
					// to just to check.If it is, we already have the correct set of SIFT DB rows to compare against,
					// otherwise, we clear the array in order to store the correct set once we iterate to the right row(s) in
					// the database. (officially, VCF co-ordinates are unique per row,
					// but we do not make any assumptions, because user may have 'modified' their VCFs)

					if (dbForPosition.size() > 0) {
						// May be the same position as the last query, we check
						String db_position = dbForPosition.get(0).split("\t")[0];
						long dbPos = Long.valueOf(db_position).longValue();
						if (dbPos == query_coordinate) { 
							// I only need to check 1
							allLinesWithCorrespondingPosFound = true; 
							// This will prevent the file "pointer" from iterating down in the while loop below.
						} else {
							dbForPosition.clear(); // Remove all cached database rows.
						}
					}

					if(PreservedDbLine!= null)
					{
						String[] localdbcomponent = PreservedDbLine.split("\t");
						long localdbPos = Long.valueOf(localdbcomponent[0]).longValue();
						if(localdbPos == query_coordinate ){
							dbForPosition.add(PreservedDbLine);
							PreservedDbLine = null;
						}
					}
					 
					//search for all rows in db that corresponds to query genomic coordinate
					// Then we know there is a corresponding set of genomic coordinates in the SIFT DB file
					// We need to get all the rows that correspond to the genomic coordinate
					// *Important*: We use short circuit evaluation, so that we do not read the next SIFT DB line pre-maturely
					while ((allLinesWithCorrespondingPosFound == false)&& (dbLine = db_br.readLine()) != null) {
						if (dbLine.startsWith("#")){
							continue;
						}
						String[] dbComponents = dbLine.split("\t");
						long dbPos = Long.valueOf(dbComponents[0]).longValue();
						if (dbPos == query_coordinate) {
							dbForPosition.add(dbLine);
						}
						else {
							if (dbPos > query_coordinate) {
								allLinesWithCorrespondingPosFound = true;
							}
						//Preserve the readed line for next search
							PreservedDbLine = dbLine;
						}
					}

					//Step 7:At this stage, dbForPosition array contains all rows from SIFT database with the corresponding query's coordinate
					//       We obtain the one(s) with the correct alternate allele(s)
					Map<String, List<String>> vcfAnnos = new HashMap<String, List<String>>();
					for (String altAllele : alternateAlleles) {
						if (!vcfAnnos.containsKey(altAllele)) {
							List<String> annotationsForAllele = new ArrayList<String>();
							vcfAnnos.put(altAllele, annotationsForAllele);
						}
						List<String> annotationsForAllele = vcfAnnos.get(altAllele);
						for (String lineFromDB : dbForPosition) {
							String[] dbComp = lineFromDB.split("\t");
							String dbRefAllele = dbComp[1];
							String dbAltAllele = dbComp[2];
							String dbGeneName = dbComp[3];
							int queryReferenceAllele_len = queryReferenceAllele.trim().length(); 
							int altAllele_len = altAllele.trim().length(); 
							if ((queryReferenceAllele_len >1 || altAllele_len >1)) 
							{ 
								String annotation = generateAnnotation(lineFromDB, altAllele, queryReferenceAllele);
								annotationsForAllele.add(annotation);
								vcfAnnos.put(altAllele, annotationsForAllele);
							}
								
							else 

								if(dbRefAllele.equalsIgnoreCase(queryReferenceAllele) && altAllele.equalsIgnoreCase(dbAltAllele)
										&& !dbGeneName.equals("NA")) {
									String annotation = generateAnnotation(lineFromDB, altAllele, queryReferenceAllele);
									annotationsForAllele.add(annotation);
									vcfAnnos.put(altAllele, annotationsForAllele);
								}
						}
					}


					// Step 8:  Decide if it is multiple Transcripts or single Transcript
					//           If single, we just take the first
					String finalAnnotation = "";
					if (multipleTranscripts) {
						Set<String> alt_alleles = vcfAnnos.keySet();
						List<String> allAnnotations = new ArrayList<String>();
						List<String> uniqallAnnotations  = new ArrayList<String>();
						for (String alt : alt_alleles) {
							List<String> anno = vcfAnnos.get(alt);
							allAnnotations.addAll(anno);
						}
						// Take Unique records in allAnnotations
						uniqallAnnotations = removeDuplicates(allAnnotations);
						finalAnnotation = StringUtils.join(uniqallAnnotations, ","); // Following the way Variant Effect Predictor outputs its annotations
					} else {
						Set<String> alt_alleles = vcfAnnos.keySet();
						List<String> oneAnnotationPerAltAllele = new ArrayList<String>();
						if (alt_alleles.size() > 0) { // There is at least 1 annotation
							for (String alt : alt_alleles) {
								List<String> annotationForOneAltAllele = vcfAnnos.get(alt);
								if (annotationForOneAltAllele.size() > 0) {
									oneAnnotationPerAltAllele.add(annotationForOneAltAllele.get(0)); // We just report the first annotation
								}
							}
							finalAnnotation = StringUtils.join(oneAnnotationPerAltAllele, ",");
						}
					}
					
					//Step 9: Now, we append SIFT information to the info part , get counts for with and without SIFt annotation
					String INFO = components[7];
					if (INFO != null) {
						if (!finalAnnotation.equals("")) {
							components[7] = INFO + ";SIFTINFO="+ finalAnnotation;
							numberWithSIFTAnnotations++;
						} 
						else {
							numberWithoutSIFTAnnotations++;
						}
					} 
					else if (!finalAnnotation.equals("")) {
						components[7] = "SIFTINFO=" + finalAnnotation;
						numberWithSIFTAnnotations++;
					} else {
						numberWithoutSIFTAnnotations++;
					}
					//append to vcf
					vcfOut.write(StringUtils.join(components, "\t") + "\n");
					//append to tsv
					
					String[] tsvLines = generateLinesForTSV(queryline,finalAnnotation, multipleTranscripts,fileFormat);
					for (String tsvLine : tsvLines) {
						tsvOut.write(tsvLine + "\n");
					}
			
				}
			
				//region if IN is not found in region file for query coordinate
				else {
					// Then this are no SIFT scores for this query, because it is not in an exome (it is OUT according to the regions file)
					numberWithoutSIFTAnnotations++;
					vcfOut.write(queryline + "\n");
					String[] tsvLines = generateLinesForTSV(queryline,null, multipleTranscripts, fileFormat);
					for (String tsvLine : tsvLines) {
						tsvOut.write(tsvLine + "\n");
					}

				}
				currentChromosome = chr;
				
			}
		} catch (NumberFormatException nfe) {
			nfe.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {

			// Close file streams
			if (currentChromosome == null)
				currentChromosome = this.ChrNo;

			//region Close the output files
			try {
				tsvOut.close();
				tsvWriter.close();
				vcfOut.close();
				vcfWriter.close();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}

			// notify to main that work is completed
			main.notifyTable(currentChromosome, numberWithSIFTAnnotations,numberWithoutSIFTAnnotations);

			//sleep for 5sec to end the work
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}// end run()

	private static List<String> removeDuplicates(List<String> input){
		List<String> UniqList  = new ArrayList<String>();
		for(int i =0; i< input.size(); i++){
			if(!UniqList.contains(input.get(i))){
				UniqList.add(input.get(i));
			}
		}
		return UniqList;
	}
	// region Utility code
	private String adjust(String original) {
		if (original == null)
			return "NA";

		// return na when there is empty field
		if (original.trim().length() == 0)
			return "NA";

		return original;
	}

	// Generate TSV Code
	@SuppressWarnings("unused")
	private String[] generateLinesForTSV(String query, String annotations, boolean isMultiTranscript, String fileFormat) {
		List<String> result = new ArrayList<String>();

		if (fileFormat.equals(SIFTConstants.VCF_FORMAT)) {
			// Parse query
			String[] data = query.split(SIFTConstants.VCF_DELIMITER);
			String chr = data[SIFTConstants.VCF_CHR_COL];
			String pos = data[SIFTConstants.VCF_POS_COL];
			String ref = data[SIFTConstants.VCF_REF_COL];
			String alt = data[SIFTConstants.VCF_ALT_COL];
			if (annotations == null || annotations.equals("")) {
				result.add(chr + "\t" + pos + "\t" + ref + "\t" + alt+ "\tNA\tNA\tNA\tNA\t"+ "NONCODING"+"\tNA\tNA\tNA\tNA\tNA\tNA\tNA\tNA");
			} else {
				String[] annos = annotations.split(",");
				for (String annotation : annos) {
					// Allele|Transcript|Gene|GeneName|Region|Ref_Amino_Acid/Alt_AminoAcid|Amino_position|SIFT_score|SIFT_median|SIFT_num_seqs|Allele_Type|SIFT_prediction
					String[] annoData = annotation.split("\\|");
					String alt_allele = annoData[SIFTConstants.TSV_ALT_COL];
					String transcript_id = annoData[SIFTConstants.TSV_TRN_COL];
					String gene_id = adjust(annoData[SIFTConstants.TSV_GID_COL]);
					String gene_name = adjust(annoData[SIFTConstants.TSV_GENE_NAME]);
					String region = adjust(annoData[SIFTConstants.TSV_REGION]);
					String[] amino_acids = annoData[SIFTConstants.TSV_AA1_AA2_COL].split("/");
					String aa1 = ""; String aa2 = "";
					String Variant_Type ="";
					String aapos = annoData[SIFTConstants.TSV_APOS_COL]; 
					aapos = adjust(aapos);
					if (amino_acids.length == 2) {
						aa1=amino_acids[0];
						aa2=amino_acids[1];
						Variant_Type = getVariantType(ref, alt_allele, region, aa1,aa2, aapos); //SW modified
						aa1 = amino_acids[0];
						aa1 = adjust(aa1);
						aa2 = amino_acids[1];
						aa2 = adjust(aa2);
					} else {
					}
					String score = annoData[SIFTConstants.TSV_SIFTSCORE_COL];
					score = adjust(score);
					String median = annoData[SIFTConstants.TSV_MEDIAN_COL];
					median = adjust(median);
					String numseqs = annoData[SIFTConstants.TSV_NUMSEQ_COL];
					numseqs = adjust(numseqs);
					String dbsnp = adjust(annoData[SIFTConstants.TSV_dbSNP]);
					String prediction = annoData[SIFTConstants.TSV_PREDICTION];
					prediction = adjust(prediction);
					result.add(chr + "\t" + pos + "\t" + ref + "\t"+ alt_allele + "\t" + transcript_id + "\t" + gene_id + "\t" + gene_name + "\t" + region + "\t" + Variant_Type+"\t"
							+ aa1 + "\t" + aa2 + "\t" + aapos + "\t" + score + "\t" + median + "\t" + numseqs + "\t" + dbsnp + "\t" + prediction);
				}
			}
		} 
		else {
			// This is for SIFT format
		}

		return result.toArray(new String[result.size()]);
	}

	///SW modified following
	private String getVariantType(String ref, String Alt_allele, String region, String aa1, String aa2, String aapos) {

		String res = ""; 
		int ref_len = ref.trim().length();
		int alt_len = Alt_allele.trim().length();
		int diff_ref_alt = ref_len - alt_len;

		if(ref_len >1 || alt_len >1){
			if((ref_len > 1) &&  (diff_ref_alt>=1 && diff_ref_alt%3!=0))
				res = "FRAMESHIFT DELETION";
			else if((ref_len>=1) && (diff_ref_alt<= -1 && diff_ref_alt%3!=0))
				res = "FRAMESHIFT INSERTION";
			else if((ref_len > alt_len)&&(ref_len >=3) && (alt_len == 1)&& (diff_ref_alt%3==0))
				res = "NONFRAMESHIFT DELETION";
			else if((alt_len>3)&&(alt_len > ref_len)&& (ref_len==1) && (diff_ref_alt%3==0))
				res = "NONFRAMESHIFT INSERTION";	
			else if((ref_len>1) && (alt_len > 1))
				res = "SUBSTITUTION";//}
		}
		else{ 		
			if(region.equals("NOANNOT")){
				res = "NA";
			}
			else if(!region.equals("CDS")){
				res="NONCODING";
			}
			else{
				if(aa1.equals("NA") || aa2.equals("NA")){
					res="NA";
				}
				else{
					if((aa1.trim().length() ==0) && (aa2.trim().length()==0))
						res ="NA";  
					else if(aa1.equals("*") && !aa2.equals("*")) 
						res = "STOP-LOSS"; 
					else if(!aa1.equals("*") && aa2.equals("*")) 
						res ="STOP-GAIN"; 
					else if(aapos.equals("1")&& aa1.equals("M")&& !aa2.equals("M")) //SW: Insert start-lost //
						res = "START-LOST";
					else if(aa1.equals(aa2)) 
						res = "SYNONYMOUS";
					else if (!aa1.equals(aa2)) 
						res = "NONSYNONYMOUS";
				}
			}
		}
		return res;
	} 

    // Generate CSV Code
	private String generateAnnotation(String dbLine, String altAllele, String refAllele) {

		String[] comps = dbLine.split("\t");
		String enst = comps[SIFT_DB_COLS.TRN_ID];
		String ensg = comps[SIFT_DB_COLS.GENE_ID];
		String geneName = comps[SIFT_DB_COLS.GENE_NAME];
		String region = comps[SIFT_DB_COLS.REGION];
		String refAmino = comps[SIFT_DB_COLS.AA1];
		String altAmino = comps[SIFT_DB_COLS.AA2];
		// String Variant_Type = getVariantType(refAmino, altAmino);
		String aminoPos = comps[SIFT_DB_COLS.AAPOS];
		String siftScore = comps[SIFT_DB_COLS.SIFTSCORE];
		String siftMedian = comps[SIFT_DB_COLS.MEDIAN];
		// String noSeqRep = comps[SIFT_DB_COLS.NO_SEQ_REP];
		String siftNumSeqs = comps[SIFT_DB_COLS.NUM_SEQS];
		String dbsnp = comps[SIFT_DB_COLS.DBSNP];
		//String siftType = comps[SIFT_DB_COLS.TYPE];
		String prediction = "NA";
		if (siftScore != null && !siftScore.equals("") && !siftScore.equals("NA")) {
			float siftScoreAsNum = Float.valueOf(siftScore).floatValue();
			float siftMedianAsNum = Float.valueOf(siftMedian).floatValue(); ///SW added
			if ((siftScoreAsNum < siftPredictionThreshold) && (siftMedianAsNum > 3.5)) ///SW added
				prediction = "DELETERIOUS (*WARNING! Low confidence)"; //SW added
			else if (siftScoreAsNum < siftPredictionThreshold) {
				prediction = "DELETERIOUS";
			} 
			else {
				prediction = "TOLERATED";
			}
		}
		String vartype = getVariantType(refAllele, altAllele, region, refAmino, altAmino, aminoPos);//SW  modified ...should add REF_ALLELE as var
		if(vartype.equals("FRAMESHIFT INSERTION") || vartype.equals("FRAMESHIFT DELETION") || vartype.equals("NONFRAMESHIFT INSERTION") || 
				vartype.equals("NONFRAMESHIFT DELETION") || vartype.equals("SUBSTITUTION")){
			refAmino = altAmino = aminoPos = siftScore = siftMedian = siftNumSeqs = dbsnp = prediction = "NA";
		}
		String[] tmp = { altAllele, enst, ensg, geneName, region, vartype, refAmino + "/" + altAmino, aminoPos, siftScore, siftMedian,
				siftNumSeqs, dbsnp, prediction};
		return StringUtils.join(tmp, '|');
	}

}
