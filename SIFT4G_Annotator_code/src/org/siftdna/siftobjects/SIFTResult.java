package org.siftdna.siftobjects;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.siftdna.main.SIFTConstants;

public class SIFTResult {

	private boolean hasResult = true;

	private ISIFTQuery siftQuery = null;
	private String chr = null;
	private long pos = -1L;
	private String ref = null;	// There can only be 1 reference allele

	private List<String> transcript_ids = null;
	private List<String> alt_alleles = null;
	private List<String> gene_ids = null;
	private List<String> gene_name = null;
	private List<String> region = null;
	private List<String> ref_aminos = null; 
	private List<String> alt_aminos = null;
	private List<Integer> amino_positions = null;
	private List<Float> siftScores = null;
	private List<String> siftPredictions = null;
	
	private List<Float> siftMedians = null; 
	private List<Integer> siftNumSeqs = null;
	private List<String> dbSNP = null;
	private List<String> annotations = null; // novel, ref, or rsid number

	public SIFTResult(ISIFTQuery siftQuery) { 
		super();
		this.siftQuery = siftQuery;
		createEmptyResult();
	}

	public void createEmptyResult() {
		this.hasResult = false;
		chr = siftQuery.getChromosome();
		pos = siftQuery.getPosition();
		ref = siftQuery.getRefAllele();

		transcript_ids = new ArrayList<String>();
		alt_alleles = new ArrayList<String>();
		gene_ids = new ArrayList<String>();
		gene_name = new ArrayList<String>();
		region = new ArrayList<String>();
		ref_aminos = new ArrayList<String>();
		alt_aminos = new ArrayList<String>();
		amino_positions = new ArrayList<Integer>();
		siftScores = new ArrayList<Float>();
		siftPredictions = new ArrayList<String>();
		siftMedians = new ArrayList<Float>();
		siftNumSeqs = new ArrayList<Integer>();
		dbSNP = new ArrayList<String>();
		annotations = new ArrayList<String>(); // novel, ref, or rsid number	
	}

	public boolean hasResult() { return hasResult; }



	public ISIFTQuery getSiftQuery() {return siftQuery; }
	public void setSiftQuery(ISIFTQuery siftQuery) { this.siftQuery = siftQuery; }
	public String getChr() { return chr; }
	public void setChr(String chr) { this.chr = chr; }
	public long getPos() { return pos; }
	public void setPos(long pos) { this.pos = pos; }
	public String getRef() { return ref; }
	public void setRef(String ref) { this.ref = ref; }

	
	public void addTranscript_ids(String transcript_id) { this.transcript_ids.add(transcript_id); }
	public void addAlt_alleles(String alt_allele) { this.alt_alleles.add(alt_allele); }
	public void addGene_ids(String gene_id) { this.gene_ids.add(gene_id); }	
	public void addGene_name(String gene_name){ this.gene_name.add(gene_name);}
	public void addRegion(String region){ this.region.add(region);}
	public void addRef_amino(String ref_amino) { this.ref_aminos.add(ref_amino); }
	public void addAlt_aminos(String alt_amino) { this.alt_aminos.add(alt_amino); }	
	public void addAmino_positions(int amino_position) { this.amino_positions.add(amino_position); }	
	public void addSiftScores(float siftScore) { this.siftScores.add(siftScore); }
	public void adddbSNP(String dbSNP){ this.dbSNP.add(dbSNP);}
	
	public void addSiftPrediction(float siftScore, float threshold) {
		if (threshold <= siftScore) {
			this.siftPredictions.add("TOLERATED");			
		} else {
			this.siftPredictions.add("DAMAGING");
		}
 
	}
	
	public void addSiftMedians(float siftMedian) { this.siftMedians.add(siftMedian); }
	public void addSiftNumSeqs(int siftNumSeq) { this.siftNumSeqs.add(siftNumSeq); }
	public void addAnnotations(String anno) { this.annotations.add(anno); }	

