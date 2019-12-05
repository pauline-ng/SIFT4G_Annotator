package org.siftdna.siftobjects;

import org.siftdna.main.SIFTConstants;

public class SIFTDBLine {
// Example of a SIFT DB file	
//  pos     ref     alt     transcript      gene            aa1    aa2      aapos   score   median  numseq  type    sort
//	7680    A       A       FBtr0300689     FBgn0031208     M       M       1       1.000   3.65    5       ref     2
//	7680    A       A       FBtr0300690     FBgn0031208     M       M       1       1.000   3.57    6       ref     2
//	7680    A       A       FBtr0330654     FBgn0031208     M       M       1       1.000   3.67    5       ref     2
//	7680    A       C       FBtr0300689     FBgn0031208     M       L       1       0.000   3.65    5       novel   1
//	7680    A       C       FBtr0300690     FBgn0031208     M       L       1       0.000   3.57    6       novel   1
//	7680    A       C       FBtr0330654     FBgn0031208     M       L       1       0.000   3.67    5       novel   1
//	7680    A       G       FBtr0300689     FBgn0031208     M       V       1       0.000   3.65    5       novel   1
//	7680    A       G       FBtr0300690     FBgn0031208     M       V       1       0.000   3.57    6       novel   1

	private long position = -1L;
	private String refAllele = null;
	private String altAllele = null;
	private String transcriptID = null;
	private String geneID = null;
	private String geneName = null;
	private String region = null;
	private String refAmino = null;
	private String altAmino = null;
	private int aminoPosition = -1;
	private float siftScore = -1.0f;
	private float siftMedian = -1.0f;
	private int siftNumSeqs = -1;
	private String dbSNP = null;
	
	
	public SIFTDBLine(String line) {
		super();
		parseLine(line);
	}
	
	private void parseLine(String line) {
		String [] components = line.split(SIFTConstants.SIFT_DB_DELIMITER);

		this.position = Long.valueOf(components[SIFT_DB_COLS.POS]).longValue();
		
		this.refAllele = components[SIFT_DB_COLS.REF];
		this.altAllele = components[SIFT_DB_COLS.ALT];
		this.transcriptID = components[SIFT_DB_COLS.TRN_ID];
		this.geneID = components[SIFT_DB_COLS.GENE_ID];
		this.geneName = components[SIFT_DB_COLS.GENE_NAME];
		this.region = components[SIFT_DB_COLS.REGION];		
		this.refAmino = components[SIFT_DB_COLS.AA1];
		this.altAmino = components[SIFT_DB_COLS.AA2];
		this.aminoPosition = Integer.valueOf(components[SIFT_DB_COLS.AAPOS]).intValue();

		// Because not all rows in SIFT database will have sift scores
		// we need to check
		String sift_score = components[SIFT_DB_COLS.SIFTSCORE];
		String sift_median = components[SIFT_DB_COLS.MEDIAN];
		String sift_num_seqs = components[SIFT_DB_COLS.NUM_SEQS];
		if (sift_score != null && !sift_score.equals("")) {
			this.siftScore = Float.valueOf(sift_score).floatValue();			
		}
		if (sift_median != null && !sift_median.equals("")) {
			this.siftMedian = Float.valueOf(sift_median).floatValue();
		}
		if (sift_num_seqs != null && !sift_num_seqs.equals("")) {
			float num = Float.valueOf(sift_num_seqs).floatValue();
			this.siftNumSeqs = Math.round(num);
		}
		this.dbSNP = components[SIFT_DB_COLS.DBSNP];		
	}//end parseLine
	

	public long getPosition() { return position; }
	public String getRefAllele() { return refAllele; }
	public String getAltAllele() { return altAllele; }
	public String getTranscriptID() { return transcriptID; }
	public String getGeneID() {	return geneID; }
	public String getGeneName() {	return geneName; }
	public String getRegion() {	return region; }
	public String getRefAmino() { return refAmino; }
	public String getAltAmino() { return altAmino; }
	public int getAminoPosition() { return aminoPosition; }
	public float getSiftScore() { return siftScore; }
	public float getSiftMedian() { return siftMedian; }
	public int getSiftNumSeqs() { return siftNumSeqs; }
	public String getdbSNP() { return dbSNP; }

	
	
	
	
}//end SIFTDBLine
