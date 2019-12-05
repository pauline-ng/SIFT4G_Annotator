package org.siftdna.siftobjects;

import org.siftdna.main.SIFTConstants;

public class SIFTSpaceQuery extends AbstractSIFTQuery {

	public SIFTSpaceQuery(String query) {
		super(query);
		parseQuery(query);
	}//end constructor
	
	@Override
	public void parseQuery(String query) {
				
		String [] components = this.query.split(SIFTConstants.SIFT_DELIMITER);
		
		setChromosome(components[SIFTConstants.SIFT_CHR_COL]);
		
		setPosition(Long.valueOf(components[SIFTConstants.SIFT_SPACE_POS_COL]).longValue());
		setRefAllele(components[SIFTConstants.SIFT_SPACE_ALLELE_COL]);
		setAltAlleles(components[SIFTConstants.SIFT_SPACE_COMMENTS_COL]);
		setOrn(Integer.valueOf(components[SIFTConstants.SIFT_SPACE_ORN_COL]).intValue());
		
		String alleles = components[SIFTConstants.SIFT_RESIDUE_ALLELE_COL];
		String [] a = alleles.split("/");
		setRefAllele(a[0]);
		setAltAlleles(a[1]);
		
	}	
}