	public List<String> getTranscript_ids() { return transcript_ids; }
	public List<String> getAlt_alleles() { return alt_alleles; }	
	public List<String> getGene_ids() { return gene_ids; }
	public List<String> getGene_name() {return gene_name;}
	public List<String> getRegion() {return region;}
	public List<String> getRef_amino() { return ref_aminos; }
	public List<String> getAlt_aminos() { return alt_aminos; }
	public List<Integer> getAmino_positions() { return amino_positions; }
	public List<Float> getSiftScores() { return siftScores; }
	public List<Float> getSiftMedians() { return siftMedians; }
	public List<Integer> getSiftNumSeqs() { return siftNumSeqs; }
	public List<String> getAnnotations() { return annotations; }
	public List<String> getdbSNP() { return dbSNP;}
	public List<String> getSiftPreds() { return siftPredictions; }

	private List<String> generateTabDelimitedRows(int num) {
		List<String> result = new ArrayList<String>();
		
		int number = ref_aminos.size();
		if (number > 0) { // there is at least 1 result
			for(int i = 0; i < num; i++) {			
				String r = chr + "\t" + pos + "\t" + ref;
			
				System.out.println("Number of rows in a single result: " + transcript_ids.size());
			
				r += "\t" + alt_alleles.get(i);
				r += "\t" + transcript_ids.get(i);
				r += "\t" + gene_ids.get(i);
				r += "\t" + gene_name.get(i);
				r += "\t" + region.get(i);
				r += "\t" + ref_aminos.get(i);
				r += "\t" + alt_aminos.get(i);
				r += "\t" + amino_positions.get(i);
				r += "\t" + siftScores.get(i);			
				r += "\t" + siftMedians.get(i);
				r += "\t" + siftNumSeqs.get(i);
				r += "\t" + dbSNP.get(i);
				r += "\t" + annotations.get(i);		
				r += "\t" + siftPredictions.get(i);
				result.add(r);	
			}//end for loop
		}
		return result;
	}//end generateTabDelimitedRows
	
	public String [] createTabDelimitedLines(boolean isMultiTranscripts) {
		
		
		int max = transcript_ids.size();
		if (!isMultiTranscripts) { max = 1; } // We only want 1  
	
		List<String> results = generateTabDelimitedRows(max);
		System.out.println("generateTabDelimitedRows: " + results.size());
		
		return results.toArray(new String[results.size()]);
	}

