package org.siftdna.siftobjects;

public interface SIFT_DB_COLS {

//  pos     ref     alt     transcript      gene            aa1    aa2      aapos   score   median  numseq  type    sort_number (not used)
//	7680    A       A       FBtr0300689     FBgn0031208     M       M       1       1.000   3.65    5       ref     2

//NEW DATABSE	
/* 0	1	2	     3	      			4				5			6	7		8		9	10		11		12		13
Coord	ref	new	Transcript			Gene_id				GENE	dbSNP	ORI_aa	NEW_aa	Pos	SIFT	Median	NumSEQ	Severity
1036355	G	A	ENSCAFT00000048354	ENSCAFG00000009799	NCAPD3	novel	A			T	376	0.009	3.24	24		1
*/

	
	/*static int POS = 0;
	static int REF = 1;
	static int ALT = 2;
	static int TRN_ID = 3;
	static int GENE_ID = 4;
	static int AA1 = 5;
	static int AA2 = 6;
	static int AAPOS = 7;
	static int SIFTSCORE = 8;
	static int MEDIAN = 9; 
	static int NUM_SEQS = 10;
	static int TYPE = 11;*/
	//NEW DATABSE	
	/* 0	1	2	     3	      			4				5			6	7		8		9	10		11		12		13
	Coord	ref	new	Transcript			Gene_id				GENE	dbSNP	ORI_aa	NEW_aa	Pos	SIFT	Median	NumSEQ	Severity
	1036355	G	A	ENSCAFT00000048354	ENSCAFG00000009799	NCAPD3	novel	A			T	376	0.009	3.24	24		1
	*/	
	static int POS = 0;
	static int REF = 1;
	static int ALT = 2;
	static int TRN_ID = 3;
	static int GENE_ID = 4;
	static int GENE_NAME = 5;
	static int REGION =6;
	static int AA1 = 7;
	static int AA2 = 8;
	static int AAPOS = 9;
	static int SIFTSCORE = 10;
	static int MEDIAN = 11;
	//static int NO_SEQ_REP = 11;
	static int NUM_SEQS = 12;
	static int DBSNP = 13;


}
