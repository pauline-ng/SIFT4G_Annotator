package org.siftdna.main;

public interface SIFTConstants {

	static final String OUTPUT_FOLDER_BTN_TEXT = "Results";
	
	static final String VCF_FORMAT = "VCF";
	static final String SIFT_RESIDUE_FORMAT = "SIFT_RESIDUE";
	static final String SIFT_SPACE_FORMAT = "SIFT_SPACE";
	static final String UNKNOWN_FORMAT = "UNKNOWN";
	
	static final String SIFT_DB_DELIMITER = "\t";
	static final String SIFT_DELIMITER = ",";
	static final String VCF_DELIMITER = "\t";
	
	static final String SIFT_PREDICTION_THRESHOLD_OPT = "p";	
	static final String SIFT_PREDICTION_THRESHOLD_OPT_DESC = "Threshold for SIFT prediction";
	
	static final String INPUT_FILE_OPT = "i";	
	static final String INPUT_FILE_OPT_DESC = "Input query file";
	
	static final String DATABASE_DIR_OPT = "d";
	static final String DATABASE_DIR_OPT_DESC = "Directory where SIFT database is stored.";
		
	static final String OUTPUT_FILE_OPT = "o";
	static final String OUTPUT_FILE_OPT_DESC = "Output file";

	static final String OUTPUT_DIR_OPT = "r";
	static final String OUTPUT_DIR_OPT_DESC = "Output directory";
		
	static final String HEADLESS_OPT = "c";
	static final String HEADLESS_OPT_DESC = "no GUI";
	
	static final String MULTI_TRANSCRIPTS_OPT = "t";
	static final String MULTI_TRANSCRIPTS_OPT_DESC = "Emit multiple transcripts";
	
	static final String BOUNDARY_IN = "IN";
	static final String BOUNDARY_OUT = "OUT";
	
	static final String HELP_OPTION = "h";
	static final String HELP_DESC = "Print Usage help";
	static final int REQ_COL = 8 ;
	
	// #CHROM	POS	ID	REF	ALT	QUAL	FILTER	INFO	FORMAT	VSample	9
	static final int VCF_CHR_COL = 0;
	static final int VCF_POS_COL = 1;
	static final int VCF_ID_COL = 2;
	static final int VCF_REF_COL = 3;
	static final int VCF_ALT_COL = 4;
	static final int VCF_QUAL_COL = 5;
	static final int VCF_FILTER_COL = 6;
	static final int VCF_INFO_COL = 7;
	static final int VCF_FORMAT_COL = 8;
	static final int VCF_FIRST_SAMPLE_COL = 9;
	
	// SIFT format (legacy)
	static final int SIFT_CHR_COL = 0;
	static final int SIFT_RESIDUE_POS_COL = 1;
	static final int SIFT_RESIDUE_ORN_COL = 2;
	static final int SIFT_RESIDUE_ALLELE_COL = 3;	
	static final int SIFT_RESIDUE_COMMENTS_COL = 4;
	
	static final int SIFT_SPACE_POS_COL = 2;	
	static final int SIFT_SPACE_ORN_COL = 3;
	static final int SIFT_SPACE_ALLELE_COL = 4;	
	static final int SIFT_SPACE_COMMENTS_COL = 5;
	
	static final String SR_CHR = "chr";
	static final String SR_REF = "ref";
	static final String SR_ALT = "alt";
	static final String SR_COORD = "coord";
	static final String SR_TRN = "transcript";
	static final String SR_GID = "gene_id";
	static final String SR_AA1 = "aa1";
	static final String SR_AA2 = "aa2";
	static final String SR_APOS = "apos";
	static final String SR_SIFTSCORE = "siftscore";
	static final String SR_MEDIAN = "siftmedian";
	static final String SR_NUMSEQ = "siftnumseqs";
	static final String SR_ANNO = "siftanno";
	static final String SR_PRED = "siftpred";
	
	
	
	static final int SR_CHR_COL = 0;
	static final int SR_COORD_COL = 1;
	static final int SR_REF_COL = 2;
	static final int SR_ALT_COL = 3;
	static final int SR_TRN_COL = 4;
	static final int SR_GID_COL = 5;
	static final int SR_AA1_COL = 6;
	static final int SR_AA2_COL = 7;
	static final int SR_APOS_COL = 8;
	static final int SR_SIFTSCORE_COL = 9;
	static final int SR_MEDIAN_COL = 10;
	static final int SR_NUMSEQ_COL = 11;
	static final int SR_ANNO_COL = 12;
	static final int SR_PRED_COL = 13;
	
	/// constants for TSV
	// A|ENSCAFT00000037887|ENSCAFG00000024550|NA|CDS|S/R|21|0.013|2.48|67|novel|DAMAGING
	static final int TSV_ALT_COL = 0;
	static final int TSV_TRN_COL = 1;
	static final int TSV_GID_COL = 2;
	static final int TSV_GENE_NAME = 3;
	static final int TSV_REGION = 4;
	static final int TSV_VARIANT_TYPE = 5;
	static final int TSV_AA1_AA2_COL = 6;
	//static final int TSV_AA2_COL = 9;
	static final int TSV_APOS_COL = 7;
	static final int TSV_SIFTSCORE_COL = 8;
	static final int TSV_MEDIAN_COL = 9;
	static final int TSV_NUMSEQ_COL = 10;
	static final int TSV_dbSNP = 11;
	static final int TSV_PREDICTION = 12;
	
	

	static final String[] COL_HEADINGS = {"Chr","Coord","Ref","Alt","Transcript","Gene","Region","VariantType","AA1","AA2","AAPOS","Score", "Median","Num.Seqs","dbSNP","Prediction"};
	
	static final String VCF_FIRST_ROW = "##fileformat=VCFv4.1";
	static final String SIFT_HEADER = "##INFO=<ID=SIFTINFO,Number=.,Type=String,Description=\"SIFT information. Format: Allele|Transcript|GeneId|GeneName|Region|VariantType|Ref_Amino_Acid/Alt_AminoAcid|Amino_position|SIFT_score|SIFT_median|SIFT_num_seqs|Allele_Type|SIFT_prediction\">";
	static final String SIFT_HEADER1 = "#CHROM\tPOS	ID\tREF\tALT\tQUAL\tFILTER\tINFO\tFORMAT"; // This is added if user input is not a VCF file
	static final String TAB_DELIMITED_HEADER = "CHROM\tPOS\tREF_ALLELE\tALT_ALLELE\tTRANSCRIPT_ID\tGENE_ID\tGENE_NAME\tREGION\tVARIANT_TYPE" +
												"\tREF_AMINO\tALT_AMINO\tAMINO_POS\t" + 
												"SIFT_SCORE\tSIFT_MEDIAN\tNUM_SEQS\tdbSNP\tSIFT_PREDICTION\n";

	static final String[] RESULTS_COL_HEADINGS = { "Chromosome", "Number of Variants with SIFT4G Annotation", "Number of Variants without SIFT4G Annotation" };
	
	static final String SIFTDB_LINK = "https://sift.bii.a-star.edu.sg/sift4g/public/Databases.txt";
	static final String SIFTDB_ANNOUNCEMENT = "https://sift.bii.a-star.edu.sg/sift4g/announcement.txt";
	static final String SIFTDB_HELP = "https://sift-dna.org/";
}//end SIFTConstants