	public String createVCFLine(boolean queryIsVCF, boolean isMultiTranscripts) {
		String result = "";
		if (queryIsVCF) {

			// "##INFO=<ID=SIFTINFO,Number=.,Type=String,Description=\"SIFT information. Format: Allele|Transcript|Ref_Amino_Acid/Alt_AminoAcid|Amino_position|SIFT_score|SIFT_median|SIFT_num_seqs|Allele_Type\">";

			// We take the SIFTQuery line, and insert SIFT information
			String row = siftQuery.getQuery();
			String [] components = row.split(SIFTConstants.VCF_DELIMITER);
			// VCF v4.1 has an additional FORMAT column
			// #CHROM  POS     ID      REF     ALT     QUAL    FILTER  INFO 
			// #CHROM  POS     ID      REF     ALT     QUAL    FILTER  INFO FORMAT	HG01098	NA11830	NA11994	NA18510	Edward	Sophie	James	Louise
			// However, since we are only inserting into INFO, it does not affect us
			String original_info = components[7];			
			// Allele|Transcript|Ref_Amino_Acid/Alt_AminoAcid|Amino_position|SIFT_score|SIFT_median|SIFT_num_seqs|Allele_Type
			List<String> singleInfo = new ArrayList<String>();
			int num = transcript_ids.size();
			for (int i = 0; i < num; i++) {				
				String alt = alt_alleles.get(i);
				String trn = transcript_ids.get(i);
				String geneid = gene_ids.get(i);
				String genename = gene_name.get(i);
				String ref_amino = ref_aminos.get(i);
				String alt_amino = alt_aminos.get(i);
				String aapos =amino_positions.get(i).toString();
				float sScore = siftScores.get(i); 
				String siftScore = ".";
				if (sScore != -1.0f) { siftScore = Float.toString(sScore); }
				
				float sMedian = siftMedians.get(i);
				String siftMedian = ".";
				if (sMedian != -1.0f) { siftMedian = Float.toString(sMedian); }

				int sNumSeqs = siftNumSeqs.get(i);
				String siftNumSeqs = ".";
				if (sNumSeqs != -1) { siftNumSeqs = Integer.toString(sNumSeqs); }

				String dbsnp = dbSNP.get(i);
				
				String type = annotations.get(i);
				
				String SIFTPrediction = siftPredictions.get(i); 

				String [] infoComps = { alt, trn, geneid, genename, ref_amino + "/" + alt_amino, aapos, siftScore, siftMedian, siftNumSeqs, dbsnp, SIFTPrediction };
				String singleInfoDatum = StringUtils.join(infoComps, "|");
				singleInfo.add(singleInfoDatum);
			}//end for

			if (isMultiTranscripts) {
				if (num > 0) {
					String newInfo = original_info + ";SIFTINFO=";
					String singleLine = StringUtils.join(singleInfo, ",");
					newInfo += singleLine;
					components[7] = newInfo;
				} else {
					components[7] = original_info; // nothing to add
				}
			} else {
				// Just take the first
				if (num > 0) {
					String info = singleInfo.get(0);
					String newInfo = original_info + ";SIFTINFO=" + info;
					components[7] = newInfo;
				} else {
					components[7] = original_info;
				}
			}
			result = StringUtils.join(components, "\t");

		} 
		else {
			// The input format is assumed to be SIFT format (in the future, we can add others
			String chr = siftQuery.getChromosome();
			String pos = Long.toString(siftQuery.getPosition());
			String id = ".";

			String ref = getRef();
			List<String> alt_alleles = getAlt_alleles();
			SortedSet<String> alternates = new TreeSet<String>();
			for(String a : alt_alleles) { alternates.add(a); }			
			String alt = StringUtils.join(alternates, ",");
			String qual = ".";
			String filter = ".";			
			List<String> transcripts = getTranscript_ids();
			List<String> ref_amino = getRef_amino();
			List<String> alt_amino = getAlt_aminos();
			List<Integer> amino_positions = getAmino_positions();
			List<Float> siftScores = getSiftScores();
			List<Float> siftMedians = getSiftMedians();
			List<Integer> siftNumSeqs = getSiftNumSeqs();
			List<String> annotation = getAnnotations();	

			// "##INFO=<ID=SIFTINFO,Number=.,Type=String,Description=\"SIFT information. Format: Allele|Transcript|Ref_Amino_Acid/Alt_AminoAcid|Amino_position|SIFT_score|SIFT_median|SIFT_num_seqs|Allele_Type\">";
			// #CHROM  POS     ID      REF     ALT     QUAL    FILTER  INFO
			int numAnnotations = ref_amino.size();
			List<String> information = new ArrayList<String>(); 
			
			result = chr + "\t" + pos + "\t" + id + "\t" + ref + "\t" + alt + "\t" + qual + "\t" + filter + "\t";
			for(int i = 0; i < numAnnotations; i++) {
				String allele = alt_alleles.get(i);
				String transcript = transcripts.get(i);
				String refAmino = ref_amino.get(i);
				String altAmino = alt_amino.get(i);
				String amino_change = refAmino + "/" + altAmino;
				String aa_pos = Integer.toString(amino_positions.get(i));
				String sift_score = Float.toString(siftScores.get(i));
				String sift_median = Float.toString(siftMedians.get(i));
				String sift_num_seqs = Integer.toString(siftNumSeqs.get(i));
				String allele_type = annotation.get(i);
				String siftPrediction = siftPredictions.get(i);
				String [] data = {allele, transcript, refAmino, amino_change, aa_pos, sift_score, sift_median, sift_num_seqs, allele_type, siftPrediction};
				String anno = StringUtils.join(data, "|");
				information.add(anno);
			}
			
			String infoAnnotations = "SIFT_INFO=";
			
			if (information.isEmpty()) {
				infoAnnotations = ".";
			} else {
				 
				if (isMultiTranscripts) {
					infoAnnotations += StringUtils.join(information, ",");
				} else {
					infoAnnotations += information.get(0); // Single transcript
				}				
			}
			result += infoAnnotations + "\t."; // The last tab and period is for the FORMAT
		}//end else
		
		
		return result;
	}




}//end SIFTResult
