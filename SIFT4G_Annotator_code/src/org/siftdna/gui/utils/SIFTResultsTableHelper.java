package org.siftdna.gui.utils;

import java.util.ArrayList;
import java.util.List;

import org.siftdna.siftobjects.SIFTResult;
import org.siftdna.siftobjects.SingleSIFTResult;

public class SIFTResultsTableHelper {

	
	public SIFTResultsTableHelper() {
		super();
	}
	
	public SingleSIFTResult [] createSingleSIFTResults(List<SIFTResult> siftResults, boolean isMultiTranscripts) {
		// Each SIFTResult object may have more than 1 annotation.
		// For the purpose of displaying results on the JTable,
		// We need to separate these and return as a JTableSource
		
		List<SingleSIFTResult> results = new ArrayList<SingleSIFTResult>();
		
		
		for(SIFTResult r : siftResults) {
			
			String chr = r.getChr();
			long pos = r.getPos();
			String ref = r.getRef();
			int orn = r.getSiftQuery().getOrn();
			

			List<String> transcript_ids = r.getTranscript_ids();
			List<String> alt_alleles = r.getAlt_alleles();
			List<String> gene_ids = r.getGene_ids();
			List<String> ref_aminos = r.getRef_amino();
			List<String> alt_aminos = r.getAlt_aminos();
			List<Integer> amino_positions = r.getAmino_positions();
			List<Float> siftScores = r.getSiftScores();
			List<Float> siftMedians = r.getSiftMedians();
			List<Integer> siftNumSeqs = r.getSiftNumSeqs();
			List<String> annotations = r.getAnnotations();
			
			List<String> siftPreds = r.getSiftPreds();
			
			int num = ref_aminos.size();
			if (!isMultiTranscripts) {
				if (num > 0) { num = 1; }
			}
			
			for(int i = 0; i < num; i++) {

				String transcript = transcript_ids.get(i);
				String alt_allele = alt_alleles.get(i);
				String gene_id = gene_ids.get(i);
				String ref_amino = ref_aminos.get(i);
				String alt_amino = alt_aminos.get(i);
				int amino_pos = amino_positions.get(i);
				float score = siftScores.get(i);
				float median = siftMedians.get(i);
				int numSeqs = siftNumSeqs.get(i);
				String anno = annotations.get(i);
				String siftPred = siftPreds.get(i);
				
				SingleSIFTResult singleResult = new SingleSIFTResult(chr, pos, ref, alt_allele, orn,
						transcript, gene_id, ref_amino, alt_amino, amino_pos, score, median, numSeqs, anno, siftPred);				
				
				results.add(singleResult);
			}
			
		}
		
		return results.toArray(new SingleSIFTResult[results.size()]);
		
	}//end createSingleSIFTResults;
	
	
	
	
	
	
	
	
	
}//end SIFTResultsTableHelper
