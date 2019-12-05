package org.siftdna.siftobjects;

import java.util.Hashtable;
import java.util.Map;

import org.siftdna.main.SIFTConstants;

public class SingleSIFTResult {

	
	private Map<String, String> data = null;
	
	
	public SingleSIFTResult(
			String chromosome, long coord, String ref, String alt_allele, int orn,			
			String transcript, String gene_id, 
			String ref_amino, String alt_amino, int amino_pos, 
			float score, float median, int numSeqs, String anno, String SIFTprediction) {
		super();
		
		data = new Hashtable<String, String>();
		data.put(SIFTConstants.SR_CHR, chromosome);
		data.put(SIFTConstants.SR_REF, ref);
		data.put(SIFTConstants.SR_COORD, Long.toString(coord));
		data.put(SIFTConstants.SR_TRN, transcript);
		data.put(SIFTConstants.SR_ALT, alt_allele);
		data.put(SIFTConstants.SR_GID, gene_id);
		data.put(SIFTConstants.SR_AA1, ref_amino);
		data.put(SIFTConstants.SR_AA2, alt_amino);
		data.put(SIFTConstants.SR_APOS, Integer.toString(amino_pos));
		data.put(SIFTConstants.SR_SIFTSCORE, Float.toString(score));
		data.put(SIFTConstants.SR_MEDIAN, Float.toString(median));
		data.put(SIFTConstants.SR_NUMSEQ, Integer.toString(numSeqs));
		data.put(SIFTConstants.SR_ANNO, anno);
		data.put(SIFTConstants.SR_PRED, SIFTprediction);
		
	}//end SingleSIFTResult

	public String getValue(String key) { return data.get(key); }

}//end SingleSIFTResult
